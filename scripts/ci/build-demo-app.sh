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

export APP_NAME=isis-2-demo
export ISIS_VERSION=$REVISION
#printf '#!/bin/sh\nset -e\nmvn install -Dflavor=$FLAVOR -Dskip.git -Dskip.arch -DskipTests -Drevision=$REVISION -Disis.version=$ISIS_VERSION -Dmavenmixin-docker --batch-mode\n' > ./build.sh
#printf '#!/bin/sh\nset -e\nmvn -s $CI_BUILDS_DIR/examples/apps/demo/.m2/settings.xml docker:push@push-image-latest -Drevision=$REVISION -Disis.version=$ISIS_VERSION -DskipTests -DskipTag -Dskip.isis-swagger -Ddocker.registryUrl=$DOCKER_REGISTRY_URL\n' > ./push_latest.sh
#printf '#!/bin/sh\nset -e\nmvn -s $CI_BUILDS_DIR/examples/apps/demo/.m2/settings.xml docker:push@push-image-tagged -Dflavor=$FLAVOR -Drevision=$REVISION -Disis.version=$ISIS_VERSION -DskipTests -Dskip.isis-swagger -Ddocker.registryUrl=$DOCKER_REGISTRY_URL\n' > ./push_flavor.sh

export FLAVOR=tomcat
mvn compile jib:build -Dflavor=$FLAVOR -Dskip.git -Dskip.arch -DskipTests -Drevision=$REVISION -Disis.version=$ISIS_VERSION --batch-mode

#sh ./push_latest.sh
#sh ./push_flavor.sh

cd $PROJECT_ROOT_DIR


