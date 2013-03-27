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

class NginxConfig():
  def __init__(self, teams):
    self.teams = teams
    redis_conn = redis.Redis()
    ports = {}
    for team in teams:
      dhcp_prefix = redis_conn.hget('dhcp', team)
      ports[team] = '80{}'.format(dhcp_prefix)
    with open('templates/files/nginx.cfg', 'r') as conf_file:
      tpl_content = conf_file.read()
      nginx_tpl = Template(tpl_content)
      self.nginxcfg_name = '/tmp/mepc/nginx.cfg'
      with open(self.nginxcfg_name, 'w') as nginxcfg_file:
        nginxcfg_file.write(nginx_tpl.render(teams=ports))
      
  def start(self):
    print 'Starting nginx'
    subprocess.call(['/usr/sbin/nginx', '-c', self.nginxcfg_name])
    print 'Stopping nginx'

class DhcpConfig():
  def __init__(self, teams):
    self.teams = teams
    redis_conn = redis.Redis()
    dhcp_prefixes = redis_conn.hgetall('dhcp')
    self.dhcpcfg_name = '/tmp/mepc/dhcp.cfg'
    with open(self.dhcpcfg_name, 'w') as dhcpcfg_file:
      dhcpcfg_file.write('\n'.join(
        ( 'default-lease-time 120;',
          'max-lease-time 600;',
          'option subnet-mask 255.255.0.0;',
          'option domain-name-servers 10.3.0.30;',
          'option domain-name "mepc.lan";',
          'subnet 10.3.0.0 netmask 255.255.0.0 {',
          '  range 10.3.100.1 10.3.100.254;',
          '}')))
      for team, prefix in dhcp_prefixes.items():
        for env in ('blue', 'green'):
          for host_id, host in enumerate(('web', 'app', 'db', 'nosql')):
            dhcpcfg_file.write('\n'.join(
              ( 'host {host} {{',
                '  hardware ethernet 02:00:00:00:{prefix}:{host_id:0>2};',
                '  fixed-address 10.3.{prefix}.{host_id};',
                '}}')).format(host='{team}-{env}-{host}'.format(team=team, env=env, host=host), prefix=prefix, host_id=host_id+1))
  
  def start(self):
    print 'Starting isc-dhcp'
    subprocess.call(['/usr/sbin/dhcpd', '-f', '-cf', '/tmp/mepc/dhcp.cfg', 'eth0'])
    print 'Stopping isc-dhcp'
    
class DnsConfig():
  def __init__(self, teams):
    redis_conn = redis.Redis()
    dhcp_prefixes = redis_conn.hgetall('dhcp')
    self.dnscfg_name = '/tmp/mepc/db.mepc.lan'
    servers = {}
    roles = map(lambda x: x.lower(), redis_conn.lrange('roles', 0, 99))
    for team, prefix in dhcp_prefixes.items():
      if team == 'demo':
        prefix = '00'
      for env in ('blue', 'green'):
        for host_id, host in enumerate(roles):
          servers['{team}-{env}-{host}'.format(team=team, env=env, host=host)] = '10.3.{prefix}.{host_id}'.format(prefix=int(prefix), host_id=host_id+1)
      servers['{team}-puppet'.format(team=team)] = '10.3.{prefix}.9'.format(prefix=int(prefix))
    with open('templates/files/dns.zone', 'r') as conf_file:
      tpl_content = conf_file.read()
      dns_tpl = Template(tpl_content)
      dnscfg_name = '/tmp/mepc/db.mepc.lan'
      with open(dnscfg_name, 'w') as dnscfg_file:
        dnscfg_file.write(dns_tpl.render(servers=servers, teams=teams))
  
  def start(self):
    print 'Starting named'
    subprocess.call(['/usr/sbin/named', '-f'])
    print 'Stopping named'
    

class HaproxyTeam():
  def __init__(self, team):
    self.team = team
    redis_conn = redis.Redis()
    self.dhcp_prefix = redis_conn.hget('dhcp', self.team)
    with open('templates/files/haproxy.cfg', 'r') as conf_file:
      tpl_content = conf_file.read()
      ha_tpl = Template(tpl_content)
      self.hacfg_name = '/tmp/mepc/{}.cfg'.format(team)
      with open(self.hacfg_name, 'w') as hacfg_file:
        hacfg_file.write(ha_tpl.render(team=team, team_idx=self.dhcp_prefix))
    os.chmod(self.hacfg_name, 0644)

  def start(self):
    print 'Starting {} haproxy'.format(self.team)
    subprocess.call(['/usr/sbin/haproxy', '-f', self.hacfg_name])
    print 'Stopping {} haproxy'.format(team)

class DeployScheduler():
  def __init__(self, name):
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
      status = subprocess.call(['git', 'clone', '/tmp/mepc/{}.git'.format(self.team)], stderr=self.logs, stdout=self.logs)
      if status != 0:
        return False
      os.chdir('{}'.format(self.team))
      status = subprocess.call(['mvn', 'clean', 'install', '-DskipTests=true'], stderr=self.logs, stdout=self.logs)
      if status != 0:
        return False
      self.version = self.get_pom_version('mepc-server')
      shutil.copy('mepc-server/target/mepc-server-{}.war'.format(self.version), '/home/pchaussalet/projects/mepc/filer/deploy/{}'.format(self.team))
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
      status = subprocess.call(['git', 'clone', '/tmp/mepc/{}.git'.format(self.team)])
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
    sock.connect('/tmp/mepc/{team}.sock'.format(team=self.team))
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
            self.logs = open('/tmp/mepc/logs/{}.log'.format(team), 'w')
            successful_steps = 0
            for step in self.pipeline:
              if not step():
                break
              successful_steps += 1
    except:
      import traceback
      traceback.print_exc()

def start_service(service, is_global, arg):
  service_inst = service(arg)
  proc = Process(target=service_inst.start)
  if is_global:
    team = 'global'
  else:
    team = arg
  processes[team].append(proc)
  proc.start()

if __name__ == '__main__':
  conn = redis.Redis()
  teams = conn.hkeys('teams')
  processes = {'global': []}
  if not os.path.exists('/tmp/mepc/logs'):
    os.mkdir('/tmp/mepc/logs')
  for team in teams:
    processes[team] = []
    for service in (HaproxyTeam, DeployScheduler):
      start_service(service, False, team)

  start_service(NginxConfig, True, teams)
  start_service(DhcpConfig, True, teams)
  start_service(DnsConfig, True, teams)
  try:
    print 'Press Ctrl-C to stop...'
    while len(processes) > 0:
      time.sleep(30)
  except KeyboardInterrupt:
    for process in processes.values():
      map(lambda x: x.terminate(), process)
