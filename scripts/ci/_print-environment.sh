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

echo ""

echo "===========  ISIS CI SCRIPT [$1]  ================="
echo "\$REVISION                  = ${REVISION}"
echo "\$PROJECT_ROOT_PATH         = ${PROJECT_ROOT_PATH}"
echo "\$MVN_STAGES                = ${MVN_STAGES}"
echo "\$MVN_ADDITIONAL_OPTS       = ${MVN_ADDITIONAL_OPTS}"
echo "- Nightly Builds:"
echo "\$NIGHTLY_ROOT_PATH         = ${NIGHTLY_ROOT_PATH}"
echo "- GitHub:"
echo "\$GH_DEPLOY_OWNER           = ${GH_DEPLOY_OWNER}"
echo "\$GITHUB_REPOSITORY         = ${GITHUB_REPOSITORY}"
echo "- nexus.incode.work Repo:"
echo "\$NEXUS_INCODE_WORK_USERNAME = ${NEXUS_INCODE_WORK_USERNAME}"
echo "\$NEXUS_INCODE_WORK_PASSWORD = ***suppressed***"
echo "- Docker Hub:"
echo "\$DOCKER_REGISTRY_USERNAME = ${DOCKER_REGISTRY_USERNAME}"
echo "\$DOCKER_REGISTRY_PASSWORD = ***suppressed***"
echo "========================================================"

echo ""

