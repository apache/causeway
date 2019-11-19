#!/bin/bash

SRC_MAIN_JAVA=../../../java
SCRIPT_DIR=$( dirname "$0" )
cd $SCRIPT_DIR



SRC_APPLIB=$SRC_MAIN_JAVA/org/apache/isis/applib

# clock
mkdir -p examples/clock
cp -R $SRC_APPLIB/clock/* examples/clock

# domain
mkdir -p examples/domain
cp -R $SRC_APPLIB/domain/* examples/domain

# events
mkdir -p examples/events
cp -R $SRC_APPLIB/events/* examples/events

# layout
mkdir -p examples/layout
cp -R $SRC_APPLIB/layout/* examples/layout

# mixins
mkdir -p examples/mixins
cp -R $SRC_APPLIB/mixins/* examples/mixins

# security
mkdir -p examples/security
cp -R $SRC_APPLIB/security/* examples/security

# spec
mkdir -p examples/spec
cp -R $SRC_APPLIB/spec/* examples/spec

# tree
mkdir -p examples/tree
cp -R $SRC_APPLIB/tree/* examples/tree

# util
mkdir -p examples/util
cp -R $SRC_APPLIB/util/* examples/util

# value
mkdir -p examples/value
cp -R $SRC_APPLIB/value/* examples/value

