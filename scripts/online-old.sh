for ss in 101 # 201 301 401 501 601 701 801 901 1001
	do
	java -Xmx10g -Djava.library.path=$CPLEXJAVALIBDIR \
    	-classpath classes:$CPLEXJARDIR:lib/commons-collections-3.2.1.jar:lib/jakarta-oro-2.0.8.jar \
    	com.moallemi.matching.MatchingMain \
		model online data/tissue-types.txt \; \
		sample supply 50 123 \; \
		sample matched-pairs-salp greedy 1000 $ss \; \
		basis indicator \; \
		basis separable demand \; \
		solve salp 1E-1 \; \
		policy dualfunction \; \
		simvalue dualpolicy-online 300 1
	done
