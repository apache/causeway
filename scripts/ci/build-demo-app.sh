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
if [ -z "$REVISION" ]; then
  echo "\$REVISION is not set" >&2
  exit 1
fi
if [ -z "$MVN_STAGES" ]; then
  MVN_STAGES="clean install"
fi

sh $SCRIPT_DIR/print-environment.sh "build-demo-app"

export FLAVOR=$1
export ISIS_VERSION=$REVISION
echo ""
echo "\$Docker Image Flavor: ${FLAVOR}"
echo "\$Isis Version: ${ISIS_VERSION}"
echo ""

cd $PROJECT_ROOT_PATH/examples/apps/demo

mvn versions:set -DnewVersion=$REVISION -Drevision=$REVISION

mvn install \
    --batch-mode \
    -Dflavor=$FLAVOR \
    -Drevision=$REVISION \
    -Dskip.git \
    -Dskip.arch \
    -DskipTests

mvn compile jib:build \
    --batch-mode \
    -Dflavor=$FLAVOR \
    -Drevision=$REVISION \
    -Dskip.git \
    -Dskip.arch \
    -DskipTests

mvn versions:revert -Drevision=$REVISION

cd $PROJECT_ROOT_PATH
