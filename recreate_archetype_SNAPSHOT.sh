#!/usr/bin/env bash
export ISISREL=2.0.0-M2-SNAPSHOT
export ISISDEV=2.0.0-M2-SNAPSHOT
export JIRA=ISIS-1811

export ISISTMP=/c/tmp   # or as required
export ISISPAR=$ISISREL
export ISISRC=RC1

#for a in simpleapp helloworld
for a in simpleapp
do
	pushd example/application/$a
	export ISISART=$a-archetype
    export ISISCPT=$(echo $ISISART | cut -d- -f2)
    export ISISCPN=$(echo $ISISART | cut -d- -f1)

	env | grep ISIS | sort
	sh ../../../scripts/recreate-archetype.sh $JIRA
	popd
done





