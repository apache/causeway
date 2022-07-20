#!/usr/bin/env bash

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

SKIP_TESTS=false

while getopts 'th' opt
do
  case $opt in
    t) export SKIP_TESTS=true
       ;;
    h) echo ""
       echo "build-tooling.sh options:"
       echo ""
       echo "  Skip options:"
       echo "  -t : skip tests"
       echo ""
       exit 1
       ;;
    *) echo "unknown option $opt - aborting" >&2
       exit 1
      ;;
  esac
done

shift $((OPTIND-1))

echo ""
echo "SKIP_TESTS : $SKIP_TESTS"

OPTS="$*"
if [[ "$SKIP_TESTS" == "true" ]]; then
  OPTS="$OPTS -DskipTests "
fi

echo mvn -Dmodule-tooling -Dskip.essential $OPTS install
mvn -Dmodule-tooling -Dskip.essential $OPTS install
