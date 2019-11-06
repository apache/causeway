#!/bin/bash
#set -x
#trap read debug
set -e

echo ""
echo ""
echo ""

echo "\$REVISION             = ${REVISION}"
echo "\$GCPAPPENGINEREPO_URL = ${GCPAPPENGINEREPO_URL}"
echo "\$ORG_NAME             = ${ORG_NAME}"
echo "\$DOCKER_REGISTRY_URL  = ${DOCKER_REGISTRY_URL}"

echo ""
echo ""
echo ""

cd $PROJECT_ROOT_DIR/examples/apps/demo

export ISIS_VERSION=$REVISION

export FLAVOR=tomcat
mvn install -Dflavor=$FLAVOR -Dskip.git -Dskip.arch -DskipTests -Drevision=$REVISION -Disis.version=$ISIS_VERSION --batch-mode
mvn compile jib:build -Dflavor=$FLAVOR -Dskip.git -Dskip.arch -DskipTests -Drevision=$REVISION -Disis.version=$ISIS_VERSION --batch-mode

cd $PROJECT_ROOT_DIR


