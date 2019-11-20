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

sh $SCRIPT_DIR/print-environment.sh "build-starter-apps"

#
# update version (but just for the modules we need to build)
#
cd $PROJECT_ROOT_PATH/core-parent

if [ -z "$REVISION" ]; then
  REVISION=$(grep "<version>" pom.xml | head -1 | cut -d">" -f2 | cut -d"<" -f1)
fi

mvn versions:set -DnewVersion=$REVISION -Drevision=$REVISION -Dstarter-apps-modules

cd $PROJECT_ROOT_PATH

#
# now build the apps
#
for app in helloworld simpleapp
do
  cd $PROJECT_ROOT_PATH/examples/demo/$app

  mvn --batch-mode \
      -Drevision=$REVISION \
       clean install

  cd $PROJECT_ROOT_PATH
done

#
# finally, revert the version
#
cd $PROJECT_ROOT_PATH/core-parent
mvn versions:revert -Drevision=$REVISION -Dstarter-apps-modules
cd $PROJECT_ROOT_PATH
