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

SCRIPT_DIR=$( dirname "$0" )
if [ -z "$PROJECT_ROOT_PATH" ]; then
  export PROJECT_ROOT_PATH=`cd $SCRIPT_DIR/../.. ; pwd`
fi


bash $SCRIPT_DIR/_print-environment.sh "build-site"

if [[ "$SKIP_EXAMPLES" == "true" ]]; then
  echo "skipping examples"
else
  bash $SCRIPT_DIR/_adoc-copy-examples.sh
fi


if [[ "$SKIP_CONFIGS" == "true" ]]; then
  echo "skipping config generation"
else
  bash $SCRIPT_DIR/_adoc-gen-config.sh
fi


if [[ "$SKIP_PROJDOC_GENERATION" == "true" ]]; then
  echo "skipping projdoc generation"
else
  bash $SCRIPT_DIR/_adoc-gen-projdoc.sh
fi

if [[ "$SKIP_FIX_ADOC_LINE_ENDINGS" == "true" ]]; then
  echo "skipping fix adoc line endings"
else
  bash $SCRIPT_DIR/_adoc-fix-adoc-line-endings.sh
fi



if [[ "$SKIP_ANTORA_GENERATION" == "true" ]]; then
  echo "skipping Antora generation ..."
else
  echo "generating site using Antora ..."
  bash $SCRIPT_DIR/_adoc-antora.sh $*
  echo "site built in ${SECONDS}s"
fi

if [[ "$SKIP_STALE_EXAMPLE_CHECK" == "true" ]]; then
  echo "skipping stale example check"
else
  WC=$(git status --porcelain | grep examples | wc -l)
  if [ "$WC" -ne "0" ]; then
    git status --porcelain
    echo "Some examples are out of date; commit changes and then try again" >&2
  fi
fi

# add a marker, that tells github not to use jekyll on the github pages folder
touch ${PROJECT_ROOT_PATH}/antora/target/site/.nojekyll

exit 0
