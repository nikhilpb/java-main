problem_size=${1:-100}
file_name="results/gen-greedy-ps"$problem_size".txt"
java -Xmx2g -Djava.library.path=$CPLEXJAVALIBDIR \
    -classpath classes:$CPLEXJARDIR:lib/commons-collections-3.2.1.jar:lib/jakarta-oro-2.0.8.jar \
    com.moallemi.matching.MatchingMain \
	model general data/tissue-types.txt \; \
	simvalue greedy-general 300 $problem_size 0.2 1 > $file_name
