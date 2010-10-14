#!/bin/sh

ROOT=`dirname $0`
cd $ROOT

if [ ! -d "logs" ];  then
	mkdir logs	
fi

LOCALCLASSPATH=

DIRLIBS=./lib/*.jar
for dir in ${DIRLIBS}
do
    # if the directory is empty, then it will return the input string
    # this is stupid, so case for it
    if [ "$dir" != "${DIRLIBS}" ] ; then
		LOCALCLASSPATH="$dir":$LOCALCLASSPATH
    fi
done

java -cp $LOCALCLASSPATH org.apache.isis.runtime.Isis -t server -r hibernate -x xstream -a database $*


