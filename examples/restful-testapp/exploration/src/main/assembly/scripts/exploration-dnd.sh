#!/bin/sh

ROOT=`dirname $0`
cd $ROOT

java -jar restful.testapp.jar -t exploration -v dnd

