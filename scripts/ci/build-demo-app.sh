#!/bin/bash
set -e

sh $CI_SCRIPTS_PATH/print-environment.sh

DOCKER_IMAGE_FLAVOR=$1
echo ""
echo "\$DOCKER_IMAGE_FLAVOR           = ${DOCKER_IMAGE_FLAVOR}"
echo ""

cd $PROJECT_ROOT_PATH/examples/apps/demo

export ISIS_VERSION=$REVISION

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


