#!/bin/bash
set -e

sh $CI_SCRIPTS_PATH/print-environment.sh

cd $PROJECT_ROOT_PATH/core

mvn -s $PROJECT_ROOT_PATH/.m2/settings.xml \
    --batch-mode \
    $MVN_STAGES \
    -Dgcpappenginerepo-deploy \
    -Dgcpappenginerepo-deploy.repositoryUrl=$GCPAPPENGINEREPO_URL \
    -Drevision=$REVISION \
    -Dskip.assemble-zip \
    $CORE_ADDITIONAL_OPTS

cd $PROJECT_ROOT_PATH

