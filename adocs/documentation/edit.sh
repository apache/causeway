#!/bin/bash

searchTerm="$@"
edit=gedit

function searchDir {
	files=($1*.adoc)
	if [ -f ${files[0]} ]; then
		loc=`grep -l "$searchTerm" $1*.adoc`
		if [ -n "$loc" ]; then
			echo "Found in $loc"
			$edit $loc
			exit
		fi
	fi

	for dir in $1*/
	do
		if [ -d "$dir" ]; then 
			searchDir $dir
		fi
	done
}

searchDir src/main/asciidoc/
