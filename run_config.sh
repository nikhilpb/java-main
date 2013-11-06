#!/bin/bash

JAVA=java
CONFIGBASE="config/"
CONFIG=${2:-"main"}
MEMORY=${3:-"2g"}

FLAGS="-Xmx"$MEMORY" -Djava.library.path="$CPLEXJAVALIBDIR
LIBS=$CPLEXJARDIR":lib/commons-collections-3.2.1.jar:lib/jakarta-oro-2.0.8.jar:lib/Jama-1.0.1.jar"

case "$1" in 
"matching") MAINCLASS="com.nikhilpb.matching.MatchingMain"
            CONFIG=$CONFIGBASE"matching/"$CONFIG".xml"
		;;
"stopping") MAINCLASS="com.nikhilpb.stopping.StoppingMain"
            CONFIG=$CONFIGBASE"stopping/"$CONFIG
		;;
*) 	echo "no class corresponding to "$1
		exit
		;;
esac

$JAVA $FLAGS -classpath classes:$LIBS $MAINCLASS $CONFIG
