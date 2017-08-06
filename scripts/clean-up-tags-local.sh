for a in `git tag --list | grep origin`
do
    git tag -d $a
done

for a in `git tag --list | grep upstream`
do
    git tag -d $a
done

for a in `git tag --list | grep ^isis-`
do
    git tag -d $a
done

for a in `git tag --list | grep ^todo`
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
