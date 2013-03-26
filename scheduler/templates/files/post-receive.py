#!/usr/bin/env python
from httplib import HTTPConnection

conn = HTTPConnection('127.0.0.1:5000')
conn.request('GET', '/build?team={{team}}')
