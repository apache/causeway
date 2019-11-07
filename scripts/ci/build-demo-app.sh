#!/bin/bash
#set -x
#trap read debug
set -e

echo ""
echo ""
echo ""

echo "\$REVISION                 = ${REVISION}"
echo "\$PROJECT_ROOT_DIR         = ${PROJECT_ROOT_DIR}"
echo "\$DOCKER_REGISTRY_USERNAME = ${DOCKER_REGISTRY_USERNAME}"
echo "\$DOCKER_REGISTRY_PASSWORD = (suppressed)"

echo ""
echo ""
echo ""

cd $PROJECT_ROOT_DIR/examples/apps/demo

export FLAVOR=springboot
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

cd $PROJECT_ROOT_DIR


