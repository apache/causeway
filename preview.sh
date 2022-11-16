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

BASENAME_0=$(basename $0)

usage() {
 echo ""                                          >&2
 echo "$BASENAME_0 options:                     " >&2
 echo "                                         " >&2
 echo "  Skip options:                          " >&2
 echo "  -e|-E skip|only examples               " >&2
 echo "  -k|-K skip|only stale example check    " >&2
 echo "  -b|-B skip|only mvn -pl core/config    " >&2
 echo "  -c|-C skip|only config doc generation  " >&2
 echo "  -d|-D skip|only projdoc generation     " >&2
 echo "  -l|-L skip|only fix adoc line endings  " >&2
 echo "  -a|-A skip|only Antora generation      " >&2
 echo "  -s|-S skip|only serving generated site " >&2
 echo ""                                          >&2
 echo "  -y skip clear project site             " >&2
 echo "  -z skip cache (template)               " >&2
 echo ""                                          >&2
 echo "  -f antora/playbooks/site-xxx.yml       " >&2
 exit 1
}

export ANTORA_CACHE_DIR=.antora-cache-dir
export ANTORA_TARGET_SITE=antora/target/site

#
# for now, we disable index generation, because (a) java 11 dependency,
# probably not available on CI build server, and (b) need to build tooling.
#
# nevertheless, a committer can use this script to easily regenerate the
# index (only) using the -I flag.
#
PLAYBOOK_FILE=antora/playbooks/site.yml

BRANCH=$(git branch --show-current)
DATE=$(date +%Y%m%d-%H%M)
export REVISION="${BRANCH}.${DATE}"


while getopts 'bBECDAKSLecdakszylhf:' opt
do
  case $opt in
    E) export EXAMPLES=exec
       forcing=true ;;
    B) export BUILD_CONFIGS=exec
       forcing=true ;;
    C) export CONFIGS=exec
       forcing=true ;;
    D) export PROJDOC_GENERATION=exec
       forcing=true ;;
    L) export FIX_ADOC_LINE_ENDINGS=exec
       forcing=true ;;
    A) export ANTORA_GENERATION=exec
       export CLEAR_CACHE=exec
       export CLEAR_PREVIOUS=exec
       forcing=true ;;
    K) export STALE_EXAMPLE_CHECK=exec
       forcing=true ;;
    S) export SERVE=exec
       forcing=true ;;

    e) export EXAMPLES=skip ;;
    b) export BUILD_CONFIGS=skip ;;
    c) export CONFIGS=skip ;;
    d) export PROJDOC_GENERATION=skip ;;
    l) export FIX_ADOC_LINE_ENDINGS=skip ;;
    a) export ANTORA_GENERATION=skip
       export CLEAR_CACHE=skip
       export CLEAR_PREVIOUS=skip
      ;;
    k) export STALE_EXAMPLE_CHECK=skip ;;
    s) export SERVE=skip ;;

    z) export CLEAR_CACHE=skip ;;
    y) export CLEAR_PREVIOUS=skip ;;

    f) PLAYBOOK_FILE=$OPTARG ;;
    h) usage
       exit 1
       ;;
    *) echo "unknown option $opt - aborting" >&2
       usage
       exit 1
      ;;
  esac
done

if [ "$forcing" = "true" ]; then
    if [ -z "$EXAMPLES" ]; then
      export EXAMPLES=skip
    fi
    if [ -z "$BUILD_CONFIGS" ]; then
      export BUILD_CONFIGS=skip
    fi
    if [ -z "$CONFIGS" ]; then
      export CONFIGS=skip
    fi
    if [ -z "$PROJDOC_GENERATION" ]; then
      export PROJDOC_GENERATION=skip
    fi
    if [ -z "$FIX_ADOC_LINE_ENDINGS" ]; then
      export FIX_ADOC_LINE_ENDINGS=skip
    fi
    if [ -z "$ANTORA_GENERATION" ]; then
      export ANTORA_GENERATION=skip
      export CLEAR_CACHE=skip
      export CLEAR_PREVIOUS=skip
    fi
    if [ -z "$STALE_EXAMPLE_CHECK" ]; then
      export STALE_EXAMPLE_CHECK=skip
    fi
    if [ -z "$SERVE" ]; then
      export SERVE=skip
    fi
fi

echo ""
echo "-e|-E skip|only examples              : $EXAMPLES"
echo "-k|-K skip|only stale example check   : $STALE_EXAMPLE_CHECK"
echo "-b|-B skip|only mvn -pl core/config   : $BUILD_CONFIGS"
echo "-c|-C skip|only configs               : $CONFIGS"
echo "-d|-D skip|only projdoc generation    : $PROJDOC_GENERATION"
echo "-l|-L skip|only fix adoc line endings : $FIX_ADOC_LINE_ENDINGS"
echo "-a|-A skip|only antora generation     : $ANTORA_GENERATION"
echo "-s|-S skip|only serve                 : $SERVE"
echo "-y    skip clearing previous site     : $CLEAR_PREVIOUS"
echo "-z    skip clear cache (template)     : $CLEAR_CACHE"
echo ""

if [[ "$EXAMPLES" == "skip" ]]; then
  export SKIP_EXAMPLES=true
fi
if [[ "$CONFIGS" == "skip" ]]; then
  export SKIP_CONFIGS=true
fi
if [[ "$STALE_EXAMPLE_CHECK" == "skip" ]]; then
  export SKIP_STALE_EXAMPLE_CHECK=true
fi
if [[ "$PROJDOC_GENERATION" == "skip" ]]; then
  export SKIP_PROJDOC_GENERATION=true
fi
if [[ "$FIX_ADOC_LINE_ENDINGS" == "skip" ]]; then
  export SKIP_FIX_ADOC_LINE_ENDINGS=true
fi
if [[ "$ANTORA_GENERATION" == "skip" ]]; then
  export SKIP_ANTORA_GENERATION=true
fi
if [[ "$SERVE" == "skip" ]]; then
  export SKIP_SERVE=true
fi

if [[ "$BUILD_CONFIGS" == "skip" ]]; then
  echo "skipping mvn -pl core/config"
else
  mvn clean install -pl core/config -DskipTests
fi

if [[ "$CLEAR_CACHE" == "skip" ]]; then
  echo "skipping clearing the Antora cache"
else
  rm -rf $ANTORA_CACHE_DIR
fi

if [[ "$CLEAR_PREVIOUS" == "skip" ]]; then
  echo "skipping clearing any previous build site"
else
  rm -rf $ANTORA_TARGET_SITE
fi

if [ ! -f $PLAYBOOK_FILE ]; then
  echo "no such file $PLAYBOOK_FILE" >&2
  exit 1
fi

export ANTORA_CMD=antora

SECONDS=0
echo "\$PLAYBOOK_FILE = $PLAYBOOK_FILE"
bash build-site.sh $PLAYBOOK_FILE || exit 1

if [[ "$SERVE" == "skip" ]]; then
  echo "skipping serving"
else
  echo ""
  echo "http://localhost:5000/docs/latest/about.html"
  echo ""

  serve -S -p 5000 $ANTORA_TARGET_SITE
fi
