import matplotlib.pyplot as plt
import functools 

N = 100

def plot_operator_time(): 
	file_name = "operadores_time.txt"
	alg1 = []
	alg2 = []
	alg3 = []
	with open(file_name, 'r') as f:
		for i in range(100):
			line = f.readline()
			d = [float(c)*1000.0 for c in line.split()]
			alg1.append(d[0])
			alg2.append(d[1])
			alg3.append(d[2])
	data = [alg1, alg2, alg3]
	plt.boxplot(data, labels=["Move + Swap", "Move", "Swap"])
	plt.title("Execution time for each of the 'operators' combinations");
	plt.ylabel("Execution time (ms)");
	plt.show()

def plot_operator_improvement(): 
	file_name = "operadores_improvement.txt"
	alg1 = []
	alg2 = []
	alg3 = []
	with open(file_name, 'r') as f:
		for i in range(100):
			line = f.readline()
			d = [float(c) for c in line.split()]
			alg1.append(d[0])
			alg2.append(d[1])
			alg3.append(d[2])
	data = [alg1, alg2, alg3]
	plt.boxplot(data, labels=["Move + Swap", "Move", "Swap"])
	plt.title("Improvement of the cost for each of the 'operators' combinations");
	plt.ylabel("Improvement of the cost (euros)");
	plt.show()

def plot_generator_time(): 
	file_name = "generator_time.txt"
	alg1 = []
	alg2 = []
	with open(file_name, 'r') as f:
		for i in range(100):
			line = f.readline()
			d = [float(c)*1000000.0 for c in line.split()]
			alg1.append(d[0])
			alg2.append(d[1])
	data = [alg1, alg2]
	plt.boxplot(data, labels=["Greedy time with revisiting", "Enhanced greedy time with revisiting"])
	plt.title("Execution time for each of the generation algorithms of the initial state");
	plt.ylabel("Execution time (µs)");
	plt.show()

def plot_generator_improvement(): 
	file_name = "generator_improvement.txt"
	alg1 = []
	alg2 = []
	with open(file_name, 'r') as f:
		for i in range(100):
			line = f.readline()
			d = [float(c) for c in line.split()]
			alg1.append(d[0])
			alg2.append(d[1])
	data = [alg1, alg2]
	plt.boxplot(data, labels=["Priority", "Priority and Weight"])
	plt.title("Improvement of the cost\nfor each of the generation algorithms of the initial state");
	plt.ylabel("Improvement of the cost (euros)");
	plt.show()

def plot_generator_cost(): 
	file_name = "generator_cost.txt"
	alg1 = []
	alg2 = []
	with open(file_name, 'r') as f:
		for i in range(100):
			line = f.readline()
			d = [float(c) for c in line.split()]
			alg1.append(d[0])
			alg2.append(d[1])
	data = [alg1, alg2]
	plt.boxplot(data, labels=["Priority", "Priority and Weight"])
	plt.title("Initial cost for each of the generation algorithms of the initial state");
	plt.ylabel("Initial cost (euros)");
	plt.show()

def plot_sa_k_lambda(): 
	file_name = "sa_values.txt"
	ks = { 1: 0, 5: 1, 25: 2, 125: 3 }
	ls = { 0.0001: 0, 0.001: 1, 0.01: 2, 0.1: 3, 1: 4 }
	data = [[[] for i in range(len(ls))] for j in range(len(ks))]

	with open(file_name, 'r') as f:
		for line in f:
			line = f.readline()
			d = [c for c in line.split()]
			k = int(d[0])	# k value
			l = float(d[1])	# lambda value
			i = float(d[2])	# improvement
			data[ks[k]][ls[l]].append(i)

	for k in ks:
		plt.boxplot(data[ks[k]], labels=[str(l) for l in ls])
		plt.title("Improvement for k={} and different values of lambda".format(k));
		plt.ylim((90, 350))
		plt.ylabel("Improvement (euros)");
		plt.xlabel("Values of lambda");
		plt.savefig('boxplot_k{}.png'.format(k))
		plt.show()

	for l in ls:
		d = []
		for k in ks:
			d.append(data[ks[k]][ls[l]])
		plt.boxplot(d, labels=[str(k) for k in ks])
		plt.title("Improvement for lambda={} and different values of k".format(l));
		plt.ylim((90, 320))
		plt.ylabel("Improvement (euros)");
		plt.xlabel("Values of k");
		plt.savefig('boxplot_lambda{}.png'.format(l))
		plt.show()

