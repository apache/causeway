svn up .
while [ $? -ne 0 ]; do
    svn up .
done
