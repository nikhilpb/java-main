#!/bin/bash

CONFIG=${1:-"config/config.xml"}

java -Xmx6g -Djava.library.path=$CPLEXJAVALIBDIR \
    	-classpath classes:$CPLEXJARDIR:lib/commons-collections-3.2.1.jar:lib/jakarta-oro-2.0.8.jar \
    	com.nikhilpb.matching.MatchingMain $CONFIG
