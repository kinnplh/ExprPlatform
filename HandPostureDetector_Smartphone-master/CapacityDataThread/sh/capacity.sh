dirName=$1;
if [ ! -d "CapacityData" ]; then
	mkdir CapacityData
fi
if [ ! -d "CapacityData/${dirName}" ]; then
	mkdir CapacityData/${dirName}
fi
echo $$ > /sdcard/CapacityData/toBeKilled.pid
dmesg -c
for k in $(seq 1 1)
do
cat ~/proc/t*/t*
i=0
j=0
while :
do
#echo "fileNum: "+$j
#echo "frameNum: "+$i
let i=i+1
cat ~/sys/touchscreen/touch_chip_info
if [ $i -eq 64 ];
then
dmesg -c | grep "ts_thread" > /sdcard/CapacityData/${dirName}/capData$j.txt
i=0
let j=j+1
fi
done 
done