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

sh $SCRIPT_DIR/print-environment.sh "build-smoketests"

#
# update version (but just for the modules we need to build)
#
if [ ! -z "$REVISION" ]; then
  cd $PROJECT_ROOT_PATH/core-parent
  mvn versions:set -DnewVersion=$REVISION -Ddemo-smoketests
  cd $PROJECT_ROOT_PATH
fi

#
# now build the apps
#
for app in smoketests
do
  cd $PROJECT_ROOT_PATH/examples/$app

  mvn --batch-mode \
      clean install

  cd $PROJECT_ROOT_PATH
done

#
# finally, revert the version
#
if [ ! -z "$REVISION" ]; then
  cd $PROJECT_ROOT_PATH/core-parent
  mvn versions:revert -Dstarter-smoketests
  cd $PROJECT_ROOT_PATH
fi
