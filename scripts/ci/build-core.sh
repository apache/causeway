#!/bin/bash
set -e

if [ -z "$BATCH_MODE_FLAG" ] || [ "$BATCH_MODE_FLAG" != "off" ]; then
  BATCH_MODE=--batch-mode
fi

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

if [ ! -z "$REVISION" ]; then

  cd $PROJECT_ROOT_PATH/core-parent
  echo "updating version in isis-parent ..."
  mvn versions:set -DnewVersion=$REVISION

  cd $PROJECT_ROOT_PATH/starters
  echo "updating version in isis-app-starter-parent ..."
  CURR=$(grep "<version>" pom.xml | head -1 | cut -d'>' -f2 | cut -d'<' -f1)
  sed -i "s|<version>$CURR</version>|<version>$REVISION</version>|g" pom.xml
fi

cd $PROJECT_ROOT_PATH/core-parent
mvn -s $SETTINGS_XML \
    $BATCH_MODE \
    $MVN_STAGES \
    $MVN_ADDITIONAL_OPTS \
    $*

if [ ! -z "$REVISION" ]; then
  cd $PROJECT_ROOT_PATH/core-parent
  echo "reverting version in isis-parent ..."
  mvn versions:revert
  cd $PROJECT_ROOT_PATH/starters
  echo "reverting version in isis-app-starter-parent ..."
  sed -i "s|<version>$REVISION</version>|<version>$CURR</version>|g" pom.xml
fi

cd $PROJECT_ROOT_PATH
