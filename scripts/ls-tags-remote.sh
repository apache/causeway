# ls-tags-remote

REMOTE=$1

if [ "$REMOTE" == "" ]
then
    echo "usage: `basename $0` [remote]" >&2
    exit 1
fi

git ls-remote --tags $REMOTE | awk '{print $2}' | cut -c11- | grep -v "\^{}$"

