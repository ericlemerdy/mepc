#!/usr/bin/env python
# -*- coding: utf-8 -*-
from dulwich.repo import Repo
import os
from flask import Flask, render_template, request, redirect, url_for, abort
from flask.ext.redis import Redis

app = Flask(__name__)
redis = Redis(app)
redis.delete('teams')
redis.delete('servers')

@app.route('/')
def index():
  return render_template('register_team.html', title=u'Enregistrez votre équipe')

@app.route('/team', methods=['GET', 'POST'])
def team():
  if request.method == 'POST':
    name = request.form['name']
    repo_dir = '/tmp/{}.git'.format(name)
    os.mkdir(repo_dir)
    Repo.init_bare(repo_dir)
    filename = '{dir}/hooks/post-receive'.format(dir=repo_dir)
    with open(filename, 'w') as hook:
      hook.write(render_template('files/post-receive.py', team=name))
    os.chmod(filename, 0755)
    redis.lpush('teams', name)
    return redirect(url_for('members', team=name))
  else:
    teams = redis.lrange('teams', 0, 9999)
    teams.sort()
    teams_servers = {}
    for team in teams:
      teams_servers[team] = {}
      servers = redis.hkeys(team)
      for server in servers:
        url = redis.hmget(team, server)
        env, role = server.split(':')
        if not teams_servers[team].has_key(env):
          teams_servers[team][env] = {}
        teams_servers[team][env][role] = url[0]
    print teams_servers
    return render_template('display_teams.html', title=u'Liste des équipes', teams=teams, srvs=teams_servers)

@app.route('/members', methods=['GET', 'POST'])
def members():
  servers = ('Application', 'SGBDR', 'Web', 'NoSQL')
  envs = ('Blue', 'Green')
  if request.method == 'GET':
    team = request.args.get('team', None)
    if not team:
      abort(400)
    return render_template('register_members.html', title=u'Renseignez vos serveurs', servers=servers, envs=envs, team=team)
  else:
    team = request.form['team']
    team_servers = {}
    for env in map(lambda x: x.lower(), envs):
      for server in map(lambda x: x.lower(), servers):
        ip = request.form['{env}_{server}_ip'.format(env=env, server=server)]
        ip = ip != '' and ip or '9999'
        port = request.form['{env}_{server}_port'.format(env=env, server=server)]
        team_servers['{env}:{server}'.format(team=team, env=env, server=server)] = 'http://{ip}:{port}'.format(ip=ip, port=port)
    redis.hmset(team, team_servers)
    return redirect(url_for('team'))

@app.route('/build')
def build():
  team = request.args.get('team', None)
  redis.publish('deploy', team)
  return '', 202

if __name__ == "__main__":
  app.run(debug=True)
