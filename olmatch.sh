java -Xmx4g -Djava.library.path=$CPLEXJAVALIBDIR \
    -classpath classes:$CPLEXJARDIR:lib/commons-collections-3.2.1.jar:lib/jakarta-oro-2.0.8.jar \
    com.moallemi.matching.MatchingMain \
	model tissue-types.txt \; \
	sample supply 75 123 \; \
	simvalue offline-match 300 1
