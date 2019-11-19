#!/bin/bash
set -e

# import shared vars (non secret!)
if [ ! -z "$SHARED_VARS_FILE" ] && [ -f "$SHARED_VARS_FILE" ]; then
  . $SHARED_VARS_FILE
  export $(cut -d= -f1 $SHARED_VARS_FILE)
fi

sh $CI_SCRIPTS_PATH/print-environment.sh "build-mixins"

cd $PROJECT_ROOT_PATH/mixins

# temporarily set version
mvn versions:set -DnewVersion=$REVISION

mvn -s $PROJECT_ROOT_PATH/.m2/settings.xml \
    --batch-mode \
    $MVN_STAGES \
    -Dskip.mavenmixin-standard \
    -Dskip.mavenmixin-surefire \
    -Dskip.mavenmixin-datanucleus-enhance \
    $MVN_ADDITIONAL_OPTS

# revert the edits from earlier ...
mvn versions:revert

cd $PROJECT_ROOT_PATH


