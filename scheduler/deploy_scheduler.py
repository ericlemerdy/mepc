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
  envs = ('blue', 'green')
  def __init__(self):
    self.pipeline = ( self.package_app,
                      self.instantiate_infra,
                      self.functional_tests,
                      self.switch_env)
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

  def package_app(self, team):
    tmp_dir = tempfile.mkdtemp(prefix=team)
    os.chdir(tmp_dir)
    status = subprocess.call(['git', 'clone', '/tmp/mepc/{}.git'.format(team)])
    if status != 0:
      return False
    os.chdir(team)
    status = subprocess.call(['mvn', 'clean', 'install'])
    if status != 0:
      return False
    os.chdir('/tmp')
    shutil.rmtree(tmp_dir)
    return True
  
  def instantiate_infra(self, team):
    target_env = self.envs[int(self.redis.hget('teams', team)) % 2]
    servers = self.teams_servers[team][target_env]
    for role, url in servers.items():
      fqdn = '{env}-{role}'.format(env=target_env, role=role)
      vagrantfile = self.template.render(fqdn=fqdn)
      print 'Launching {role} ({fqdn}) on {url}'.format(role=role, fqdn=fqdn, url=url)
      resp = urllib.urlopen(url, urllib.urlencode({'config': vagrantfile, 'action': 'up'}))
      if resp.getcode() > 399:
        print resp.getcode()
        return False
    return True
  
  def functional_tests(self, team):
    print 'functional_tests', team
    return True
  
  def switch_env(self, team):
    new_env = self.envs[int(self.redis.hget('teams', team)) % 2]
    old_env = self.envs[(int(self.redis.hget('teams', team))+1) % 2]
    for backend in [tmp+'-back' for tmp in ('nosql', 'rdbms', 'app', 'web')]:
      self.send_haproxy_cmd('set weight {backend}/{env} 1'.format(backend=backend, env=new_env))
    for backend in [tmp+'-back' for tmp in ('web', 'app', 'rdbms', 'nosql')]:
      self.send_haproxy_cmd('set weight {backend}/{env} 0'.format(backend=backend, env=iold_env))

  def send_haproxy_cmd(self, team, command):
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

