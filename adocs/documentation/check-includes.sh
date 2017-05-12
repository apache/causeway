
root="src/main/asciidoc/guides/cgcom/cgcom.adoc"
root="src"

for adoc in $(find $root -name "*.adoc" -print) ; do

    base_adoc=$(basename $adoc .adoc)
    dir_adoc=$(dirname $adoc)

    for incl in $(grep "^include::" $adoc ); do

        match=$(echo $incl | grep "^include::_$base_adoc" )
        if [ -z "$match" ] ; then 
            match2=$(echo $incl | grep "^include::$base_adoc" )
            if [ -z "$match2" ] ; then 
                echo "$adoc : $incl"
            fi
        fi

    done


done
