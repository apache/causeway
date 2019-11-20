#!/bin/bash
#set -x
#trap read debug
set -e

export BASELINE=2.0.0-M2
export GCPAPPENGINEREPO_URL=https://repo.incode.work
export SHARED_VARS_FILE=~/ci-env.txt

echo "REVISION=$BASELINE.$(date +%Y%m%d)-$(date +%H%M)-dryrun" > $SHARED_VARS_FILE 

## for the consumers to import shared vars (non secret!)
## source $SHARED_VARS_FILE && export $(cut -d= -f1 $SHARED_VARS_FILE)

export PROJECT_ROOT_PATH=$PWD
export CI_SCRIPTS_PATH=$PROJECT_ROOT_PATH/scripts/ci
export MVN_STAGES="install"

echo "=================  DRY RUN  =================="
echo "Shared Vars File: ${SHARED_VARS_FILE}"
cat $SHARED_VARS_FILE
echo "=============================================="

cd $PROJECT_ROOT_PATH

SECRETS_FILE=~/ci-secrets.txt
if [ -f "$SECRETS_FILE" ]; then
	  source $SECRETS_FILE
	  export $(cut -d= -f1 $SECRETS_FILE)
else
    echo "creating a template secrets file at your home: $SECRETS_FILE"
    printf 'DOCKER_REGISTRY_USERNAME=apacheisiscommitters\nDOCKER_REGISTRY_PASSWORD=\n' > $SECRETS_FILE
    exit 0
fi

bash $CI_SCRIPTS_PATH/build-core.sh
bash $CI_SCRIPTS_PATH/build-demo-app.sh dryrun
bash $CI_SCRIPTS_PATH/build-starter-apps.sh
