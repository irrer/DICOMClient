
mkdir target >/dev/null 2>&1

for f in *.html ; do
	out=target/${f}
	echo Constructing ${out} ...
    cat common/head.html ${f} common/tail.html > ${out}
done

