#!/usr/bin/python

import sys

if len(sys.argv) != 3:
	sys.exit("expect 2 arguments")	

file = 'results/{0}'.format(sys.argv[1])

print file
