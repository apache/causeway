#!/bin/bash
set -eo pipefail

if [ -z "$BATCH_MODE_FLAG" ] || [ "$BATCH_MODE_FLAG" != "off" ]; then
  BATCH_MODE=--batch-mode
fi

SCRIPT_DIR=$( dirname "$0" )
if [ -z "$PROJECT_ROOT_PATH" ]; then
  PROJECT_ROOT_PATH=`cd $SCRIPT_DIR/../.. ; pwd`
fi

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
  echo ">>> mvn versions:set -DnewVersion=$REVISION ..."
  echo ""
  echo ""
  mvn versions:set \
      -DnewVersion=$REVISION \
      | fgrep --line-buffered -v "^Progress (1)" \
      | fgrep --line-buffered -v "Downloading from central" \
      | fgrep --line-buffered -v "Downloaded from central" \
      | fgrep --line-buffered -v "Downloading from DataNucleus_2" \
      | fgrep --line-buffered -v "Downloaded from DataNucleus_2"

  cd $PROJECT_ROOT_PATH/starters
  echo ""
  echo ""
  echo ">>> sed'ing version in starters ..."
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
    | fgrep --line-buffered -v "^Progress (1)" \
    | fgrep --line-buffered -v "Downloading from central" \
    | fgrep --line-buffered -v "Downloaded from central" \
    | fgrep --line-buffered -v "Downloading from DataNucleus_2" \
    | fgrep --line-buffered -v "Downloaded from DataNucleus_2" \
    | fgrep --line-buffered -v "Uploading from gcpappenginerepo" \
    | fgrep --line-buffered -v "Uploaded from gcpappenginerepo" \
    | fgrep --line-buffered -v "Downloading from gcpappenginerepo" \
    | fgrep --line-buffered -v "Downloaded from gcpappenginerepo" \
    | fgrep --line-buffered -v "[INFO] --- maven-enforcer-plugin" \
    | fgrep --line-buffered -v "[INFO] --- maven-site-plugin" \
    | fgrep --line-buffered -v "[INFO] <<< maven-source-plugin:" \
    | fgrep --line-buffered -v "[INFO] >>> maven-source-plugin" \
    | fgrep --line-buffered -v "[INFO] Installing" \
    | fgrep --line-buffered -v "[INFO] Copying" \
    | fgrep --line-buffered -v "[INFO] Using alternate deployment repository gcpappenginerepo" \
    | fgrep --line-buffered -v "[INFO] No site descriptor found: nothing to attach." \
    | fgrep --line-buffered -v "[INFO] Skipping because packaging 'jar' is not pom."

if [ ! -z "$REVISION" ]; then
  cd $PROJECT_ROOT_PATH/core-parent
  echo ""
  echo ""
  echo ">>> mvn versions:revert ..."
  echo ""
  echo ""
  mvn versions:revert \
      | fgrep --line-buffered -v "^Progress (1)" \
      | fgrep --line-buffered -v "Downloading from central" \
      | fgrep --line-buffered -v "Downloaded from central" \
      | fgrep --line-buffered -v "Downloading from DataNucleus_2" \
      | fgrep --line-buffered -v "Downloaded from DataNucleus_2"

  cd $PROJECT_ROOT_PATH/starters
  echo ""
  echo ""
  echo ">>> sed'ing to revert version in starters ..."
  echo ""
  echo ""
  sed -i "s|<version>$REVISION</version>|<version>$CURR</version>|g" pom.xml
fi

cd $PROJECT_ROOT_PATH
