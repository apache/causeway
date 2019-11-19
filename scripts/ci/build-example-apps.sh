#!/bin/bash
set -e

if [ -z "$REVISION" ]; then
  if [ ! -z "$SHARED_VARS_FILE" ] && [ -f "$SHARED_VARS_FILE" ]; then
    . $SHARED_VARS_FILE
    export $(cut -d= -f1 $SHARED_VARS_FILE)
  fi
fi
if [ -z "$REVISION" ]; then
  echo "\$REVISION is not set" >&2
  exit 1
fi

sh $CI_SCRIPTS_PATH/print-environment.sh "build-example-apps"

cd $PROJECT_ROOT_PATH/examples

mvn versions:set -DnewVersion=$REVISION

mvn --batch-mode \
    -Dexample-apps \
    clean install

mvn versions:revert

cd $PROJECT_ROOT_PATH
