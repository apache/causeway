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

#
# Utility script to quickly delete old branches both locally and remotely
#

usage() {
  echo "$(basename $0): [-x] branch_number" >&2
  echo "" >&2
  echo "    -x : execute (else, will just dry-run)" >&2
  echo "" >&2
  echo "eg: $(basename $0) -x 3576 " >&2
  echo "" >&2
  echo "    git branch -D CAUSEWAY-3576             # if exists" >&2
  echo "    git push origin CAUSEWAY-3576 --delete  # if exists" >&2
}

BRANCHNUM=""
EXEC=""

while getopts ":hx" arg; do
  case $arg in
    x)
      EXEC="exec"
      ;;
    h)
      usage
      exit 0
      ;;
    *)
      usage
      exit 1
  esac
done

shift $((OPTIND-1))
BRANCHNUM=$*


if [ -z "$BRANCHNUM"  ]; then
  usage
  exit 1
fi

echo ""
echo "BRANCHNUM : $BRANCHNUM"
echo "EXECUTE   : $EXEC"
echo ""

BRANCH="CAUSEWAY-${BRANCHNUM}"

LOCAL=$(git branch | grep $BRANCH)

if [ -n "$LOCAL" ]; then
  LOCAL_SHA=$(git rev-parse $BRANCH)
  echo git branch -D $BRANCH
  echo "local sha: $LOCAL_SHA"
  echo ""
  if [ -n "$EXEC" ]; then
    git branch -D $BRANCH
  fi
fi

REMOTE_SHA=$(git ls-remote 2>&1 | grep $BRANCH | awk '{ print $1 }')
if [ -n "$REMOTE_SHA" ]; then
  echo git push origin --delete $BRANCH
  echo "remote sha: $REMOTE_SHA"
  echo ""
  if [ -n "$EXEC" ]; then
    git push origin --delete $BRANCH
  fi
fi
echo ""

