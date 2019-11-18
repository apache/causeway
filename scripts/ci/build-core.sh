#!/bin/bash
set -e

# import shared vars (non secret!)
if [ -z "$SHARED_VARS_FILE" ]; then
  echo "\$SHARED_VARS_FILE not defined; skipping"
  exit 0
fi

. $SHARED_VARS_FILE
export $(cut -d= -f1 $SHARED_VARS_FILE)

sh $CI_SCRIPTS_PATH/print-environment.sh "build-core"

cd $PROJECT_ROOT_PATH/core

mvn -s $PROJECT_ROOT_PATH/.m2/settings.xml \
    --batch-mode \
    $MVN_STAGES \
    -Drevision=$REVISION \
    -Dskip.assemble-zip \
    $MVN_ADDITIONAL_OPTS

cd $PROJECT_ROOT_PATH

