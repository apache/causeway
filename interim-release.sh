#!/bin/bash
VERSION_BASE=$1
REMOTE=$2

if [ -z "$VERSION_BASE" -o -z "$REMOTE" ]; then
    echo "usage: $(basename $0) [base version] [remote]"
    echo "   eg: $(basename $0) 1.13.0 incodehq"
    exit 1
fi

DATE=`date +'%Y%m%d-%H%M'`

VERSION="$VERSION_BASE.$DATE"
BRANCH="interim/$VERSION"
TAG="isis-$VERSION"

echo "removing any earlier (local) 'interim' branches"
for a in `git branch --list | grep interim`
do
    git branch -D $a
done

echo "checking out new branch $BRANCH"
git checkout -b "$BRANCH"

echo "updating version in all pom.xml files..."
pushd core >/dev/null
mvn versions:set -DnewVersion=$VERSION > /dev/null
popd >/dev/null

echo "Committing changes"
git commit -am "bumping to $VERSION"

echo "tagging"
git tag $TAG

echo "removing any earlier remote branches"
for a in `git ls-remote --heads $REMOTE  | sed 's?.*refs/heads/??' | grep interim`
do
    git push $REMOTE --delete $a
done

echo "pushing tag"
git push $REMOTE $TAG

echo "pushing branch"
git push $REMOTE $BRANCH

