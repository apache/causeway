for a in `cat md-files.txt | grep -v ^#`
do
	dir=`dirname $a`
	htmlname=`echo $a | sed "s/\.md$/\.html/"`

	echo $htmlname

	mkdir -p "content/$dir"
	cat redirect-page.html > "content/$htmlname"
done
