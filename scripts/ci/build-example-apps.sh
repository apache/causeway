#!/bin/bash
set -e

sh $CI_SCRIPTS_PATH/print-environment.sh "build-example-apps"

cd $PROJECT_ROOT_PATH/examples

mvn --batch-mode \
    -Dexample-apps \
    clean install \
    -Drevision=$REVISION

cd $PROJECT_ROOT_DIR

