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


cd ../mixins

# can't use flatten pom, so have to edit directly instead...
mvn versions:set -DnewVersion=$REVISION
if [ $? -ne 0 ]; then
  exit 1
fi

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
if [ $? -ne 0 ]; then
  exit 1
fi

cd ..

# revert the edits from earlier
git checkout $(git status --porcelain | awk '{ print $2 }')
if [ $? -ne 0 ]; then
  exit 1
fi

