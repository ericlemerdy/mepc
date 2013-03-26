#!/bin/bash
sudo PATH=venv/bin:$PATH ./webapp.py
#`dirname $0`/venv/bin/uwsgi --http :9090 --virtualenv venv --wsgi-file webapp.py --callable app --enable-threads --process 4 --threads 2
