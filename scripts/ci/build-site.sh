#!/bin/bash
set -e

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


SITE_CONFIG=$1

bash $SCRIPT_DIR/_print-environment.sh "build-site"

bash $SCRIPT_DIR/_adoc-copy-examples.sh
bash $SCRIPT_DIR/_adoc-gen-config.sh


##
## check if anything had not been sync'd
##
WC=$(git status --porcelain | wc -l)
if [ "$WC" -ne "0" ]; then
  echo "Some examples are out of date; run sync-examples.sh and commit, then try again" >&2
  exit 1
fi




##
## run antora
##
echo "running antora ..."
if [ -z "$ANTORA_CMD" ]; then
  ANTORA_CMD=$(npm bin)/antora
fi

$ANTORA_CMD --stacktrace $SITE_CONFIG

# add a marker, that tells github not to use jekyll on the github pages folder
touch ${PROJECT_ROOT_PATH}/antora/target/site/.nojekyll
