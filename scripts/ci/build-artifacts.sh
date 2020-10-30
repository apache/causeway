#!/bin/bash
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.

set -eo pipefail

if [ -z "$BATCH_MODE_FLAG" ] || [ "$BATCH_MODE_FLAG" != "off" ]; then
  BATCH_MODE=--batch-mode
fi

if [ -z "$JIB_PUSH_FLAG" ] || [ "$JIB_PUSH_FLAG" != "on" ]; then
  #DRYRUN
  JIB_MODE=buildTar
else
  #PUSH (JIB_PUSH_FLAG = on)
  JIB_MODE=build
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

sh $SCRIPT_DIR/_print-environment.sh "build-artifacts"

### FUNCTIONS

function buildDockerImage() {
	local dir=${1}
	
	cd $PROJECT_ROOT_PATH/${dir}
	
	echo ""
	echo ""
	echo ">>> $PROJECT_ROOT_PATH/${dir}: mvn compile jib:$JIB_MODE ..."
	echo ""
	echo ""
	
	mvn --batch-mode \
    	compile jib:$JIB_MODE \
    	-Dmaven.source.skip=true \
    	-Dskip.git \
    	-Dskip.arch \
    	-DskipTests
		    
}

### MAIN

if [ ! -z "$REVISION" ]; then

  echo ""
  echo ""
  echo ">>> mvn versions:set -DnewVersion=$REVISION ..."
  echo ""
  echo ""
  cd $PROJECT_ROOT_PATH
  mvn versions:set \
      -DprocessAllModules \
      -DnewVersion=$REVISION \
      -Dmodule-all \
      | fgrep --line-buffered -v "^Progress (1)" \
      | fgrep --line-buffered -v "Downloading from central" \
      | fgrep --line-buffered -v "Downloaded from central" \
      | fgrep --line-buffered -v "Downloading from DataNucleus_2" \
      | fgrep --line-buffered -v "Downloaded from DataNucleus_2"
      
  echo ""
  echo ""
  echo ">>> sed'ing version in starters and parent ..."
  echo ""
  echo ""
  cd $PROJECT_ROOT_PATH/starters
  CURR=$(grep "<version>" pom.xml | head -1 | cut -d'>' -f2 | cut -d'<' -f1)
  sed -i "s|<version>$CURR</version>|<version>$REVISION</version>|g" pom.xml

  # -- debug the version rewriting -- 
  # 1) add an exit statement after the fi below
  # exit 0
  # 2) run this script from project root via:
  # export REVISION=1.9.0-SNAPSHOT ; bash scripts/ci/build-artifacts.sh
  # 3) then inspect the pom files with following command:
  # find . -name "pom.xml" | xargs grep '<version>.*-SNAPSHOT</version>'

fi

cd $PROJECT_ROOT_PATH
echo ""
echo ""
echo ">>> ${PROJECT_ROOT_PATH}: mvn -s $SETTINGS_XML $MVN_STAGES $* $MVN_ADDITIONAL_OPTS"
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

# now build the individual docker images
# yet does not work when push flag is on
#buildDockerImage examples/demo/wicket 
#buildDockerImage examples/demo/vaadin

if [ ! -z "$REVISION" ]; then
  cd $PROJECT_ROOT_PATH
  echo ""
  echo ""
  echo ">>> mvn versions:revert ..."
  echo ""
  echo ""
  mvn versions:revert \
      -DprocessAllModules \
      -Dmodule-all \
      | fgrep --line-buffered -v "^Progress (1)" \
      | fgrep --line-buffered -v "Downloading from central" \
      | fgrep --line-buffered -v "Downloaded from central" \
      | fgrep --line-buffered -v "Downloading from DataNucleus_2" \
      | fgrep --line-buffered -v "Downloaded from DataNucleus_2"

  
  echo ""
  echo ""
  echo ">>> sed'ing to revert version in starters and parent ..."
  echo ""
  echo ""
  cd $PROJECT_ROOT_PATH/starters
  sed -i "s|<version>$REVISION</version>|<version>$CURR</version>|g" pom.xml
fi

cd $PROJECT_ROOT_PATH
