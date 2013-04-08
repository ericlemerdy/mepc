#!/usr/bin/env python
from httplib import HTTPConnection

conn = HTTPConnection('10.3.0.20')
conn.request('GET', '/build?team={{team}}')
