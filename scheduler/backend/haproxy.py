#!/usr/bin/env python
import redis
import os
from jinja2 import Template

WORKDIR = '/var/lib/mepc'

class HaproxyTeam():
  def __init__(self, team):
    self.team = team
    redis_conn = redis.Redis()
    self.dhcp_prefix = redis_conn.hget('dhcp', self.team)
    with open('templates/files/haproxy.cfg', 'r') as conf_file:
      tpl_content = conf_file.read()
      ha_tpl = Template(tpl_content)
      self.hacfg_name = WORKDIR+'/{}.cfg'.format(team)
      with open(self.hacfg_name, 'w') as hacfg_file:
        hacfg_file.write(ha_tpl.render(team=team, team_idx=self.dhcp_prefix))
    os.chmod(self.hacfg_name, 0644)

  def start(self):
    print 'Starting {} haproxy'.format(self.team)
    os.execv('/usr/sbin/haproxy', ('/usr/sbin/haproxy', '-f', self.hacfg_name))

HaproxyTeam('demo').start()
