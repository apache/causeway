# clean-up-tags-remote

REMOTE=$1
TEXT=$2

if [ "$REMOTE" == "" -o "$TEXT" == "" ]
then
    echo "usage: `basename $0` [remote] [text]" >&2
    exit 1
fi

for a in `git ls-remote --tags $REMOTE | awk '{print $2}' | cut -c11- | grep -v "\^{}$" | grep -v ^rel | grep $TEXT`
do
    echo git push $REMOTE :refs/tags/$a
    git push $REMOTE :refs/tags/$a
done

