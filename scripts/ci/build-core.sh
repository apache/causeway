#!/bin/bash
#set -x
#trap read debug

echo ""
echo ""
echo ""

echo "\$REVISION             = ${REVISION}"
echo "\$MVN_STAGES           = ${MVN_STAGES}"
echo "\$PROJECT_ROOT_DIR     = ${PROJECT_ROOT_DIR}"
echo "\$GCPAPPENGINEREPO_URL = ${GCPAPPENGINEREPO_URL}"
echo "\$CORE_ADDITIONAL_OPTS = ${CORE_ADDITIONAL_OPTS}"

echo ""
echo ""
echo ""

cd $PROJECT_ROOT_DIR/core

mvn -s $PROJECT_ROOT_DIR/.m2/settings.xml \
    --batch-mode \
    $MVN_STAGES \
    -Dgcpappenginerepo-deploy \
    -Dgcpappenginerepo-deploy.repositoryUrl=$GCPAPPENGINEREPO_URL \
    -Drevision=$REVISION \
    -Dskip.assemble-zip \
    $CORE_ADDITIONAL_OPTS
if [ $? -ne 0 ]; then
  exit 1
fi


cd $PROJECT_ROOT_DIR

