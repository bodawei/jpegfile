#!/bin/sh

# Right now, this is a lame wrapper.  Usage is:
# > cat myFile.txt | validateutf8.sh
#  or
# > validateutf8.sh fileName.txt

BINDIR="`dirname $0`"
JARFILE="$BINDIR/../dist/JPEGFile.jar"

if [ ! -e ${JARFILE} ]
then
	echo "You must build the project first."
	exit 1;
else
	java -classpath ${JARFILE} bdw.cli.ValidateUtf8File "$@"
fi;

