from itertools import product
import subprocess
import numpy as np

def get_command_to_run(output_file, seeds, node_count, edge_percent, pe_grid_size, algorithm):
    return ["java", "-jar", "/home/andrei/Dev/RouteFindingSimulator/target/RouteFindingSimulator-1.0-SNAPSHOT-jar-with-dependencies.jar", 
                "-o", f"{output_file}",
                "-p", f"{int(pe_grid_size)}",
                "-a", f"{algorithm}",
                "-u", "true",
                "-g", f"{int(node_count)},{float(edge_percent)},{int(seeds[0])},{int(seeds[1])}"]

def main():
    with open("/home/andrei/Dev/RouteFindingSimulator/measurements/test_commtime3.csv",'w') as outfile:
        outfile.write("algorithm,pe_grid_size,node_count,edge_percentage,undirected,weight_mean,weight_std,edge_seed,weight_seed,runtime,commtime,commcount,total_read,total_write\n")
    random_seeds = [[73, 6135], [8804, 1854], [8224, 2195], ]#[480, 5607], [5764, 4112], [722, 2905], [4776, 3417], [6117, 6371], [9242, 7314], [4399, 4691]]
    node_counts = np.round(10 * (2 ** np.linspace(0, 5, 14))) #[10,40,160,640]
    edge_percentages = np.round(np.linspace(0.05,0.95,5),5) # [0.1, 0.3, 0.5, 0.7, 0.9]
    pe_grid_sizes = 2 ** np.linspace(0,4,5) #[1,2,4,8,16]
    algorithms = ['dijkstra', 'foxotto', 'cannons']
    progress = 0
    configs = [config for config in product(random_seeds, node_counts, edge_percentages, pe_grid_sizes, algorithms)]
    total = len(configs)
    print("Starting...")
    subprocess.run(["mvn", "clean", "compile", "assembly:single"], capture_output=True)
    for seeds, node_count, edge_percent, pe_grid_size, algorithm in configs:
        result = subprocess.run(get_command_to_run("/home/andrei/Dev/RouteFindingSimulator/measurements/test_commtime3.csv", seeds,node_count,edge_percent,pe_grid_size,algorithm), capture_output=True)
        progress+=1
        print(f"\r{progress}/{total} {seeds, node_count, edge_percent, pe_grid_size, algorithm} {' '*50}",end='')
    print()


if __name__ == "__main__":
    main()