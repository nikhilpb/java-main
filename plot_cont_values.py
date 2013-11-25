#!/apps/gurobi/linux64/bin/python2.7

import sys

if len(sys.argv) != 3:
	sys.exit("expect 2 arguments")	

file_name = 'results/' + sys.argv[1]

time_periods = int(sys.argv[2])

print 'plotting continuation values'
print 'file name: ' + file_name + ', time perids: ' + str(time_periods)

import csv

file = open(file_name, 'rb')
reader = csv.reader(file)

data = {}

for r in reader:
	if r[0] in data:
		data[r[0]].append([float(r[1]), float(r[2])])
	else:
		data[r[0]] = []

print data

