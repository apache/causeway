#!/bin/sh

ROOT=`dirname $0`
cd $ROOT

java -jar fitnesse.testapp.jar -t exploration -v html
