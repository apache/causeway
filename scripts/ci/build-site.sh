#!/bin/bash
set -e

SCRIPT_DIR=$( dirname "$0" )
if [ -z "$PROJECT_ROOT_PATH" ]; then
  export PROJECT_ROOT_PATH=`cd $SCRIPT_DIR/../.. ; pwd`
fi


bash $SCRIPT_DIR/_print-environment.sh "build-site"

bash $SCRIPT_DIR/_adoc-copy-examples.sh
bash $SCRIPT_DIR/_adoc-gen-config.sh


# check if any examples have not been sync'd
if [ "$CHECK_FOR_STALE_EXAMPLES" != "skip" ]; then
  WC=$(git status --porcelain | grep examples | wc -l)
  if [ "$WC" -ne "0" ]; then
    git status --porcelain
    echo "Some examples are out of date; run sync-adoc.sh and commit, then try again" >&2
    exit 1
  fi
fi

bash $SCRIPT_DIR/_adoc-antora.sh $*

# add a marker, that tells github not to use jekyll on the github pages folder
touch ${PROJECT_ROOT_PATH}/antora/target/site/.nojekyll
