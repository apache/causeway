#!/bin/bash
#set -x
#trap read debug
set -e

export BASELINE=2.0.0-M2
export DOCKER_HOST=tcp://docker:2375
export DOCKER_DRIVER=overlay2
export ORG_NAME="apacheisis"
export GCPAPPENGINEREPO_URL=https://repo.incode.work

export REVISION=$BASELINE.$(date +%Y%m%d)

export PROJECT_ROOT_DIR=$PWD
export CI_SCRIPTS_DIR=$PROJECT_ROOT_DIR/scripts/ci
export CI_DRY_RUN=true
export MVN_STAGES="install"

echo "=================  DRY RUN  =================="
echo "\$REVISION             = ${REVISION}"
echo "\$CI_BUILDS_DIR        = ${CI_BUILDS_DIR}"
echo "=============================================="

cd $PROJECT_ROOT_DIR

#sh $CI_SCRIPTS_DIR/build-mixins.sh
#sh $CI_SCRIPTS_DIR/build-core.sh
#sh $CI_SCRIPTS_DIR/build-example-apps.sh
sh $CI_SCRIPTS_DIR/build-demo-app.sh
