#!/bin/bash
set -e

sh $CI_SCRIPTS_PATH/print-environment.sh "build-demo-app"

export FLAVOR=$1
export ISIS_VERSION=$REVISION
echo ""
echo "\$Docker Image Flavor: ${FLAVOR}"
echo "\$Isis Version: ${ISIS_VERSION}"
echo ""

cd $PROJECT_ROOT_PATH/examples/apps/demo

mvn install \
    --batch-mode \
    -Dflavor=$FLAVOR \
    -Dskip.git \
    -Dskip.arch \
    -DskipTests \
    -Drevision=$REVISION \
    -Disis.version=$ISIS_VERSION

mvn compile jib:build \
    --batch-mode \
    -Dflavor=$FLAVOR \
    -Dskip.git \
    -Dskip.arch \
    -DskipTests \
    -Drevision=$REVISION \
    -Disis.version=$ISIS_VERSION

cd $PROJECT_ROOT_PATH


