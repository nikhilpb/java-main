#!/bin/bash

JAVA=java
CONFIG=${2:-"config/config.xml"}
MEMORY="6g"

FLAGS="-Xmx"$MEMORY" -Djava.library.path="$CPLEXJAVALIBDIR
LIBS=$CPLEXJARDIR":lib/commons-collections-3.2.1.jar:lib/jakarta-oro-2.0.8.jar"

case "$1" in 
"matching") MAINCLASS="com.nikhilpb.matching.MatchingMain"
		;;
"stopping") MAINCLASS="com.nikhilpb.stopping.StoppingMain"
		;;
*) 	echo "no class corresponding to "$1
		exit
		;;
esac

$JAVA $FLAGS -classpath classes:$LIBS $MAINCLASS $CONFIG
