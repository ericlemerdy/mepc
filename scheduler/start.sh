#!/bin/bash
`dirname $0`/venv/bin/uwsgi --http :9090 --virtualenv venv --wsgi-file webapp.py --callable app --enable-threads
