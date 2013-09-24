#!/bin/bash

problem_size=${1:-100}
sample_size=500
sample_runs=300
initial_pop_param=0.2
simvalue_seed=1

file_name="results/gen-regress-ps"$problem_size".txt"
echo "problem size = "$problem_size > $file_name

for ss in {1..10}
	do
	java -Xmx6g -Djava.library.path=$CPLEXJAVALIBDIR \
    	-classpath classes:$CPLEXJARDIR:lib/commons-collections-3.2.1.jar:lib/jakarta-oro-2.0.8.jar:lib/Jama-1.0.1.jar \
    	com.moallemi.matching.MatchingMain \
		model general data/tissue-types.txt \; \
		basis separable both \; \
		regress $sample_size $problem_size $initial_pop_param $(($ss*100+1)) \; \
		simvalue dualpolicy-general $sample_runs $problem_size $initial_pop_param $simvalue_seed >> $file_name
	done
