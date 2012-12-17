#!/bin/bash
PREFIX=$1

CONTAINER=`sudo lxc-list |grep "^  ${PREFIX}" |sed 's/^ *//g' |awk -F' ' '{ print $1 }'`

sudo lxc-console -n ${CONTAINER}
