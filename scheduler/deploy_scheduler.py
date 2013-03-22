#!/usr/bin/env python
import redis

class DeployScheduler():
  def __init__(self):
    self.pipeline = ( self.package_app,
                      self.instantiate_infra,
                      self.functional_tests,
                      self.switch_env)
    self.redis = redis.Redis()
    teams = self.redis.lrange('teams', 0, 9999)
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
        teams_servers[team][env][role] = url[0]
    self.ps = redis.Redis().pubsub()
    self.ps.subscribe('deploy')

  def package_app(self, team):
    print 'package_app', team
    return True
  
  def instantiate_infra(self, team):
    print 'instantiate_infra', team
    return True
  
  def functional_tests(self, team):
    print 'functional_tests', team
    return True
  
  def switch_env(self, team):
    print 'switch env', team
    return True
  
  def start(self):
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
      print 'bye !'


if __name__ == '__main__':
  sched = DeployScheduler()
  sched.start()

