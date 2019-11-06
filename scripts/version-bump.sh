#!/bin/bash
JIRA=$1
shift
REL=$1
shift

if [ ! "$JIRA" -o ! "$REL" ]; then
    echo "" >&2
    echo "usage: " >&2
    echo "" >&2
    echo "$(basename $0) \${ISISJIRA} \${ISISREL} " >&2
    echo "" >&2
    exit 1
fi

sed -i -E "s|<revision>[^<]+<|<revision>${REL}<|g" core/pom.xml
sed -i -E "s|<revision>[^<]+<|<revision>${REL}<|g" example/application/simpleapp/pom.xml
sed -i -E "s|<revision>[^<]+<|<revision>${REL}<|g" example/application/helloworld/pom.xml
git commit -am "${JIRA}: bumps revision property across all pom.xml's to ${REL}"

