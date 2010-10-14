#!/bin/sh

#
# update DEPLOYMENT_FLAGS to customize the way that [[NAME]] is run for a particular
# deployment.  For example, to run as a client in client/server mode, use:
# 
# DEPLOYMENT_FLAGS=--type client --connector encoding-sockets
#
# Consult the [[NAME]] documentation for the various options available.
#
DEPLOYMENT_FLAGS=

ROOT=`dirname $0`
cd $ROOT

java -jar expenses.jar $DEPLOYMENT_FLAGS $*

