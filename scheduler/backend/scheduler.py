#!/usr/bin/env python
import redis
import os
import tempfile
import subprocess
import shutil
import urllib
import socket
import time
from jinja2 import Template
from multiprocessing import Process
from uwsgidecorators import thread
import uwsgi
import signal

WORKDIR = '/var/lib/mepc'

class DeployScheduler():
  def __init__(self, team):
    self.run = 0
    self.team = team
    self.pipeline = ( self.package_app,
                      self.instantiate_infra,
                      self.functional_tests,
                      self.switch_env,
                      self.shutdown_infra)
    self.redis = redis.Redis()
    self.servers = {}
    servers = self.redis.hkeys(team)
    for server in servers:
      url = self.redis.hmget(team, server)
      env, role = server.split(':')
      if not self.servers.has_key(env):
        self.servers[env] = {}
      self.servers[env][role] = url[0]
    self.ps = redis.Redis().pubsub()
    self.ps.subscribe('deploy')
    self.dhcp_prefix = self.redis.hget('dhcp', team)
    with open('templates/files/Vagrantfile', 'r') as conf_file:
      tpl_content = conf_file.read()
      self.template = Template(tpl_content)
    self.envs = map(lambda x: x.lower(), self.redis.lrange('envs', 0, 99))
    self.roles = map(lambda x: x.lower(), self.redis.lrange('roles', 0, 99))
    self.steps = {}
    for key, value in self.redis.hgetall('steps').items():
      self.steps[key] = value.split(':')

  def package_app(self):
    try:
      tmp_dir = tempfile.mkdtemp(prefix=self.team)
      os.chdir(tmp_dir)
      status = subprocess.call(['git', 'clone', WORKDIR+'/repos/{}.git'.format(self.team)], stderr=self.logs, stdout=self.logs)
      if status != 0:
        return False
      os.chdir('{}'.format(self.team))
      status = subprocess.call(['mvn', 'clean', 'install', '-DskipTests=true'], stderr=self.logs, stdout=self.logs)
      if status != 0:
        return False
      self.version = self.get_pom_version('mepc-server')
      shutil.copy('mepc-server/target/mepc-server-{}.war'.format(self.version), WORKDIR+'/deploy/{}'.format(self.team))
      os.chdir('/tmp')
      return True
    finally:
      shutil.rmtree(tmp_dir)
  
  def instantiate_infra(self):
    env_idx = int(self.redis.hget('teams', self.team)) % 2
    target_env = self.envs[env_idx]
    servers = self.servers[target_env]
    dhcp_prefix = self.redis.hget('dhcp', self.team) + str(env_idx)
    current_roles = self.steps[self.version.split('.')[0]]
    for role, url in servers.items():
      if role in current_roles:
        role_idx = str(map(lambda x: x.lower(), self.roles).index(role))
        fqdn = '{team}-{env}-{role}'.format(team=self.team, env=target_env, role=role + str(self.run))
        vagrantfile = self.template.render(fqdn=fqdn, mac=dhcp_prefix+role_idx, puppetmaster='{}-puppet'.format(self.team))
        print 'Launching {role} ({fqdn}) on {url}'.format(role=role, fqdn=fqdn, url=url)
        resp = urllib.urlopen(url, urllib.urlencode({'config': vagrantfile, 'action': 'up'}))
        if resp.getcode() > 399:
          print resp.getcode()
          self.destroy_env(target_env)
          return False
    if 'web' in current_roles:
      self.send_haproxy_cmd('set weight blue-back/web 1')
      self.send_haproxy_cmd('set weight green-back/web 1')
      self.send_haproxy_cmd('set weight blue-back/app 0')
      self.send_haproxy_cmd('set weight green-back/app 0')
    else:
      self.send_haproxy_cmd('set weight blue-back/app 1')
      self.send_haproxy_cmd('set weight green-back/app 1')
      self.send_haproxy_cmd('set weight blue-back/web 0')
      self.send_haproxy_cmd('set weight green-back/web 0')
    return True
  
  def functional_tests(self):
    try:
      env_idx = int(self.redis.hget('teams', self.team)) % 2
      target_env = self.envs[env_idx]
      tmp_dir = tempfile.mkdtemp(prefix=self.team)
      os.chdir(tmp_dir)
      status = subprocess.call(['git', 'clone', WORKDIR+'/repos/{}.git'.format(self.team)])
      if status != 0:
        return False
      os.chdir('{}/mepc-functional-tests'.format(self.team))
      status = subprocess.call(['mvn', 'clean', 'install', '-Dfr.valtech.appHost=127.0.0.1:8{env}{dhcp}'.format(env=env_idx+1, dhcp=self.dhcp_prefix)], stderr=self.logs, stdout=self.logs)
      if status != 0:
        self.destroy_env(target_env)
        return False
      os.chdir('/tmp')
      return True
    finally:
      shutil.rmtree(tmp_dir)
  
  def switch_env(self):
    new_env = self.envs[int(self.redis.hget('teams', self.team)) % 2]
    old_env = self.envs[(int(self.redis.hget('teams', self.team))+1) % 2]
    for backend in [tmp+'-back' for tmp in ('nosql', 'rdbms', 'app', 'web')]:
      self.send_haproxy_cmd('set weight {backend}/{env} 1'.format(backend=backend, env=new_env))
    for backend in [tmp+'-back' for tmp in ('web', 'app', 'rdbms', 'nosql')]:
      self.send_haproxy_cmd('set weight {backend}/{env} 0'.format(backend=backend, env=old_env))
    self.redis.hincrby('teams', self.team, 1)
    return True

  def shutdown_infra(self):
    old_env = self.envs[(int(self.redis.hget('teams', self.team))) % 2]
    self.destroy_env(old_env)
    return True
    
  def destroy_env(self, env):
    servers = self.servers[env]
    current_roles = self.steps[self.version.split('.')[0]]
    for role, url in servers.items():
      if role in current_roles:
        print 'Destroying {role} on {url}'.format(role=role, url=url)
        resp = urllib.urlopen(url, urllib.urlencode({'action': 'destroy -f'}))
        if resp.getcode() > 399:
          print resp.getcode()
          return False
    return True

  def send_haproxy_cmd(self, command):
    sock = socket.socket(socket.AF_UNIX)
    sock.connect(WORKDIR+'/{team}.sock'.format(team=self.team))
    sock.send('{command}\n'.format(command=command))
  
  def get_pom_version(self, project='.'):
    with open('{}/target/maven-archiver/pom.properties'.format(project), 'r') as props:
      for line in props:
        if line.startswith('version='):
          return line.strip().split('=')[-1]

  def start(self):
    try:
      print 'Listening for {} commits'.format(self.team)
      for message in self.ps.listen():
        if message['type'] == 'message':
          team = message['data']
          if team == self.team:
            self.run += 1
            print team
            self.logs = open(WORKDIR+'/logs/{}.log'.format(team), 'w')
            successful_steps = 0
            for step in self.pipeline:
              if not step():
                break
              successful_steps += 1
    except:
      import traceback
      traceback.print_exc()

DeployScheduler('demo').start()
