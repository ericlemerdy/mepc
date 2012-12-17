#!/bin/bash
REPO_DIR=/var/repos/mepc
PROJECT_DIR=${REPO_DIR}/java

cd ${REPO_DIR}
UPDATE=`/usr/bin/git fetch --dry-run 2>&1 |/usr/bin/wc -l`
if [ $UPDATE -ne 0 ]
then
	/usr/bin/git pull --rebase
	cd ${PROJECT_DIR}
	/usr/bin/mvn clean install
	if [ $? -eq 0 ]
	then
		/var/repos/mepc/scripts/launch_env.sh int
		/var/repos/mepc/scripts/deploy.sh int
		/var/repos/mepc/scripts/validate.sh int
	fi
fi
