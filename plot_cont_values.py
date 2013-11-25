#!/usr/bin/python

import sys

file_name = 'results/' + sys.argv[1]

time_periods = int(sys.argv[2])

print 'plotting continuation values'
print 'file name: ' + file_name + ', time periods: ' + str(time_periods)

import csv

file = open(file_name, 'rb')
reader = csv.reader(file)

data = {}

for r in reader:
	if r[0] in data:
		data[r[0]].append([float(r[1]), float(r[2])])
	else:
		data[r[0]] = []

curves = {}

for k, l in data.iteritems():
	prices = []
	values = []
	for r in l:
		prices.append(r[0])
		values.append(r[1])
	curves[k] = [prices, values]

import matplotlib.pyplot as plt

for k in curves:
	plt.plot(curves[k][0], curves[k][1])

plt.show()
