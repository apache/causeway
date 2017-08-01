
#root="src/main/asciidoc/guides/cgcom/cgcom.adoc"
root="src"

for adoc in $(find $root -name "*.adoc" -print) ; do

    base_adoc=$(basename $adoc .adoc)
    dir_adoc=$(dirname $adoc)
	#echo "^\[\[_${base_adoc}\]\]$"

    for incl in $(grep "^\[\[.*\]\]$" $adoc | grep -v "^\[\[${base_adoc}\]\]$" | grep -v "^\[\[_${base_adoc}\]\]$" | grep -v "^\[\[__" ); do

		echo "$adoc : $incl"
	
    done


done
