problem_size=50
for epsilon in 1E-4 1E-2 1 1E2 1E4
	do
	sge_run --grid_submit=batch --grid_mem=4g --grid_priority=normal "./salp.sh "$problem_size" "$epsilon" > gen_salp_"$problem_size"_"$epsilon".txt"
	done
