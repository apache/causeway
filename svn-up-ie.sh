svn up . --ignore-externals
while [ $? -ne 0 ]; do
    svn up . --ignore-externals
done
