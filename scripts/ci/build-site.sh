#!/bin/bash
set -e

SITE_CONFIG=$1

sh $CI_SCRIPTS_PATH/print-environment.sh "build-site"

echo ""
echo "\$SITE_CONFIG: ${SITE_CONFIG}"
echo ""

# TODO: run groovy.
#  However, this currently needs a local build of the metadata ... will also need to pull down from repo and unzip
#groovy $CI_SCRIPTS_PATH/generateConfigDocs -f "./core/config/target/classes/META-INF/spring-configuration-metadata.json" -o ./core/config/_adoc/modules/config/examples/generated \
#

# run antora
$(npm bin)/antora --stacktrace $SITE_CONFIG

