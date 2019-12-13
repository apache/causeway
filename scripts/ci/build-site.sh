#!/bin/bash
set -e

SCRIPT_DIR=$( dirname "$0" )
if [ -z "$PROJECT_ROOT_PATH" ]; then
  PROJECT_ROOT_PATH=`cd $SCRIPT_DIR/../.. ; pwd`
fi


bash $SCRIPT_DIR/_print-environment.sh "build-site"

bash $SCRIPT_DIR/_adoc-copy-examples.sh
bash $SCRIPT_DIR/_adoc-gen-config.sh


# check if anything had not been sync'd
WC=$(git status --porcelain | wc -l)
if [ "$WC" -ne "0" ]; then
  git status --porcelain
  echo "Some examples are out of date; run sync-adoc.sh and commit, then try again" >&2
  exit 1
fi

bash $SCRIPT_DIR/_adoc-antora.sh ${PROJECT_ROOT_PATH}/$*

# add a marker, that tells github not to use jekyll on the github pages folder
touch ${PROJECT_ROOT_PATH}/antora/target/site/.nojekyll
