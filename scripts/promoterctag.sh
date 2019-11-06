#!/bin/bash
#
# Removes an RC tag and replaces with final one
#
# usage: 
# promoterctag isis-1.2.0 RC1

git tag -f $1 $1-$2
git tag -d $1-$2
git push origin :refs/tags/$1-$2
git push origin refs/tags/$1:refs/tags/rel/$1