def plot_sa_execution_time(): 
	file_name = "sa_execution_time.txt"
	ks = { 1: 0, 5: 1, 25: 2, 125: 3 }
	ls = { 0.0001: 0, 0.001: 1, 0.01: 2, 0.1: 3, 1: 4 }
	data = [[[] for i in range(len(ls))] for j in range(len(ks))]

	with open(file_name, 'r') as f:
		for line in f:
			d = [c for c in line.split()]
			k = int(d[0])	# k value
			l = float(d[1])	# lambda value
			i = float(d[2]) * 1000.0	# improvement
			data[ks[k]][ls[l]].append(i)

	for k in ks:
		plt.boxplot(data[ks[k]], labels=[str(l) for l in ls])
		plt.title("Execution time for k={} and different values of lambda".format(k));
		plt.ylabel("Time (ms)");
		plt.xlabel("Values of lambda");
		plt.savefig('sa_execution_time_k{}.png'.format(k))
		plt.show()

	for l in ls:
		d = []
		for k in ks:
			d.append(data[ks[k]][ls[l]])
		plt.boxplot(d, labels=[str(k) for k in ks])
		plt.title("Execution time for lambda={} and different values of k".format(l));
		plt.ylabel("Time (ms)");
		plt.xlabel("Values of k");
		plt.savefig('sa_execution_time_l{}.png'.format(l))
		plt.show()

def plot_proportion(): 
	file_name = "proportion_study.txt"
	ps = {}
	g_times = [[] for i in range(10)]
	e_times = [[] for i in range(10)]
	idx = 0
	with open(file_name, 'r') as f:
		for line in f:
			line = f.readline()
			d = [c for c in line.split()]
			p = str(d[0])				# proportion
			g = float(d[1])*1000		# generation time
			e = float(d[2])*1000		# execution time
			if p not in ps:
				ps[p] = idx
				idx += 1
			g_times[ps[p]].append(g)
			e_times[ps[p]].append(e)
	
	plt.plot([p for p in ps], [sum(l)/len(l)*1000 for l in g_times])
	plt.title("Evolution of the time for generating the initial solution");
	plt.ylabel("Time (µs)");
	plt.xlabel("Proportions");
	plt.savefig("prop_gen.png")
	plt.show()

	plt.plot([p for p in ps], [sum(l)/len(l) for l in e_times])
	plt.title("Evolution of the execution time");
	plt.ylabel("Time (ms)");
	plt.xlabel("Proportions");
	plt.savefig("prop_exec.png")
	plt.show()

def plot_proportion_cost(): 
	file_name = "proportion_cost_study.txt"
	ps = {}
	g_times = [[] for i in range(10)]
	e_times = [[] for i in range(10)]
	t_cost = [[] for i in range(10)]
	idx = 0
	with open(file_name, 'r') as f:
		for line in f:
			line = f.readline()
			d = [c for c in line.split()]
			p = str(d[0])				# proportion
			g = float(d[1])		# generation time
			e = float(d[2])		# execution time
			if p not in ps:
				ps[p] = idx
				idx += 1
			g_times[ps[p]].append(g)
			e_times[ps[p]].append(e)
			t_cost[ps[p]].append(g+e)
	
	plt.plot([p for p in ps], [sum(l)/len(l) for l in g_times])
	plt.title("Evolution of the transport cost");
	plt.ylabel("Cost (euros)");
	plt.xlabel("Proportions");
	plt.savefig("prop_tran_cost.png")
	plt.show()

	plt.plot([p for p in ps], [sum(l)/len(l) for l in e_times])
	plt.title("Evolution of the storage cost");
	plt.ylabel("Cost (euros)");
	plt.xlabel("Proportions");
	plt.savefig("prop_stor_cost.png")
	plt.show()

	plt.plot([p for p in ps], [sum(l)/len(l) for l in t_cost])
	plt.title("Evolution of the total cost");
	plt.ylabel("Cost (euros)");
	plt.xlabel("Proportions");
	plt.savefig("prop_total_cost.png")
	plt.show()


