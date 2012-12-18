#!/bin/bash
ENVTYPE=$1

APP_VALID=KO
FRONT_VALID=KO
while [ "${APP_VALID}${FRONT_VALID}" != "OKOK" ]
do
	for host in app front
	do
		CONTAINER=`sudo lxc-list |grep "^  ${ENVTYPE}${host}" |sed 's/^ *//g' |awk -F' ' '{ print $1 }'`
		if [ -f /var/lib/lxc/${CONTAINER}/rootfs/tmp/deployed ]
		then
			if [ "${host}" == "app" ]
			then
				APP_VALID=OK
			else
				FRONT_VALID=OK
			fi
		else
			if [ "${host}" == "app" ]
			then
				APP_VALID=KO
			else
				FRONT_VALID=KO
			fi
		fi
	done
	sleep 5
done

if [ "_${ENVTYPE}" == "_int" ]
then
	WEBHOST=intcache
	ENVFILE=/var/repos/mepc/scripts/ci/int.env
else
	WEBHOST=cache
	cat /var/repos/mepc/scripts/varnish/${ENVTYPE} | telnet 10.0.3.30 6082
	ENVFILE=/var/repos/mepc/scripts/ci/prod.env
fi

cd /var/repos/mepc/java/mepc-functional-tests
foreman start -e ${ENVFILE} |grep FAILURE
if [ $? -eq 0 ]
then
	exit 1
else
	exit 0
fi
