#!/bin/bash

echo ""
echo ""
echo ""

if [ ! -z "$TIMESTAMP" ]
then
  # TIMESTAMP: 2022-03-14T18:43:38Z
  DATE="$(echo $TIMESTAMP | cut -c1-4)$(echo $TIMESTAMP | cut -c6-7)$(echo $TIMESTAMP | cut -c9-10).$(echo $TIMESTAMP | cut -c12-13)$(echo $TIMESTAMP | cut -c15-16)"
else
  DATE=$(date +%Y%m%d.%H%M)
fi
BRANCH=$(echo $GITHUB_REF_NAME)

GIT_SHORT_COMMIT=$(echo $GITHUB_SHA | cut -c1-8)

REVISION=$BASELINE.$DATE.$BRANCH.$GIT_SHORT_COMMIT

echo "##[set-output name=revision;]${REVISION}"