def plot_packages(): 
	file_name = "npack_study.txt"
	ps = {}
	g_times = [[] for i in range(100, 650, 50)]
	e_times = [[] for i in range(100, 650, 50)]
	idx = 0
	with open(file_name, 'r') as f:
		for line in f:
			line = f.readline()
			d = [c for c in line.split()]
			p = str(d[0])				# n packages
			g = float(d[1])*1000		# generation time
			e = float(d[2])*1000		# execution time
			if p not in ps:
				ps[p] = idx
				idx += 1
			g_times[ps[p]].append(g)
			e_times[ps[p]].append(e)
	
	plt.plot([p for p in ps], [sum(l)/len(l)*1000 for l in g_times])
	plt.title("Evolution of the time for generating the initial solution");
	plt.ylabel("Time (µs)");
	plt.xlabel("Number of packages");
	plt.savefig("npaq_gen.png")
	plt.show()

	plt.plot([p for p in ps], [sum(l)/len(l) for l in e_times])
	plt.title("Evolution of the execution time");
	plt.ylabel("Time (ms)");
	plt.xlabel("Number of packages");
	plt.savefig("npaq_exec.png")
	plt.show()


def happiness_cost_study(): 
	file_name = "happy_study.txt"
	ps = {'0': 0}
	for i in range(1, 10, 1):
		ps[str(i/10.0)] = i
	ps['1'] = 10
	print(ps)
	idx = 0
	ets = [[] for i in range(11)]		# execution time
	tcs = [[] for i in range(11)]		# transport cost
	scs = [[] for i in range(11)]		# storage cost
	ttc = [[] for i in range(11)]		# total cost
	hpn = [[] for i in range(11)]		# total cost
	with open(file_name, 'r') as f:
		for line in f:
			line = f.readline()
			d = [c for c in line.split()]
			p = str(d[0])				# proportion
			e = float(d[1])*1000		# execution time
			s = float(d[2])				# storage cost
			t = float(d[3])				# transport cost
			if p not in ps:
				ps[p] = idx
				idx += 1
				print(p)
			ets[ps[p]].append(e)
			tcs[ps[p]].append(t)
			scs[ps[p]].append(s)
			ttc[ps[p]].append(t + s)
			hpn[ps[p]].append(float(d[5]))

	plt.plot([p for p in ps], [sum(l)/len(l) for l in ets])
	plt.title("Evolution of the execution time");
	plt.ylabel("Time (ms)");
	plt.xlabel("Heuristic proportion");
	plt.savefig("happiness_exec_time.png")
	plt.show()

	plt.plot([p for p in ps], [sum(l)/len(l) for l in scs])
	plt.title("Evolution of the storage cost");
	plt.ylabel("Cost (euros)");
	plt.xlabel("Heuristic proportion");
	plt.savefig("happiness_storage_cost.png")
	plt.show()

	plt.plot([p for p in ps], [sum(l)/len(l) for l in tcs])
	plt.title("Evolution of the transport cost");
	plt.ylabel("Cost (euros)");
	plt.xlabel("Heuristic proportion");
	plt.savefig("happiness_tran_cost.png")
	plt.show()

	plt.plot([p for p in ps], [sum(l)/len(l) for l in ttc])
	plt.title("Evolution of the total cost");
	plt.ylabel("Cost (euros)");
	plt.xlabel("Heuristic proportion");
	plt.savefig("happiness_total_cost.png")
	plt.show()

	plt.plot([p for p in ps], [sum(l)/len(l) for l in hpn])
	plt.title("Evolution of the happiness metric");
	plt.ylabel("Happiness");
	plt.xlabel("Heuristic proportion");
	plt.savefig("happiness_happy_score.png")
	plt.show()


