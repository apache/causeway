#!/bin/bash
set -e

SCRIPT_DIR=$( dirname "$0" )
if [ -z "$PROJECT_ROOT_PATH" ]; then
  PROJECT_ROOT_PATH=`cd $SCRIPT_DIR/../.. ; pwd`
fi



echo "doc gen: copying over examples ..."
for examples_sh in $(find $PROJECT_ROOT_PATH -name examples.sh -print)
do
  echo $examples_sh
  sh $examples_sh
done
