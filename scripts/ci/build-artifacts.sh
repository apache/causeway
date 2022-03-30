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

mvn $MAVEN_CLI_OPTS versions:set -DnewVersion=$REVISION \
      | fgrep --line-buffered -v "CP: " \
      | fgrep --line-buffered -v "^Progress (1)" \
      | fgrep --line-buffered -v "Downloading from central" \
      | fgrep --line-buffered -v "Downloaded from central" \
      | fgrep --line-buffered -v "Downloading from DataNucleus_2" \
      | fgrep --line-buffered -v "Downloaded from DataNucleus_2" \
      | fgrep --line-buffered -v "Uploading from nexus_incode_work" \
      | fgrep --line-buffered -v "Uploaded from nexus_incode_work" \
      | fgrep --line-buffered -v "Downloading from nexus_incode_work" \
      | fgrep --line-buffered -v "Downloaded from nexus_incode_work" \
      | fgrep --line-buffered -v "[INFO] --- maven-enforcer-plugin" \
      | fgrep --line-buffered -v "[INFO] --- maven-site-plugin" \
      | fgrep --line-buffered -v "[INFO] <<< maven-source-plugin:" \
      | fgrep --line-buffered -v "[INFO] >>> maven-source-plugin" \
      | fgrep --line-buffered -v "[INFO] Installing" \
      | fgrep --line-buffered -v "[INFO] Copying" \
      | fgrep --line-buffered -v "[INFO] Using alternate deployment repository nexus_incode_work" \
      | fgrep --line-buffered -v "[INFO] No site descriptor found: nothing to attach." \
      | fgrep --line-buffered -v "[INFO] Skipping because packaging 'jar' is not pom."

mvn $MAVEN_CLI_OPTS -Drevision=$REVISION deploy \
     | fgrep --line-buffered -v "CP: " \
     | fgrep --line-buffered -v "^Progress (1)" \
     | fgrep --line-buffered -v "Downloading from central" \
     | fgrep --line-buffered -v "Downloaded from central" \
     | fgrep --line-buffered -v "Downloading from DataNucleus_2" \
     | fgrep --line-buffered -v "Downloaded from DataNucleus_2" \
     | fgrep --line-buffered -v "Uploading from nexus_incode_work" \
     | fgrep --line-buffered -v "Uploaded from nexus_incode_work" \
     | fgrep --line-buffered -v "Downloading from nexus_incode_work" \
     | fgrep --line-buffered -v "Downloaded from nexus_incode_work" \
     | fgrep --line-buffered -v "[INFO] --- maven-enforcer-plugin" \
     | fgrep --line-buffered -v "[INFO] --- maven-site-plugin" \
     | fgrep --line-buffered -v "[INFO] <<< maven-source-plugin:" \
     | fgrep --line-buffered -v "[INFO] >>> maven-source-plugin" \
     | fgrep --line-buffered -v "[INFO] Installing" \
     | fgrep --line-buffered -v "[INFO] Copying" \
     | fgrep --line-buffered -v "[INFO] Using alternate deployment repository nexus_incode_work" \
     | fgrep --line-buffered -v "[INFO] No site descriptor found: nothing to attach." \
     | fgrep --line-buffered -v "[INFO] Skipping because packaging 'jar' is not pom."
