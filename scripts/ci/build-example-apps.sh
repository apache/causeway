#!/bin/bash
set -e

# import shared vars (non secret!)
if [ -z "$SHARED_VARS_FILE" ]; then
  echo "\$SHARED_VARS_FILE not defined; skipping"
  exit 0
fi

. $SHARED_VARS_FILE
export $(cut -d= -f1 $SHARED_VARS_FILE)

sh $CI_SCRIPTS_PATH/print-environment.sh "build-example-apps"

cd $PROJECT_ROOT_PATH/examples

mvn --batch-mode \
    -Dexample-apps \
    clean install \
    -Drevision=$REVISION

cd $PROJECT_ROOT_DIR

