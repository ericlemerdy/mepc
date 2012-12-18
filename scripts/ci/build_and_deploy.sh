#!/bin/bash
REPO_DIR=/var/repos/mepc
PROJECT_DIR=${REPO_DIR}/java

if [[ -f /tmp/build_and_deploy ]]
then
	exit 0
fi

sudo touch /tmp/build_and_deploy

cd ${REPO_DIR}
UPDATE=`/usr/bin/git fetch --dry-run 2>&1 |/usr/bin/wc -l`
#if [ true ]
if [ $UPDATE -ne 0 ]
then
	/usr/bin/git pull --rebase
	cd ${REPO_DIR}/scripts
	sudo ./package_front.sh
	cd ${PROJECT_DIR}
	/usr/bin/mvn clean install
	if [ $? -eq 0 ]
	then
		/var/repos/mepc/scripts/lxc/launch_env.sh int
		/var/repos/mepc/scripts/ci/deploy.sh int
		/var/repos/mepc/scripts/ci/validate.sh int
		if [ $? -eq 0 ]
		then
			/var/repos/mepc/scripts/lxc/shutdown_env.sh int
			/var/repos/mepc/scripts/lxc/destroy_env.sh int
			EXISTING_PROD=`sudo lxc-list |grep -E '^  (blue|green)app' |sed 's/^ *\(.*\)app.*/\1/'`
			if [ "_${EXISTING_PROD}" == "_blue" ]
			then
				TARGET_PROD=green
			else
				TARGET_PROD=blue
			fi
			/var/repos/mepc/scripts/lxc/launch_env.sh ${TARGET_PROD}
			/var/repos/mepc/scripts/ci/deploy.sh ${TARGET_PROD}
			/var/repos/mepc/scripts/ci/validate.sh ${TARGET_PROD}
			if [ $? -eq 0 ]
			then
				/var/repos/mepc/scripts/lxc/shutdown_env.sh ${EXISTING_PROD}
				/var/repos/mepc/scripts/lxc/destroy_env.sh ${EXISTING_PROD}
			else
				cat /var/repos/mepc/scripts/varnish/${EXISTING_PROD} | telnet 10.0.3.30 6082
			fi
		else
			echo Invalid deployment.
		fi
	else
		echo Build failed.
	fi
else
	echo No change found.
fi
sudo rm /tmp/build_and_deploy
