#!/usr/bin/env python
import redis
import os

WORKDIR = '/var/lib/mepc'

class DhcpConfig():
  def __init__(self, teams):
    self.teams = teams
    redis_conn = redis.Redis()
    dhcp_prefixes = redis_conn.hgetall('dhcp')
    self.dhcpcfg_name = '/etc/dhcp/dhcpd.conf'
    with open(self.dhcpcfg_name, 'w') as dhcpcfg_file:
      dhcpcfg_file.write('\n'.join(
        ( 'default-lease-time 120;',
          'max-lease-time 600;',
          'option subnet-mask 255.255.0.0;',
          'option domain-name-servers 10.3.0.20;',
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
    os.chmod(self.dhcpcfg_name, 0666)
    os.execv('/usr/sbin/dhcpd', ('/usr/sbin/dhcpd', '-q', '-f', '-pf', '/var/run/dhcp-server/dhcpd.pid', 'eth1'))

conn = redis.Redis()
teams = conn.hkeys('teams')
DhcpConfig(teams).start()

