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

set -e

# example usage from project root:
# 1) build all modules
# mvn install -Dmodule-all
# 2) build the docker images as tar files
# export JIB_MODE=tar ; bash scripts/ci/build-docker-images.sh
#
# possible modes are
# push ... push docker images to dockerhub
# tar  ... build docker images and save them locally as tar files
# skip ... skip docker image build steps
case $JIB_MODE in
  "push") JIB_CMD="build" ;;
  "tar") JIB_CMD="buildTar" ;;
  *) JIB_CMD="skip" ;;
esac

if [ -z "$BATCH_MODE_FLAG" ] || [ "$BATCH_MODE_FLAG" != "off" ]; then
  BATCH_MODE=--batch-mode
fi

SCRIPT_DIR=$( dirname "$0" )
if [ -z "$PROJECT_ROOT_PATH" ]; then
  PROJECT_ROOT_PATH=`cd $SCRIPT_DIR/../.. ; pwd`
fi

if [ -z "$REVISION" ]; then
  if [ ! -z "$SHARED_VARS_FILE" ] && [ -f "$SHARED_VARS_FILE" ]; then
    . $SHARED_VARS_FILE
    export $(cut -d= -f1 $SHARED_VARS_FILE)
  fi
fi
if [ -z "$MVN_STAGES" ]; then
  MVN_STAGES="clean install"
fi

bash $SCRIPT_DIR/_print-environment.sh "build-demo-app"

export CAUSEWAY_VERSION=$REVISION
echo ""
echo "\$Causeway Version: ${CAUSEWAY_VERSION}"
echo ""

function setRevision() {
	local dir=${1}

	if [ ! -z "$REVISION" ]; then
	  cd $PROJECT_ROOT_PATH/${dir}
	  mvn versions:set -DnewVersion=$REVISION -DprocessAllModules=true -Dmodule-all
	fi
}

function revertRevision() {
	local dir=${1}

	if [ ! -z "$REVISION" ]; then
	  cd $PROJECT_ROOT_PATH/${dir}
	  mvn versions:revert -DnewVersion=$REVISION -DprocessAllModules=true -Dmodule-all
	fi
}

function buildDockerImage() {
	local dir=${1}

	cd $PROJECT_ROOT_PATH/${dir}

	echo ""
	echo ""
	echo ">>> $PROJECT_ROOT_PATH/${dir}: mvn compile jib:$JIB_CMD ..."
	echo ""
	echo ""

	mvn --batch-mode \
    	compile jib:$JIB_CMD \
    	-Dmaven.source.skip=true \
    	-Dskip.git \
    	-Dskip.arch \
    	-DskipTests

}


setRevision

# -- debug the version rewriting --
# 1) add an exit statement after the comments below
# exit 0
# 2) run this script from project root via:
# export REVISION=1.9.0-SNAPSHOT ; export JIB_MODE=skip ; bash scripts/ci/build-docker-images.sh
# 3) then inspect the pom files with following command:
# find . -name "pom.xml" | xargs grep '<version>.*-SNAPSHOT</version>'


# now build the individual docker images
if [ "$JIB_CMD" != "skip"  ]; then
  buildDockerImage examples/demo/wicket
fi

revertRevision

