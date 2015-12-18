#!/usr/bin/env bash

SUBMIT=/home/hadoop/shell/submit.sh
CONF=/home/hadoop/conf/pipe3to4.conf
submitArg=""
DRYRUN=""
VERBOSE=0
DATERANGE=0

LOGERR=$(mktemp)
echo "error log in $LOGERR"

usage="$0 [-n|--dry-run] [-v|--verbose] [-d|--date-range begindate endate] [submitArg]"
while [[ $# > 0 ]]
do
key="$1"

case $key in
    -h|--help)
    echo "$usage"
    exit 0
    ;;
    -n|--dry-run)
    DRYRUN="-n"
    ;;
    -v|--verbose)
    VERBOSE=1
    ;;
    -d|--date-range)
    DATERANGE=1
    begindate=$2
    enddate=$3
    shift
    shift
    ;;
    *)
    submitArg="$*"
    break
    ;;
esac
shift # past argument or value
done

tmpfile=$(mktemp)

if [[ $DATERANGE -eq 1 ]]
then
    echo "$begindate" > $tmpfile
    echo "$enddate" >> $tmpfile
else
    echo "nothing" > $tmpfile
fi

echo $tmpfile | $SUBMIT -c $CONF $DRYRUN $submitArg 2>>$LOGERR
ret=$?
echo "$0 : submit return : $ret"
rm $tmpfile