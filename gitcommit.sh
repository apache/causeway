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

usage() {
  echo "$(basename $0): [-A] [-P] message" >&2
  echo "  -A : suppress adding automatically, ie don't call 'git add .'" >&2
  echo "  -p : also push" >&2
}

PUSH=""
ADD=""

while getopts ":hAp" arg; do
  case $arg in
    h)
      usage
      exit 0
      ;;
    A)
      ADD="no-add"
      ;;
    p)
      PUSH="push"
      ;;
    *)
      usage
      exit 1
  esac
done

if [ $# -lt 1 ];
then
  echo $USAGE >&2
  exit 1
fi

shift $((OPTIND-1))

ISSUE=$(git rev-parse --abbrev-ref HEAD | cut -d- -f1,2)
MSG=$*

echo "ISSUE     : $ISSUE"
echo "MSG       : $MSG"
echo "(NO-)ADD  : $ADD"
echo "PUSH      : $PUSH"

if [ -d _pipeline-resources ]
then
  pushd _pipeline-resources || exit
  if [ -z "$ADD" ]
  then
    git add .
  fi
  git commit -m "$ISSUE: ${MSG}"
  if [ -n "$PUSH" ]
  then
    git push
  fi
  popd || exit
fi

if [ -z "$ADD" ]
then
  git add .
fi
git commit -m "$ISSUE: ${MSG}"
if [ -n "$PUSH" ]
then
  git push
fi
