#!/bin/bash
#set -x
#trap read debug
set -e

export BASELINE=2.0.0-M2
export GCPAPPENGINEREPO_URL=https://repo.incode.work

export REVISION=$BASELINE.$(date +%Y%m%d)

export PROJECT_ROOT_PATH=$PWD
export CI_SCRIPTS_PATH=$PROJECT_ROOT_PATH/scripts/ci
export MVN_STAGES="install"

SECRETS_FILE=~/ci-secrets.txt

echo "=================  DRY RUN  =================="
echo "\$REVISION             = ${REVISION}"
echo "\$CI_BUILDS_DIR        = ${CI_BUILDS_DIR}"
echo "=============================================="

cd $PROJECT_ROOT_PATH

if [ -f "$SECRETS_FILE" ]; then
	source $SECRETS_FILE
	export $(cut -d= -f1 $SECRETS_FILE)
else
    echo "creating a template secrets file at your home: $SECRETS_FILE"
    printf 'DOCKER_REGISTRY_USERNAME=apacheisiscommitters\nDOCKER_REGISTRY_PASSWORD=\n' > $SECRETS_FILE
    exit 0
fi

sh $CI_SCRIPTS_PATH/build-mixins.sh
sh $CI_SCRIPTS_PATH/build-core.sh
sh $CI_SCRIPTS_PATH/build-demo-app.sh dryrun
sh $CI_SCRIPTS_PATH/build-example-apps.sh
