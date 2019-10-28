#!/bin/bash
#set -x
#trap read debug

echo ""
echo ""
echo ""

echo "\$REVISION             = ${REVISION}"
echo "\$GCPAPPENGINEREPO_URL = ${GCPAPPENGINEREPO_URL}"

echo ""
echo ""
echo ""


pushd mixins

# can't use flatten pom, so have to edit directly instead...
mvn versions:set -DnewVersion=$REVISION > /dev/null

mvn -s ../.m2/settings.xml \
    --batch-mode \
    clean deploy \
    -Dgcpappenginerepo-deploy \
    -Dgcpappenginerepo-deploy.repositoryUrl=$GCPAPPENGINEREPO_URL \
    -Drevision=$REVISION \
    -Dskip.mavenmixin-standard \
    -Dskip.mavenmixin-docker \
    -Dskip.mavenmixin-surefire \
    -Dskip.mavenmixin-datanucleus-enhance \
    $CORE_ADDITIONAL_OPTS

popd

