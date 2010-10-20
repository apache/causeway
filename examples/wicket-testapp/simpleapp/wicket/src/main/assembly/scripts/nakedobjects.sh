#!/bin/sh

#
# update DEPLOYMENT_FLAGS to customize the way that Naked Objects is run for a particular
# deployment.  For example, to run as a client in client/server mode, use:
# 
# DEPLOYMENT_FLAGS=--type client --connector encoding-sockets
#
# Consult the Naked Objects documentation for the various options available.
#
DEPLOYMENT_FLAGS=

ROOT=`dirname $0`
cd $ROOT

java -jar simple.jar $DEPLOYMENT_FLAGS $*

