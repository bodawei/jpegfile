#!/bin/sh

# Right now, this is a lame wrapper.  Usage is:
# > cat myFile.txt | hex2bin > binFile

BINDIR="`dirname $0`"
JARFILE="$BINDIR/../target/jpegfile-0.1-SNAPSHOT.jar"

if [ ! -e ${JARFILE} ]
then
	echo "You must build the project first."
	exit 1;
else
	java -classpath ${JARFILE} bdw.cli.ShowSize "$@"
fi;
