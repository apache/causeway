#!/usr/bin/env bash
for a in `ls | grep _dg`
do
	b=$(echo $a | cut -c5- | sed 's|_|/|g')
	d=$(dirname $b)
	f=$(basename $b)
	mkdir -p ../$d
	mv $a ../$d/$f
done
