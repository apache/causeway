#!/bin/bash
set -e

# import shared vars (non secret!)
if [ ! -z "$SHARED_VARS_FILE" ] && [ -f "$SHARED_VARS_FILE" ]; then
  . $SHARED_VARS_FILE
  export $(cut -d= -f1 $SHARED_VARS_FILE)
fi

sh $CI_SCRIPTS_PATH/print-environment.sh "build-example-apps"

cd $PROJECT_ROOT_PATH/examples

mvn --batch-mode \
    -Dexample-apps \
    clean install \
    -Drevision=$REVISION

cd $PROJECT_ROOT_DIR

