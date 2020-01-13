#!/bin/bash

SRC_MAIN_JAVA=../../../java
SCRIPT_DIR=$( dirname "$0" )
cd $SCRIPT_DIR || exit 1



SRC_APPLIB=$SRC_MAIN_JAVA/org/apache/isis/applib

for dir in clock domain events layout mixins security spec tree util value
do
  rm -rf examples/$dir
  mkdir -p examples/$dir
  cp -R $SRC_APPLIB/$dir/* examples/$dir
done
