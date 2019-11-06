#!/bin/bash
#set -x
#trap read debug

echo ""
echo ""
echo ""

echo "\$REVISION             = ${REVISION}"
echo "\$GCPAPPENGINEREPO_URL = ${GCPAPPENGINEREPO_URL}"
echo "\$DOCKER_REGISTRY_USERNAME = ${DOCKER_REGISTRY_USERNAME}"

echo ""
echo ""
echo ""


cd $PROJECT_ROOT_DIR/mixins

# can't use flatten pom, so have to edit directly instead...
mvn versions:set -DnewVersion=$REVISION
if [ $? -ne 0 ]; then
  exit 1
fi

mvn -s $PROJECT_ROOT_DIR/.m2/settings.xml \
    --batch-mode \
    $MVN_STAGES \
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

cd $PROJECT_ROOT_DIR

if [ -z "$CI_DRY_RUN" ]; then
  	
  	# revert the edits from earlier ...
	git checkout $(git status --porcelain | awk '{ print $2 }')
	if [ $? -ne 0 ]; then
	  exit 1
	fi

fi

