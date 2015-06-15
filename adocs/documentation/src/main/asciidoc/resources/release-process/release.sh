#!/bin/bash
#
# parameterize
#

if [ "$OS" == "Windows_NT" ]; then
    ISISTMP=/c/tmp
else
    ISISTMP=/tmp
fi


# artifact
# releaseVersion
# developmentVersion
# release candidate

# export ISISART=isis
# export ISISREL=1.8.0
# export ISISDEV=1.9.0-SNAPSHOT
# export ISISRC=RC1

read -p "ISISART? ($ISISART): " xISISART
read -p "ISISREL? ($ISISREL): " xISISREL
read -p "ISISDEV? ($ISISDEV): " xISISDEV
read -p "ISISRC? ($ISISRC): " xISISRC

if [ ! -z $xISISART ]; then ISISART=$xISISART; fi
if [ ! -z $xISISREL ]; then ISISREL=$xISISREL; fi
if [ ! -z $xISISDEV ]; then ISISDEV=$xISISDEV; fi
if [ ! -z $xISISRC ]; then ISISRC=$xISISRC; fi

echo "" 
if [ -z $ISISART ]; then echo "ISISART is required">&2; exit; fi
if [ -z $ISISREL ]; then echo "ISISREL is required">&2; exit; fi
if [ -z $ISISDEV ]; then echo "ISISDEV is required">&2; exit; fi
if [ -z $ISISRC ]; then echo "ISISRC is required">&2; exit; fi

# derived
export ISISCPT=$(echo $ISISART | cut -d- -f2)
export ISISCPN=$(echo $ISISART | cut -d- -f3)
if [ $(echo "$ISISART" | grep -v "-") ]; then export ISISCOR="Y"; else export ISISCOR="N"; fi


echo "" 
echo "" 
echo "" 
echo "#################################################" 
echo "env vars"
echo "#################################################" 
echo "" 
env | grep ISIS | sort
echo "" 

exit

#
# release prepare
#
echo "" 
echo "" 
echo "" 
echo "#################################################" 
echo "release prepare" 
echo "#################################################" 
echo "" 
echo "" 
echo "" 


# eg isis-1.4.0-RC1
git checkout $ISISART-$ISISREL-$ISISRC 
if [ $? -ne 0 ]; then
    echo "git checkout $ISISART-$ISISREL-$ISISRC  failed :-(" >&2
    exit 1
fi

mvn release:prepare -P apache-release -D dryRun=true -DreleaseVersion=$ISISREL -DdevelopmentVersion=$ISISDEV -Dtag=$ISISART-$ISISREL-$ISISRC
if [ $? -ne 0 ]; then
    echo "mvn release:prepare -DdryRun=true failed :-("  >&2
    exit 1
fi

mvn release:prepare -P apache-release -D skipTests=true -Dresume=false -DreleaseVersion=$ISISREL -DdevelopmentVersion=$ISISDEV -Dtag=$ISISART-$ISISREL-$ISISRC
if [ $? -ne 0 ]; then
    echo "mvn release:prepare failed :-("  >&2
    exit 1
fi


#
# sanity check
#
echo "" 
echo "" 
echo "" 
echo "#################################################" 
echo "sanity check" 
echo "#################################################" 
echo "" 
echo "" 
echo "" 

rm -rf $ISISTMP/$ISISART-$ISISREL
mkdir $ISISTMP/$ISISART-$ISISREL

if [ "$ISISCOR" == "Y" ]; then
    ZIPDIR="$M2_REPO/repository/org/apache/isis/core/$ISISART/$ISISREL"
else
    ZIPDIR="$M2_REPO/repository/org/apache/isis/$ISISCPT/$ISISART/$ISISREL"
fi
echo "cp \"$ZIPDIR/$ISISART-$ISISREL-source-release.zip\" $ISISTMP/$ISISART-$ISISREL/."
cp "$ZIPDIR/$ISISART-$ISISREL-source-release.zip" $ISISTMP/$ISISART-$ISISREL/.

pushd $ISISTMP/$ISISART-$ISISREL
unzip $ISISART-$ISISREL-source-release.zip

cd $ISISART-$ISISREL
mvn clean install
if [ $? -ne 0 ]; then
    echo "sanity check failed :-("  >&2
    popd
    exit 1
fi

cat DEPENDENCIES

popd


#
# release perform
#
echo "" 
echo "" 
echo "" 
echo "#################################################" 
echo "release perform" 
echo "#################################################" 
echo "" 
echo "" 
echo "" 

mvn release:perform -P apache-release -DworkingDirectory=$ISISTMP/$ISISART-$ISISREL-$ISISRC
if [ $? -ne 0 ]; then
    echo "mvn release:perform failed :-("  >&2
    exit 1
fi


#
# nexus
#
echo "" 
echo "" 
echo "" 
echo "#################################################" 
echo "nexus staging" 
echo "#################################################" 
echo "" 
echo "" 
echo "" 
read -p "Hit enter when staged in nexus (else ^C): " CONFIRM



#
# git push branch/tag
#
echo "" 
echo "" 
echo "" 
echo "#################################################" 
echo "git push branch/tag" 
echo "#################################################" 
echo "" 
echo "" 
echo "" 

git push -u origin prepare/$ISISART-$ISISREL-$ISISRC
git push origin refs/tags/$ISISART-$ISISREL:refs/tags/$ISISART-$ISISREL-$ISISRC
git fetch
