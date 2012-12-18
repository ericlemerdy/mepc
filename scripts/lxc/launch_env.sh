#!/bin/bash
SUFFIX=`date +%Y%m%d%H%M%S`

ENVTYPE=$1
source /root/scripts/lxc/${ENVTYPE}_ips

echo sudo lxc-list \|grep -E "^  ${ENVTYPE}(db|legacydb|app|front)[0-9]{14}"
sudo lxc-list |grep -E "^  ${ENVTYPE}(db|legacydb|app|front)[0-9]{14}"
if [ $? -eq 0 ]
then
	echo Env ${PREFIX} already exists. Delete it before creating a new one.
	exit 1
fi

for host in app front
do
	PREFIX=${ENVTYPE}${host}
	CONTAINER=${PREFIX}${SUFFIX}
	sudo lxc-create -n ${CONTAINER} -t ubuntu-puppet --fssize 5G -- -r precise
	echo lxc.network.ipv4 = ${!host} |sudo tee -a /var/lib/lxc/${CONTAINER}/config
	while read line
	do
		echo ${line} |awk -F'=' '{ print $2 " " $1 }' |sudo tee -a /var/lib/lxc/${CONTAINER}/rootfs/etc/hosts
	done < /root/scripts/lxc/${ENVTYPE}_ips
	sudo lxc-start -d -n ${CONTAINER}
done