def sa_happiness_cost_study(): 
	file_name = "sa_happy_study.txt"
	ps = {'0': 0}
	for i in range(1, 10, 1):
		ps[str(i/10.0)] = i
	ps['1'] = 10
	print(ps)
	idx = 0
	ets = [[] for i in range(11)]		# execution time
	tcs = [[] for i in range(11)]		# transport cost
	scs = [[] for i in range(11)]		# storage cost
	ttc = [[] for i in range(11)]		# total cost
	hpn = [[] for i in range(11)]		# total cost
	with open(file_name, 'r') as f:
		for line in f:
			line = f.readline()
			d = [c for c in line.split()]
			p = str(d[0])				# proportion
			e = float(d[1])*1000		# execution time
			s = float(d[2])				# storage cost
			t = float(d[3])				# transport cost
			if p not in ps:
				ps[p] = idx
				idx += 1
				print(p)
			ets[ps[p]].append(e)
			tcs[ps[p]].append(t)
			scs[ps[p]].append(s)
			ttc[ps[p]].append(t + s)
			hpn[ps[p]].append(float(d[5]))

	plt.plot([p for p in ps], [sum(l)/len(l) for l in ets])
	plt.title("Evolution of the execution time");
	plt.ylabel("Time (ms)");
	plt.xlabel("Heuristic proportion");
	plt.savefig("sa_happiness_exec_time.png")
	plt.show()

	plt.plot([p for p in ps], [sum(l)/len(l) for l in scs])
	plt.title("Evolution of the storage cost");
	plt.ylabel("Cost (euros)");
	plt.xlabel("Heuristic proportion");
	plt.savefig("sa_happiness_storage_cost.png")
	plt.show()

	plt.plot([p for p in ps], [sum(l)/len(l) for l in tcs])
	plt.title("Evolution of the transport cost");
	plt.ylabel("Cost (euros)");
	plt.xlabel("Heuristic proportion");
	plt.savefig("sa_happiness_tran_cost.png")
	plt.show()

	plt.plot([p for p in ps], [sum(l)/len(l) for l in ttc])
	plt.title("Evolution of the total cost");
	plt.ylabel("Cost (euros)");
	plt.xlabel("Heuristic proportion");
	plt.savefig("sa_happiness_total_cost.png")
	plt.show()

	plt.plot([p for p in ps], [sum(l)/len(l) for l in hpn])
	plt.title("Evolution of the happiness metric");
	plt.ylabel("Happiness");
	plt.xlabel("Heuristic proportion");
	plt.savefig("sa_happiness_happy_score.png")
	plt.show()


def hc_sa_comparative():
	exec_times = []
	total_costs = []
	happiness = []
	for file_name in ["hc_comparative.txt", "as_comparative.txt"]:
		exec_time = []
		total_cost = []
		happy = []
		with open(file_name, 'r') as f:
			for line in f:
				line = f.readline()
				d = [c for c in line.split()]
				exec_time.append(float(d[0])*1000.0)
				total_cost.append(float(d[1]))
				happy.append(float(d[2]))
		exec_times.append(exec_time)
		total_costs.append(total_cost)
		happiness.append(happy)
	plt.boxplot(exec_times, labels=["Hill Climbing", "Simulated Annealing"])
	plt.title("Execution time comparison");
	plt.ylabel("Execution time (ms)");
	plt.savefig("hc_sa_execution_time.png")
	plt.show()

	plt.boxplot(total_costs, labels=["Hill Climbing", "Simulated Annealing"])
	plt.title("Total cost comparison");
	plt.ylabel("Total cost (euros)");
	plt.savefig("hc_sa_total_cost.png")
	plt.show()

	plt.boxplot(happiness, labels=["Hill Climbing", "Simulated Annealing"])
	plt.title("Happiness metric comparison");
	plt.ylabel("Happiness metric");
	plt.savefig("hc_sa_happinesse.png")
	plt.show()


# hc_sa_comparative()
hc_sa_comparative()