#!/bin/sh

#
# update DEPLOYMENT_FLAGS to customize the way that Apache Isis is run for a particular
# deployment.  For example, to run as a client in client/server mode, use:
# 
# DEPLOYMENT_FLAGS=--type client --connector encoding-sockets
#
# Consult the Apache Isis documentation for the various options available.
#
DEPLOYMENT_FLAGS=

ROOT=`dirname $0`
cd $ROOT

java -jar ${rootArtifactId}.jar $DEPLOYMENT_FLAGS $*

