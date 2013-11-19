#!/bin/bash

problem_size=${1:-100}
epsilon=${2:-0}
sample_size=200
sample_runs=300
initial_pop_param=0.2
simvalue_seed=1
sample_seed=101
file_name="results/pools-salp-ps"$problem_size"-eps"$epsilon".txt"
echo "problem size = "$problem_size", epsilon = "$epsilon > $file_name

for ss in {1..10}
do
java -Xmx6g -Djava.library.path=$CPLEXJAVALIBDIR \
   	-classpath classes:$CPLEXJARDIR:lib/commons-collections-3.2.1.jar:lib/jakarta-oro-2.0.8.jar \
   	com.nikhilpb.pools.MatchingPoolsMain \
		model data/pools-model.txt \; \
		sample $sample_size $problem_size $(($ss*100+1)) \; \
		basis separable \; \
		solve salp "1E"$epsilon \; \
		value vf $sample_runs $problem_size $simvalue_seed
done
