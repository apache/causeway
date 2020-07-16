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

SCRIPT_DIR=$( dirname "$0" )
if [ -z "$PROJECT_ROOT_PATH" ]; then
  PROJECT_ROOT_PATH=`cd $SCRIPT_DIR/../.. ; pwd`
fi

sh $SCRIPT_DIR/_print-environment.sh "build-core-using-gradle"

# use maven to run the JAXB Java Source Generator
# this step could be migrated to run with gradle instead
# that would be the api/schema/build.gradle file to put some love to

cd $PROJECT_ROOT_PATH/api/schema
mvn generate-sources

# build the rest with gradle ...

cd $PROJECT_ROOT_PATH

gradle build

cd $PROJECT_ROOT_PATH
