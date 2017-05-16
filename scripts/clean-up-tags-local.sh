for a in `git tag --list | grep upstream`
do
    git tag -d $a
done

for a in `git tag --list | grep ^isis-`
do
    git tag -d $a
done

for a in `git tag --list | grep ^quickstart`
do
    git tag -d $a
done

for a in `git tag --list | grep ^simple`
do
    git tag -d $a
done
