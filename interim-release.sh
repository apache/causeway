VERSION_BASE=$1

if [ ! "$VERSION_BASE" ]; then
    echo "usage: $(basename $0) [base version]"
    echo "   eg: $(basename $0) 1.13.0"
    exit 1
fi

DATE=`date +'%Y%m%d-%H%M'`

VERSION="$VERSION_BASE.$DATE"
BRANCH="interim/$VERSION"
TAG="isis-$VERSION"

git checkout -b "$BRANCH"

echo "updating version in all pom.xml files..."
pushd core >/dev/null
mvn versions:set -DnewVersion=$VERSION > /dev/null
popd >/dev/null

echo "Committing changes"
git commit -am "bumping to $VERSION"

echo "tagging"
git tag $TAG
