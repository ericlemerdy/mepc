#!/usr/bin/env python
import redis
import os
from jinja2 import Template

WORKDIR = '/var/lib/mepc'

class DnsConfig():
  def __init__(self, teams):
    redis_conn = redis.Redis()
    dhcp_prefixes = redis_conn.hgetall('dhcp')
    self.dnscfg_name = WORKDIR+'/db.mepc.lan'
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
      dnscfg_name = WORKDIR+'/db.mepc.lan'
      with open(dnscfg_name, 'w') as dnscfg_file:
        dnscfg_file.write(dns_tpl.render(servers=servers, teams=teams))
  
  def start(self):
    print 'Starting named'
    os.execv('/usr/sbin/named', ('/usr/sbin/named', '-f'))


def start_service(service, is_global, arg):
  service_inst = service(arg)
  return service_inst.start()

conn = redis.Redis()
teams = conn.hkeys('teams')
DnsConfig(teams).start()
