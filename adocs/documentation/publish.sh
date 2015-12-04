#!/usr/bin/env bash
if [ "$1" = "" ]; then
	MSG=`git log -1 --pretty=%B`
else
	MSG=$1
fi
echo $MSG
mvn clean install -D message="$MSG"
