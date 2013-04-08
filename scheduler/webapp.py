#!/usr/bin/env python
# -*- coding: utf-8 -*-
from dulwich.repo import Repo
import os
from flask import Flask, render_template, request, redirect, url_for, abort
from flask.ext.redis import Redis
import subprocess
import shutil

app = Flask(__name__)
redis = Redis(app)
redis.delete('teams')
redis.delete('servers')
redis.delete('dhcp')
redis.delete('roles')
redis.delete('envs')
redis.delete('steps')

for role in ('Application', 'SGBDR', 'Web', 'NoSQL'):
  redis.rpush('roles', role)
for env in ('Blue', 'Green'):
  redis.rpush('envs', env)

redis.hmset('steps', {'1': 'application'})
redis.hmset('steps', {'2': 'application'})
redis.hmset('steps', {'3': 'application:web'})
redis.hmset('steps', {'4': 'application:web:sgbdr'})
redis.hmset('steps', {'5': 'application:web:sgbdr:nosql'})
redis.hmset('steps', {'6': 'application:web:nosql'})

WORKDIR = '/var/lib/mepc'

def ensure_dir(path):
  if os.path.exists(path):
    shutil.rmtree(path)
  os.mkdir(path)
  os.chmod(path, 0777)

ensure_dir(WORKDIR+'/deploy')
ensure_dir(WORKDIR+'/logs')
ensure_dir(WORKDIR+'/repos')

demo_srv = {
'green_nosql_ip':         '10.3.100.2',
'green_nosql_port':       '9997',
'green_web_ip':           '10.3.100.1',
'green_web_port':         '9999',
'green_sgbdr_ip':         '10.3.100.1',
'green_sgbdr_port':       '9997',
'green_application_ip':   '10.3.100.2',
'green_application_port': '9999',
'blue_nosql_ip':          '10.3.100.2',
'blue_nosql_port':        '9996',
'blue_web_ip':            '10.3.100.1',
'blue_web_port':          '9998',
'blue_sgbdr_ip':          '10.3.100.1',
'blue_sgbdr_port':        '9996',
'blue_application_ip':    '10.3.100.2',
'blue_application_port':  '9998'
}

@app.route('/')
def index():
  try:
    teams = redis.hgetall('dhcp')
    if 'demo' not in teams.keys():
      init_team('demo')
      servers = redis.lrange('roles', 0, 99)
      envs = redis.lrange('envs', 0, 99)
      init_members(servers, envs, 'demo', demo_srv)
      teams = redis.hgetall('dhcp')
    teams_servers = {}
    for team in teams.keys():
      teams_servers[team] = {}
      servers = redis.hkeys(team)
      for server in servers:
        url = redis.hmget(team, server)
        env, role = server.split(':')
        if not teams_servers[team].has_key(env):
          teams_servers[team][env] = {}
        teams_servers[team][env][role] = url[0]
    return render_template('display_teams.html', title=u'Liste des équipes', teams=teams, srvs=teams_servers)
  except Exception as e:
    print e

def init_team(name):
  repo_dir = WORKDIR+'/repos/{}.git'.format(name)
  os.mkdir(repo_dir)
  Repo.init_bare(repo_dir)
  for r, d, f in os.walk(repo_dir):
    os.chmod(r, 0777)
  hook_name = '{dir}/hooks/post-receive'.format(dir=repo_dir)
  with open(hook_name, 'w') as hook_file:
    hook_file.write(render_template('files/post-receive.py', team=name))
  os.chmod(hook_name, 0755)
  deploy_dir = WORKDIR+'/deploy/{}'.format(name)
  os.mkdir(deploy_dir)
  os.chmod(deploy_dir, 0777)
  if name == 'demo':
    dhcp = '00'
  else:
    dhcp = '0{}'.format(max(map(lambda x: int(x), redis.hvals('dhcp')))+1)
  redis.hmset('dhcp', {name: dhcp})
  redis.hmset('teams', {name: 0})

@app.route('/team', methods=['GET', 'POST'])
def team():
  if request.method == 'POST':
    name = request.form['name']
    init_team(name)
    return redirect(url_for('members', team=name))
  else:
    return render_template('register_team.html', title=u'Enregistrez votre équipe')

def init_members(servers, envs, team, data):
  team_servers = {}
  for env in map(lambda x: x.lower(), envs):
    for server in map(lambda x: x.lower(), servers):
      ip = data['{env}_{server}_ip'.format(env=env, server=server)]
      ip = ip != '' and ip or '9999'
      port = data['{env}_{server}_port'.format(env=env, server=server)]
      team_servers['{env}:{server}'.format(team=team, env=env, server=server)] = 'http://{ip}:{port}'.format(ip=ip, port=port)
  redis.hmset(team, team_servers)

@app.route('/members', methods=['GET', 'POST'])
def members():
  servers = redis.lrange('roles', 0, 99)
  envs = redis.lrange('envs', 0, 99)
  if request.method == 'GET':
    team = request.args.get('team', None)
    if not team:
      abort(400)
    return render_template('register_members.html', title=u'Renseignez vos serveurs', servers=servers, envs=envs, team=team)
  else:
    team = request.form['team']
    init_members(servers, envs, team, request.form)
    return redirect(url_for('index'))

@app.route('/build')
def build():
  team = request.args.get('team', None)
  redis.publish('deploy', team)
  return '', 202

if __name__ == "__main__":
  app.run(debug=True)
