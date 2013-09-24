#!/bin/bash

problem_size=${1:-100}
epsilon=${2:-0}
sample_size=10
sample_runs=300
initial_pop_param=0.2
simvalue_seed=1
file_name="results/gen-salp-sgd-ps"$problem_size"-eps"$epsilon".txt"
echo "problem size = "$problem_size", epsilon = "$epsilon > $file_name

ss=101

java -Xmx6g -Djava.library.path=$CPLEXJAVALIBDIR \
    -classpath classes:$CPLEXJARDIR:lib/commons-collections-3.2.1.jar:lib/jakarta-oro-2.0.8.jar \
    com.moallemi.matching.MatchingMain \
	model general data/tissue-types.txt \; \
	sample instances-of-general $sample_size $problem_size $initial_pop_param $(($ss*100+1)) \; \
	basis separable both \; \
	solve sgd-general $epsilon 1.0 1.0
