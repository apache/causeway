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

#set -x
#trap read debug
set -e

echo "=================  DRY RUN  =================="

if [ -z "$PROJECT_ROOT_PATH" ]; then
  export PROJECT_ROOT_PATH=$1
fi

if [ -z "$MVN_SNAPSHOTS_PATH" ]; then
  export MVN_SNAPSHOTS_PATH=$2
fi

if [ -z "$PROJECT_ROOT_PATH" ]; then
  echo "you probably forgot to pass first arg or set 'export PROJECT_ROOT_PATH=...'; using current dir"
  export PROJECT_ROOT_PATH=$PWD
fi

if [ -z "$MVN_SNAPSHOTS_PATH" ]; then
  echo "you probably forgot to pass second arg or set 'export MVN_SNAPSHOTS_PATH=...'; using /tmp fs"
  export MVN_SNAPSHOTS_PATH=/tmp/mvn-snapshots
fi

export BASELINE=2.0.0-M7
#export REVISION="$BASELINE.$(date +%Y%m%d)-$(date +%H%M)-dryrun"
export REVISION="$BASELINE-dryrun"

# (used by build-artifacts.sh)
export MVN_STAGES=deploy

# when 'off' keep unique REVISION that has SHA checksum - don't revert at end of script 
export REV_REVERT_FLAG=off

# used to skip building incubator docker images (demo vaadin)
export INCUBATOR=skip

# possible modes are
# attach ... enables the 'source' profile, which brings in the maven-source-plugin
# (else) ... explicitly ensure that maven-source-plugin is disabled
export SOURCE_MODE=attach

# -Dmodule-all ... build all modules
# -Denforcer.failFast=true ... fail fast on convergence issues (enforcer plugin)
# -T 1C ... 1 build thread per core
export MVN_ADDITIONAL_OPTS="\
-Dmodule-examples-demo-wicket \
-Dnightly-localfs-repo \
-Djacoco-report-xml \
-Dskip-docker=true \
-Denforcer.failFast=true \
-DskipTests=false"

# possible modes are
# push ... push docker images to dockerhub
# tar  ... build docker images and save them locally as tar files
# skip ... skip docker image build steps
export JIB_MODE=tar
export JIB_ADDITIONAL_OPTS="-Denv.REVISION=$REVISION"

CMD=$PROJECT_ROOT_PATH/scripts/ci/build-artifacts.sh

echo "=============================================="            
echo BASELINE            \: $BASELINE
echo REVISION            \: $REVISION
echo PROJECT_ROOT_PATH   \: $PROJECT_ROOT_PATH
echo CI_SCRIPTS_PATH     \: $CI_SCRIPTS_PATH
echo MVN_SNAPSHOTS_PATH  \: $MVN_SNAPSHOTS_PATH
echo MVN_ADDITIONAL_OPTS \: $MVN_ADDITIONAL_OPTS
echo JIB_MODE            \: $JIB_MODE
echo JIB_ADDITIONAL_OPTS \: $JIB_ADDITIONAL_OPTS
echo CMD                 \: $CMD 
echo "=============================================="
            
read -p "Press [Enter] key to start ..."
            
pushd $PROJECT_ROOT_PATH >> /dev/null

bash $CMD

popd >> /dev/null
