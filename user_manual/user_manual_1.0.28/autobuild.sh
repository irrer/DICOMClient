
oldSum=x
while true ; do
    newSum=$( cat *Content.html | sha1sum | sed 's/ .*//' )
    if [ $newSum != $oldSum ] ; then
       echo
       build.sh
       date
	   oldSum=$newSum
    fi
    sleep 1
done

