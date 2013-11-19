#!/bin/bash

problem_size=${1:-100}
epsilon=${2:-0}
sample_size=200
sample_runs=300
initial_pop_param=0.2
simvalue_seed=1
sample_seed=101

java -Xmx6g -Djava.library.path=$CPLEXJAVALIBDIR \
   	-classpath classes:$CPLEXJARDIR:lib/commons-collections-3.2.1.jar:lib/jakarta-oro-2.0.8.jar \
   	com.nikhilpb.pools.MatchingPoolsMain \
		model data/pools-model.txt \; \
		basis separable \; \
		solve ssgd 1 1.0 1.0 1000.0 5000 20 300 456 
