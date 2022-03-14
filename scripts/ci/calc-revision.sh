#!/bin/bash

echo ""
echo ""
echo ""

DATE=$(date +%Y%m%d.%H%M)
BRANCH=$(echo $GITHUB_REF_NAME)

GIT_SHORT_COMMIT=$(echo $GITHUB_SHA | cut -c1-8)

REVISION=$BASELINE.$DATE.$BRANCH.$GIT_SHORT_COMMIT

echo "##[set-output name=revision;]${REVISION}"
