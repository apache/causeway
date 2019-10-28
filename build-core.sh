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

pushd core

mvn -s .m2/settings.xml \
    --batch-mode \
    clean deploy \
    -Dgcpappenginerepo-deploy \
    -Dgcpappenginerepo-deploy.repositoryUrl=$GCPAPPENGINEREPO_URL \
    -Drevision=$REVISION \
    -Dskip.assemble-zip \
    $CORE_ADDITIONAL_OPTS

popd

