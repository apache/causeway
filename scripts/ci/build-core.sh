#!/bin/bash
set -e

if [ -z "$BATCH_MODE_FLAG" ] || [ "$BATCH_MODE_FLAG" != "off" ]; then
  BATCH_MODE=--batch-mode
fi

SCRIPT_DIR=$( dirname "$0" )
if [ -z "$PROJECT_ROOT_PATH" ]; then
  PROJECT_ROOT_PATH=`cd $SCRIPT_DIR/../.. ; pwd`
fi

#if [ -z "$REVISION" ]; then
#  if [ ! -z "$SHARED_VARS_FILE" ] && [ -f "$SHARED_VARS_FILE" ]; then
#    . $SHARED_VARS_FILE
#    export $(cut -d= -f1 $SHARED_VARS_FILE)
#  fi
#fi
if [ -z "$MVN_STAGES" ]; then
  MVN_STAGES="clean install"
fi
if [ -z "$SETTINGS_XML" ]; then
  SETTINGS_XML=$PROJECT_ROOT_PATH/.m2/settings.xml
fi

sh $SCRIPT_DIR/_print-environment.sh "build-core"

if [ ! -z "$REVISION" ]; then

  cd $PROJECT_ROOT_PATH/core-parent
  echo ""
  echo ""
  echo ""
  echo ""
  echo ">>> mvn versions:set -DnewVersion=$REVISION ..."
  echo ""
  echo ""
  echo ""
  echo ""
  mvn versions:set \
      -DnewVersion=$REVISION \
      | grep -v "^Progress (1)" \
      | grep -v "Downloading from central" \
      | grep -v "Downloaded from central" \
      | grep -v "Downloading from DataNucleus_2" \
      | grep -v "Downloaded from DataNucleus_2" \
      | grep -v "^\[INFO\] $"

  cd $PROJECT_ROOT_PATH/starters
  echo ""
  echo ""
  echo ""
  echo ""
  echo ">>> sed'ing version in starters ..."
  echo ""
  echo ""
  echo ""
  echo ""
  CURR=$(grep "<version>" pom.xml | head -1 | cut -d'>' -f2 | cut -d'<' -f1)
  sed -i "s|<version>$CURR</version>|<version>$REVISION</version>|g" pom.xml
fi

cd $PROJECT_ROOT_PATH/core-parent
echo ""
echo ""
echo ">>> mvn $MVN_STAGES $MVN_ADDITIONAL_OPTS"
echo ""
echo ""
mvn -s $SETTINGS_XML \
    $BATCH_MODE \
    -T1C \
    $MVN_STAGES \
    $MVN_ADDITIONAL_OPTS \
    $* \
    | grep -v "^Progress (1)" \
    | grep -v "Downloading from central" \
    | grep -v "Downloaded from central" \
    | grep -v "Downloading from DataNucleus_2" \
    | grep -v "Downloaded from DataNucleus_2" \
    | grep -v "Uploading from gcpappenginerepo" \
    | grep -v "Uploaded from gcpappenginerepo" \
    | grep -v "Downloading from gcpappenginerepo" \
    | grep -v "Downloaded from gcpappenginerepo" \
    | grep -v "^\[INFO\] $" \
    | grep -v "^\[INFO\] --- maven-enforcer-plugin" \
    | grep -v "^\[INFO\] --- maven-site-plugin" \
    | grep -v "^\[INFO\] <<< maven-source-plugin:" \
    | grep -v "^\[INFO\] >>> maven-source-plugin" \
    | grep -v "^\[INFO\] Using alternate deployment repository gcpappenginerepo" \
    | grep -v "^\[INFO\] No site descriptor found: nothing to attach." \
    | grep -v "^\[INFO\] Skipping because packaging 'jar' is not pom."

if [ ! -z "$REVISION" ]; then
  cd $PROJECT_ROOT_PATH/core-parent
  echo ""
  echo ""
  echo ">>> mvn versions:revert ..."
  echo ""
  echo ""
  mvn versions:revert \
      | grep -v "^Progress (1)" \
      | grep -v "Downloading from central" \
      | grep -v "Downloaded from central" \
      | grep -v "Downloading from DataNucleus_2" \
      | grep -v "Downloaded from DataNucleus_2" \
      | grep -v "^\[INFO\] $"

  cd $PROJECT_ROOT_PATH/starters
  echo ""
  echo ""
  echo ">>> sed'ing to revert version in starters ..."
  echo ""
  echo ""
  sed -i "s|<version>$REVISION</version>|<version>$CURR</version>|g" pom.xml
fi

cd $PROJECT_ROOT_PATH
