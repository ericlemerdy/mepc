#!/bin/bash
git daemon --reuseaddr --base-path=/tmp/mepc --export-all --verbose --enable=receive-pack --detach
sudo PATH=venv/bin:$PATH ./deploy_scheduler.py
