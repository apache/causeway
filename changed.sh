#!/usr/bin/env bash

#
# rebuilds using mvn just those modules that have changed.
#
# nb: incubator, regression and demo app are ignored (ie only the default profiles)
#
#

PL_ARG=$(git status --porcelain | cut -c4- | grep -v demo | grep "/src/main/java" | sed 's|/src/main/java/.*||' | sort -u | paste -sd "," -)

echo mvn install -DskipTests -am -o -pl $PL_ARG
mvn install -DskipTests -am -o -pl $PL_ARG
