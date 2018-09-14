#!/usr/bin/env bash
export ISISREL=2.0.0-M2
export ISISDEV=2.0.0-M3-SNAPSHOT
export JIRA=ISIS-1811

export ISISTMP=/c/tmp   # or as required
export ISISPAR=$ISISREL
export ISISRC=RC1

for a in simpleapp helloworld
do
	pushd example/application/$a
	export ISISART=$a-archetype
    export ISISCPT=$(echo $ISISART | cut -d- -f2)
    export ISISCPN=$(echo $ISISART | cut -d- -f1)

	env | grep ISIS | sort
	sh ../../../scripts/recreate-archetype.sh $JIRA
	popd
done





