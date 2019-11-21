#!/bin/bash
set -e

SCRIPT_DIR=$( dirname "$0" )
if [ -z "$PROJECT_ROOT_PATH" ]; then
  PROJECT_ROOT_PATH=`cd $SCRIPT_DIR/../.. ; pwd`
fi

if [ -z "$REVISION" ]; then
  if [ ! -z "$SHARED_VARS_FILE" ] && [ -f "$SHARED_VARS_FILE" ]; then
    . $SHARED_VARS_FILE
    export $(cut -d= -f1 $SHARED_VARS_FILE)
  fi
fi
if [ -z "$MVN_STAGES" ]; then
  MVN_STAGES="clean install"
fi
if [ -z "$SETTINGS_XML" ]; then
  SETTINGS_XML=$PROJECT_ROOT_PATH/.m2/settings.xml
fi

sh $SCRIPT_DIR/print-environment.sh "build-core"

cd $PROJECT_ROOT_PATH/core-parent

if [ ! -z "$REVISION" ]; then
  mvn versions:set -DnewVersion=$REVISION
fi

cat pom.xml

mvn -s $SETTINGS_XML \
    --batch-mode \
    $MVN_STAGES \
    $MVN_ADDITIONAL_OPTS \
    $*

if [ ! -z "$REVISION" ]; then
  mvn versions:revert
fi

cd $PROJECT_ROOT_PATH
