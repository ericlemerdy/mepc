#!/usr/bin/env python
import redis
import os
import tempfile
import subprocess
import shutil
import urllib
import socket
from jinja2 import Template

class DeployScheduler():
  def __init__(self):
    self.pipeline = ( self.package_app,
                      self.instantiate_infra,
                      self.functional_tests,
                      self.switch_env,
                      self.shutdown_infra)
    self.redis = redis.Redis()
    teams = self.redis.hkeys('teams')
    teams.sort()
    self.teams_servers = {}
    for team in teams:
      self.teams_servers[team] = {}
      servers = self.redis.hkeys(team)
      for server in servers:
        url = self.redis.hmget(team, server)
        env, role = server.split(':')
        if not self.teams_servers[team].has_key(env):
          self.teams_servers[team][env] = {}
        self.teams_servers[team][env][role] = url[0]
    self.ps = redis.Redis().pubsub()
    self.ps.subscribe('deploy')
    with open('templates/files/Vagrantfile', 'r') as conf_file:
      tpl_content = conf_file.read()
      self.template = Template(tpl_content)
    self.envs = map(lambda x: x.lower(), self.redis.lrange('envs', 0, 99))
    self.roles = map(lambda x: x.lower(), self.redis.lrange('roles', 0, 99))
    self.steps = {}
    for key, value in self.redis.hgetall('steps').items():
      self.steps[key] = value.split(':')

  def package_app(self, team):
    tmp_dir = tempfile.mkdtemp(prefix=team)
    os.chdir(tmp_dir)
    status = subprocess.call(['git', 'clone', '/tmp/mepc/{}.git'.format(team)])
    if status != 0:
      return False
    os.chdir('{}/java'.format(team))
    status = subprocess.call(['mvn', 'clean', 'install'])
    if status != 0:
      return False
    os.chdir('/tmp')
    shutil.rmtree(tmp_dir)
    return True
  
  def instantiate_infra(self, team):
    env_idx = int(self.redis.hget('teams', team)) % 2
    target_env = self.envs[env_idx]
    servers = self.teams_servers[team][target_env]
    dhcp_prefix = self.redis.hget('dhcp', team) + str(env_idx)
    for role, url in servers.items():
      role_idx = str(map(lambda x: x.lower(), self.roles).index(role))
      fqdn = '{env}-{role}'.format(env=target_env, role=role)
      vagrantfile = self.template.render(fqdn=fqdn, mac=dhcp_prefix+role_idx)
      print 'Launching {role} ({fqdn}) on {url}'.format(role=role, fqdn=fqdn, url=url)
      resp = urllib.urlopen(url, urllib.urlencode({'config': vagrantfile, 'action': 'up'}))
      if resp.getcode() > 399:
        print resp.getcode()
        self.destroy_env(team, target_env)
        return False
    return True
  
  def functional_tests(self, team):
    env_idx = int(self.redis.hget('teams', team)) % 2
    target_env = self.envs[env_idx]
    tmp_dir = tempfile.mkdtemp(prefix=team)
    os.chdir(tmp_dir)
    status = subprocess.call(['git', 'clone', '/tmp/mepc/{}.git'.format(team)])
    if status != 0:
      return False
    os.chdir('{}/java'.format(team))
    status = subprocess.call(['mvn', 'clean', 'install', ' -Dfr.valtech.appHost='])
    if status != 0:
      return False
    os.chdir('/tmp')
    shutil.rmtree(tmp_dir)
    return True
  
  def switch_env(self, team):
    new_env = self.envs[int(self.redis.hget('teams', team)) % 2]
    old_env = self.envs[(int(self.redis.hget('teams', team))+1) % 2]
    for backend in [tmp+'-back' for tmp in ('nosql', 'rdbms', 'app', 'web')]:
      self.send_haproxy_cmd(team, 'set weight {backend}/{env} 1'.format(backend=backend, env=new_env))
    for backend in [tmp+'-back' for tmp in ('web', 'app', 'rdbms', 'nosql')]:
      self.send_haproxy_cmd(team, 'set weight {backend}/{env} 0'.format(backend=backend, env=old_env))
    self.redis.hincrby('teams', team, 1)
    return True

  def shutdown_infra(self, team):
    old_env = self.envs[(int(self.redis.hget('teams', team))) % 2]
    self.destroy_env(team, old_env)
    return True
    
  def destroy_env(self, team, env):
    servers = self.teams_servers[team][env]
    for role, url in servers.items():
      print 'Destroying {role} on {url}'.format(role=role, url=url)
      resp = urllib.urlopen(url, urllib.urlencode({'action': 'destroy -f'}))
      if resp.getcode() > 399:
        print resp.getcode()
        return False
    return True

  def send_haproxy_cmd(self, team, command):
    print team ,'-', command
    sock = socket.socket(socket.AF_UNIX)
    sock.connect('/tmp/mepc/{team}.sock'.format(team=team))
    sock.send('{command}\n'.format(command=command))
  
  def start(self):
    print 'Waiting for some commits to deploy :)'
    print 'Press Ctrl-C to stop...'
    try:
      for message in self.ps.listen():
        if message['type'] == 'message':
          team = message['data']
          successful_steps = 0
          for step in self.pipeline:
            if not step(team):
              break
            successful_steps += 1
    except KeyboardInterrupt:
      print '\b\bbye !'


if __name__ == '__main__':
  sched = DeployScheduler()
  sched.start()

