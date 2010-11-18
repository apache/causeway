mvn clean install -D modules=all -o
if [ $? != 0 ]
then
	echo "failed to 'clean install' all" >&2
	exit 1
fi
mvn site-deploy -D modules=standard -D site=full -D deploy=local -o
if [ $? != 0 ]
then
	echo "failed to 'site-deploy' full" >&2
	exit 1
fi
