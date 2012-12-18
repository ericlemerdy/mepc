#!/bin/bash

ENVTYPE=$1

for host in front app
do
	PREFIX=${ENVTYPE}${host}
	CONTAINER=`sudo lxc-list |grep "^  ${PREFIX}" |sed 's/^ *//g' |awk -F' ' '{ print $1 }'`
	sudo lxc-destroy -f -n ${CONTAINER}
done
