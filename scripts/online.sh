problem_size=${1:-50}
sample_size=200
sim_runs=300
initial_pop_param=0.2
simvalue_seed=1
sampling_policy="offline"
epsilon=${2:-1}
file_name="results/online-salp-ps"$problem_size"-"$sampling_policy"-eps"$epsilon".txt"
sample_seed=101
supply_seed=123

for ss in {1..10}
	do
	java -Xmx15g -Djava.library.path=$CPLEXJAVALIBDIR \
    	-classpath classes:$CPLEXJARDIR:lib/commons-collections-3.2.1.jar:lib/jakarta-oro-2.0.8.jar \
    	com.moallemi.matching.MatchingMain \
		model online data/tissue-types.txt \; \
		sample supply $problem_size $supply_seed \; \
		sample matched-pairs $problem_size $sample_size $(($ss*100+1)) \; \
		basis indicator \; \
		basis separable demand \; \
		solve salp $epsilon \; \
		policy dualfunction \; \
		simvalue dualpolicy-online $sim_runs $simvalue_seed >> $file_name
	done
