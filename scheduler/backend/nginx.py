#!/usr/bin/env python
import redis
import os
from jinja2 import Template

WORKDIR = '/var/lib/mepc'

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
      self.nginxcfg_name = WORKDIR+'/nginx.cfg'
      with open(self.nginxcfg_name, 'w') as nginxcfg_file:
        nginxcfg_file.write(nginx_tpl.render(teams=ports))
      
  def start(self):
    print 'Starting nginx'
    os.chmod(self.nginxcfg_name, 0644)
    os.execv('/usr/sbin/nginx', ('/usr/sbin/nginx', '-c', self.nginxcfg_name))

conn = redis.Redis()
teams = conn.hkeys('teams')
NginxConfig(teams).start()
