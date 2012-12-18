#!/bin/bash
REPO_DIR=/var/repos/mepc

ENV_TYPE=$1

cd ${REPO_DIR}/java/mepc-server/target
APP_CONTAINER=`sudo lxc-list |grep "^  ${ENV_TYPE}app" |sed 's/^ *//g' |awk -F' ' '{ print $1 }'`
APP_DIR=/var/lib/lxc/${APP_CONTAINER}/rootfs/tmp
APP_ARTIFACT=`ls mepc-server-*.tar.gz`
sudo cp ${APP_ARTIFACT} ${APP_DIR}/mepc.tar.gz

cd ${REPO_DIR}
FRONT_CONTAINER=`sudo lxc-list |grep "^  ${ENV_TYPE}front" |sed 's/^ *//g' |awk -F' ' '{ print $1 }'`
FRONT_DIR=/var/lib/lxc/${FRONT_CONTAINER}/rootfs/tmp
FRONT_ARTIFACT=front.tar.gz
sudo cp ${FRONT_ARTIFACT} ${FRONT_DIR}/front.tar.gz
