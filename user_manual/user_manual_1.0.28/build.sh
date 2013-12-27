
mkdir target >/dev/null 2>&1

for f in *Content.html ; do
	out=target/$( echo $f | sed 's/Content.html/.html/')
	echo Constructing $out ...
    cat head.html $f tail.html > $out
done

