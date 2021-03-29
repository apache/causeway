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

if [ -z "$REVISION" ]; then
  export REVISION="SNAPSHOT"
fi


MODE=projdoc
INDEX_PATH="${PROJECT_ROOT_PATH}/antora/components/refguide-index"
OVERVIEW_PATH="${PROJECT_ROOT_PATH}/core/adoc"

##
## run java
##
JAVA_CMD=$(command -v java)

echo ""
echo "\$JAVA_CMD     : ${JAVA_CMD}"
echo ""

# for now meant to run with nightly builds only
if [ -z "${JAVA_CMD}" ]; then
  echo "projdoc gen: no java, skipping"
else
  java -jar "${PROJECT_ROOT_PATH}/tooling/cli/target/isis-tooling-cli.jar" -p "${PROJECT_ROOT_PATH}" -r "${OVERVIEW_PATH}" -o "${INDEX_PATH}" $MODE
fi


