#!/usr/bin/python

low = 90.0
high = 110.0
delta = 1.0
n = int((high - low) / delta) 
esses = [p*delta + low for p in range(n)]

import math

def phi(x):
    'Cumulative distribution function for the standard normal distribution'
    return (1.0 + math.erf(x / math.sqrt(2.0))) / 2.0

def black_scholes(s, k, r, sigma, t):
	d1 = 1. / (sigma * math.sqrt(t)) * (math.log(s/k) + (r + sigma**2)*t/2.)
	d2 = 1. / (sigma * math.sqrt(t)) * (math.log(s/k) + (r - sigma**2)*t/2.)
	return phi(d1) * s - phi(d2) * k

print map(lambda x: black_scholes(x, 100., 0.0, 0.05, 5), esses)

import matplotlib.pyplot as plt

labs = []
plots = []
T = 5
for tau in range(T-2):
	t = T - tau - 1
	vals = map(lambda s : black_scholes(s, 100., 0., 0.05, t), esses)
	p, = plt.plot(esses, vals)
	plots.append(p)
	labs.append(str(tau + 1))

plt.legend(plots, labs)
plt.show()




