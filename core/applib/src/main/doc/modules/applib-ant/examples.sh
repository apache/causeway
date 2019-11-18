#!/bin/bash

SRC_MAIN_JAVA=../../../java
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd $SCRIPT_DIR



SRC_APPLIB=$SRC_MAIN_JAVA/org/apache/isis/applib

# annotations
mkdir -p examples/annotation
cp -R $SRC_APPLIB/annotation/* examples/annotation

