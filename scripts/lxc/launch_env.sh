#!/bin/bash
SUFFIX=`date +%Y%m%d%H%M%S`

ENVTYPE=$1
source ${ENVTYPE}_ips

for host in db legacydb app front
do
	PREFIX=${ENVTYPE}${host}
	CONTAINER=${PREFIX}${SUFFIX}
	sudo lxc-create -n ${CONTAINER} -t ubuntu-puppet --fssize 5G -- -r precise
	echo lxc.network.ipv4 = ${!host} |sudo tee -a /var/lib/lxc/${CONTAINER}/config
	while read line
	do
		echo ${line} |awk -F'=' '{ print $2 " " $1 }' |sudo tee -a /var/lib/lxc/${CONTAINER}/rootfs/etc/hosts
	done < ${ENVTYPE}_ips
	sudo lxc-start -d -n ${CONTAINER}
done
