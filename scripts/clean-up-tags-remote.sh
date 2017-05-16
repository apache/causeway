REMOTE=$1

if [ "$REMOTE" == "" ]
then
    echo "usage: `basename $0` [remote]" >&2
    exit 1
fi

for a in `git ls-remote --tags $REMOTE | awk '{print $2}' | cut -c11- | grep -v "\^{}$" | grep -v ^rel | grep -v interim`
do
    echo git push $REMOTE :refs/tags/$a
    git push $REMOTE :refs/tags/$a
done

