#!/bin/bash

problem_size=${1:-100}
sample_runs=300
simvalue_seed=1

java -Xmx6g -Djava.library.path=$CPLEXJAVALIBDIR \
    -classpath classes:$CPLEXJARDIR:lib/commons-collections-3.2.1.jar:lib/jakarta-oro-2.0.8.jar \
    com.nikhilpb.pools.MatchingPoolsMain \
	model data/pools-model.txt \; \
	value offline $sample_runs $problem_size $simvalue_seed
