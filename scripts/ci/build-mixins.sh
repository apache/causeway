#!/bin/bash
set -e

echo "Shared Vars File: ${SHARED_VARS_FILE}"
echo
cat ${SHARED_VARS_FILE}
echo

# import shared vars (non secret!)
. $SHARED_VARS_FILE
export $(cut -d= -f1 $SHARED_VARS_FILE)

sh $CI_SCRIPTS_PATH/print-environment.sh "build-mixins"

cd $PROJECT_ROOT_PATH/mixins

# can't use flatten pom, so have to edit directly instead...
mvn versions:set -DnewVersion=$REVISION

mvn -s $PROJECT_ROOT_PATH/.m2/settings.xml \
    --batch-mode \
    $MVN_STAGES \
    -Drevision=$REVISION \
    -Dskip.mavenmixin-standard \
    -Dskip.mavenmixin-surefire \
    -Dskip.mavenmixin-datanucleus-enhance \
    $MVN_ADDITIONAL_OPTS

# revert the edits from earlier ...
mvn versions:revert

cd $PROJECT_ROOT_PATH


