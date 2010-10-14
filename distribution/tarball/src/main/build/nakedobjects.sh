#!/bin/sh

addToClassPath() {
	for jarfile in $1
	do
    	# if the directory is empty, then it will return the input string
	    # this is stupid, so case for it
    	if [ "$jarfile" != "$1" ] ; then
			LOCALCLASSPATH="$jarfile":$LOCALCLASSPATH
		fi
	done
}

ROOT=`dirname $0`
cd $ROOT

LOCALCLASSPATH=classes:resources

addToClassPath "./lib/*.jar"
addToClassPath "../../lib/*.jar"

java -cp $LOCALCLASSPATH org.apache.isis.runtime.Isis $*
