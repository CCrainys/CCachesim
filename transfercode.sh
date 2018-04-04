
t="transfercode.sh"
temp="temp.java"

for entry in `ls`
do
	
	if [ $entry != $t ]
	then
		iconv -c -f GB2312 -t UTF-8 $entry >> $temp
		rm $entry
		mv $temp $entry
	fi	
	#iconv -c -f GB2312 -t UTF-8

done