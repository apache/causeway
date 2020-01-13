#!/bin/bash
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


if [ "$SKIP_STALE_EXAMPLE_CHECK" == "true" ]; then
  echo "skipping stale example check"
else
  WC=$(git status --porcelain | grep examples | wc -l)
  if [ "$WC" -ne "0" ]; then
    git status --porcelain
    echo "Some examples are out of date; run sync-adoc.sh and commit, then try again" >&2
    exit 1
  fi
fi

if [ "$SKIP_GENERATION" == "true" ]; then
  echo "skipping building..."
else
  echo "building ..."
  bash $SCRIPT_DIR/_adoc-antora.sh $*
  echo "site built in ${SECONDS}s"
fi

# add a marker, that tells github not to use jekyll on the github pages folder
touch ${PROJECT_ROOT_PATH}/antora/target/site/.nojekyll
