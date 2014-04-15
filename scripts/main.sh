#!/bin/bash

java -classpath classes:lib/commons-collections-3.2.1.jar:lib/jakarta-oro-2.0.8.jar \
    com.nikhilpb.matching.MatchingMain config/config1.xml
