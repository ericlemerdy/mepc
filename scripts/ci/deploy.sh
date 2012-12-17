#!/bin/bash
REPO_DIR=/var/repos/mepc
PROJECT_DIR=${REPO_DIR}/java

ENV_TYPE=$1

APP_ARTIFACT=mepc-server.tar.gz
FRONT_ARTIFACT=front.tar.gz

cd ${PROJECT_DIR}/target

APP_CONTAINER=`sudo lxc-list |grep "^  ${ENV_TYPE}app" |sed 's/^ *//g' |awk -F' ' '{ print $1 }'`
APP_DIR=/var/lib/lxc/${APP_CONTAINER}/rootfs/opt/mepc
sudo mkdir ${APP_DIR}
sudo tar xzf ${APP_ARTIFACT} -C ${APP_DIR}

FRONT_CONTAINER=`sudo lxc-list |grep "^  ${ENV_TYPE}front" |sed 's/^ *//g' |awk -F' ' '{ print $1 }'`
FRONT_DIR=/var/lib/lxc/${FRONT_CONTAINER}/rootfs/var/www
sudo tar xzf ${FRONT_ARTIFACT} -C ${FRONT_DIR}
