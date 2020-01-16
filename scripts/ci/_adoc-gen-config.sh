#!/bin/bash

SCRIPT_DIR=$( dirname "$0" )
if [ -z "$PROJECT_ROOT_PATH" ]; then
  PROJECT_ROOT_PATH=`cd $SCRIPT_DIR/../.. ; pwd`
fi

if [ -z "$REVISION" ]; then
  if [ ! -z "$SHARED_VARS_FILE" ] && [ -f "$SHARED_VARS_FILE" ]; then
    . $SHARED_VARS_FILE
    export $(cut -d= -f1 $SHARED_VARS_FILE)
  fi
fi

if [ -z "$REVISION" ]; then
  export REVISION="SNAPSHOT"
fi




##
## run groovy
##
GROOVY_CMD=$(command -v groovy)
DOS2UNIX_CMD=$(command -v dos2unix)

echo ""
echo "\$GROOVY_CMD   : ${GROOVY_CMD}"
echo "\$DOS2UNIX_CMD : ${DOS2UNIX_CMD}"
echo ""
 
# for now meant to run with nightly builds only 
if [ -z "${GROOVY_CMD}" ]; then
  echo "doc gen: no groovy, skipping"
else
  if [ ! -f "$PROJECT_ROOT_PATH/core/config/target/classes/META-INF/spring-configuration-metadata.json" ]; then
    echo "doc gen: no spring-configuration-metadata.json to parse: skipping"
  else
    # generate automated site content (adoc files)
    echo "doc gen: generating config .adoc from Spring metadata ..."

    rm -rf $PROJECT_ROOT_PATH/core/config/src/main/adoc/modules/config/examples/generated

    ${GROOVY_CMD} $SCRIPT_DIR/../generateConfigDocs.groovy \
      -f $PROJECT_ROOT_PATH/core/config/target/classes/META-INF/spring-configuration-metadata.json \
      -o $PROJECT_ROOT_PATH/core/config/src/main/adoc/modules/config/examples/generated

    if [ ! -z "${DOS2UNIX_CMD}" ]; then
      for FILE in $PROJECT_ROOT_PATH/core/config/src/main/adoc/modules/config/examples/generated/*
      do
        ${DOS2UNIX_CMD} $FILE
      done
      echo
      echo
      echo
    fi
  fi
fi


