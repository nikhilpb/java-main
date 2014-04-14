dim <- 10
base_name <-paste('../results/doe/qfun-dim-', toString(dim), '-', sep = '')  
time_periods <- 10


q <- read.csv(paste(base_name, 'tp-1.csv', sep = ''), header=FALSE)
plot(q, type = 'l', xlab = 'lambda', ylab = 'q value')

for (t in seq(2, time_periods-1)) {
  q <- read.csv(paste(base_name, 'tp-', toString(t), '.csv', sep = ''), header=FALSE)
  lines(q)
}

min_file_name <- paste('../results/doe/qfun-myopic-min-dim-', toString(dim), '.csv', sep = '') 
min_points <- read.csv(min_file_name, header=FALSE)
plot(min_points, xlab='time period', ylab='qfun minimum', main=paste('dimension',toString(dim)))




