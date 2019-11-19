#!/bin/bash

SRC_MAIN_JAVA=../../../java
SCRIPT_DIR=$( dirname "$0" )
cd $SCRIPT_DIR



SRC_APPLIB=$SRC_MAIN_JAVA/org/apache/isis/applib

# annotations
mkdir -p examples/annotation
cp -R $SRC_APPLIB/annotation/* examples/annotation

