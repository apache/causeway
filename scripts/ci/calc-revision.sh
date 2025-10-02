#!/bin/bash

#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
#

echo ""
echo ""
echo ""

if [ ! -z "$TIMESTAMP" ]
then
  # TIMESTAMP: 2022-03-14T18:43:38Z
  DATE="$(echo $TIMESTAMP | cut -c1-4)$(echo $TIMESTAMP | cut -c6-7)$(echo $TIMESTAMP | cut -c9-10).$(echo $TIMESTAMP | cut -c12-13)$(echo $TIMESTAMP | cut -c15-16)"
else
  DATE=$(date +%Y%m%d.%H%M)
fi
BRANCH=$(echo $GITHUB_REF_NAME)

GIT_SHORT_COMMIT=$(echo $GITHUB_SHA | cut -c1-8)

REVISION=$BASELINE.$DATE.$BRANCH.$GIT_SHORT_COMMIT

echo "##[set-output name=revision;]${REVISION}"
