problem_size=${1:-50}
sample_size=1000
sim_runs=300
initial_pop_param=0.2
simvalue_seed=1
file_name="results/online-salp-matchone-ps"$problem_size"-greedy.txt"
sample_seed=101
supply_seed=123

for epsilon in 1E-4 1E-3 1E-2 1E-4 1 1E1 1E2 1E3 1E4 1E10
	do
	java -Xmx10g -Djava.library.path=$CPLEXJAVALIBDIR \
    	-classpath classes:$CPLEXJARDIR:lib/commons-collections-3.2.1.jar:lib/jakarta-oro-2.0.8.jar \
    	com.moallemi.matching.MatchingMain \
		model online data/tissue-types.txt \; \
		sample supply $problem_size $supply_seed \; \
		sample matched-pairs-greedy $sample_size $sample_seed \; \
		basis indicator \; \
		basis separable demand \; \
		solve salp $epsilon \; \
		policy dualfunction \; \
		simvalue dualpolicy-online $sim_runs $simvalue_seed >> $file_name
	done
