#!/bin/sh

BINDIR="`dirname $0`"
JARFILE="$BINDIR/../target/jpegfile-0.9-SNAPSHOT.jar"
JPEGFILE="$1"

if [ "${JPEGFILE}" == "" -o "${JPEGFILE}" == "--help" -o "${JPEGFILE}" == "-h" ]
then
	echo "Usage: addComment.sh <jpeg-file-pathname> <output-jpeg-filename> <comment string>"
	exit 1;
fi;

if [ ! -e "${JPEGFILE}" ]
then
	echo "The file ${JPEGFILE} does not exist."
	exit 1;
fi;

if [ ! -e ${JARFILE} ]
then
	echo "You must build the project first with 'mvn package'."
	exit 1;
fi;

java -classpath ${JARFILE} bdw.cli.AddComment "$@"

