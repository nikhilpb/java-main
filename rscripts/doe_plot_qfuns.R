base_name <- '../results/doe/qfun-dim-10-'
time_periods <- 10


q <- read.csv(paste(base_name, 'tp-1.csv', sep = ''), header=FALSE)
plot(q, type = 'l', xlab = 'lambda', ylab = 'q value')

for (t in seq(2, time_periods-1)) {
  q <- read.csv(paste(base_name, 'tp-', toString(t), '.csv', sep = ''), header=FALSE)
  lines(q)
}




