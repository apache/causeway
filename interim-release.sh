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
TAG="interim/isis-$VERSION"

CURR_BRANCH=`git rev-parse --abbrev-ref HEAD`

echo "checking out new branch $BRANCH"
git checkout -b "$BRANCH"

echo "updating version in all pom.xml files..."
pushd core-parent >/dev/null
mvn versions:set -DnewVersion=$VERSION > /dev/null
popd >/dev/null
pushd starter >/dev/null
mvn versions:set -DnewVersion=$VERSION > /dev/null
popd >/dev/null

echo "Committing changes"
git commit -am "bumping to $VERSION"

echo "Building"
mvn clean install -o

echo "tagging"
git tag $TAG

echo "pushing tag"
git push $REMOTE $TAG

echo "removing any earlier remote branches"
for a in `git ls-remote --heads $REMOTE  | sed 's?.*refs/heads/??' | grep interim`
do
    git push $REMOTE --delete $a
done

# need to push branch, so will be picked up for building by CI
echo "pushing branch"
git push $REMOTE $BRANCH

echo "switching back to original branch"
git checkout $CURR_BRANCH

echo "and deleting interim branch"
git branch -D $BRANCH
