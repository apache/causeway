#!/bin/bash
#set -x
#trap read debug

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

cd examples

mvn --batch-mode \
    -Dexample-apps \
    clean install \
    -Drevision=$REVISION
if [ $? -ne 0 ]; then
  exit 1
fi

cd ..


#export APP_NAME=helloworld
#cd examples/apps/$APP_NAME
#
#mvn --batch-mode \
#    install \
#    -Drevision=$REVISION \
#    -Disis.version=$REVISION \
#    -Dmavenmixin-docker \
#    -Ddocker-plugin.imageName=$ORG_NAME/$APP_NAME
#if [ $? -ne 0 ]; then
#  exit 1
#fi
#
#mvn -s ../../../.m2/settings.xml \
#    --batch-mode \
#    docker:push@push-image-tagged \
#    -DskipTests \
#    -Dskip.isis-validate \
#    -Dskip.isis-swagger \
#    -Drevision=$REVISION \
#    -Disis.version=$REVISION \
#    -Dmavenmixin-docker \
#    -Ddocker-plugin.imageName=$ORG_NAME/$APP_NAME \
#    -Ddocker-plugin.serverId=docker-registry \
#    -Ddocker.registryUrl=$DOCKER_REGISTRY_URL
#if [ $? -ne 0 ]; then
#  exit 1
#fi
#
#cd ../../..

#export APP_NAME=simpleapp
#cd examples/apps/$APP_NAME
#
#mvn --batch-mode \
#    install \
#    -Drevision=$REVISION \
#    -Disis.version=$REVISION \
#    -Dmavenmixin-docker \
#    -Ddocker-plugin.imageName=$ORG_NAME/$APP_NAME
#if [ $? -ne 0 ]; then
#  exit 1
#fi
#
#mvn -s ../../../.m2/settings.xml \
#    --batch-mode \
#    docker:push@push-image-tagged \
#    -pl webapp \
#    -DskipTests \
#    -Dskip.isis-validate \
#    -Dskip.isis-swagger \
#    -Drevision=$REVISION \
#    -Disis.version=$REVISION \
#    -Dmavenmixin-docker \
#    -Ddocker-plugin.imageName=$ORG_NAME/$APP_NAME \
#    -Ddocker-plugin.serverId=docker-registry \
#    -Ddocker.registryUrl=$DOCKER_REGISTRY_URL
#if [ $? -ne 0 ]; then
#  exit 1
#fi
#
#cd ../../..
