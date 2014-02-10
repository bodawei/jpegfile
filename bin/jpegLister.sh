#!	/bin/sh

BINDIR="`dirname $0`"
JARFILE="$BINDIR/../target/jpegfile-0.1-SNAPSHOT.jar"

if [ ! -e ${JARFILE} ]
then
	echo "You must build the project first."
	exit 1;
else
	java -classpath ${JARFILE} bdw.cli.Lister "$@"
fi;

