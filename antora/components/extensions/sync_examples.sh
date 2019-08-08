#!/usr/bin/env bash
SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
if [ $# -ne 1 ]; then
    FILELIST=$SCRIPTPATH/examples.csv
else
    FILELIST=$1
fi
FILELISTFQN=$(readlink -f $FILELIST)
DIRNAME=$(dirname $FILELISTFQN)
BASENAME=$(basename $FILELISTFQN)

cd $DIRNAME >/dev/null 2>&1
cat $BASENAME | tail +2 | grep -v ^# | while read LINE
do
    FILE=$(echo $LINE | awk -F, '{print $1}' | awk '{$1=$1;print}')
    F2=$(echo $LINE | awk -F, '{print $2}' | awk '{$1=$1;print}')
    F3=$(echo $LINE | awk -F, '{print $3}' | awk '{$1=$1;print}')
    SOURCEFQN=$DIRNAME/$F2/$FILE
    TARGETDIR=$DIRNAME/$F3
    TARGETFQN=$DIRNAME/$F3/$FILE

    echo "SOURCEFQN: $SOURCEFQN"

    if [ -f "$SOURCEFQN" ]
    then

        if [ -f "$TARGETFQN" ]
        then
            rm $TARGETFQN
        fi
        mkdir -p $TARGETDIR
        cp $SOURCEFQN $TARGETFQN
    else
        echo "- does not exist ($SOURCEFQN) " >&2
        exit 1
    fi
done
cd - >/dev/null 2>&1