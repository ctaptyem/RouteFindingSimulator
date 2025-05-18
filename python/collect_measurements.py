from itertools import product
import math
from pathlib import Path
import subprocess
from typing import Callable, Iterable
import numpy as np
import networkx as nx


RANDOM_GRAPH = Path("input_graphs/random_graph.txt")

def generate_graph(output_file: Path, node_count: int, avg_degree: float, undirected: bool, edge_seed: int, weight_seed: int) -> np.ndarray:
    edge_prop = 2.0*avg_degree/(node_count - 1)
    edge_rand = np.random.default_rng(edge_seed)
    weight_rand = np.random.default_rng(weight_seed)
    if edge_prop < 0.5:
        graph = np.ones((node_count, node_count)) * np.inf
        np.fill_diagonal(graph, 0.0)
        edge_count = int(edge_prop * ((node_count * node_count - node_count) / (2 if undirected else 1)))

        while edge_count:
            node_A, node_B = edge_rand.integers(0,node_count, size=2)
            while node_A == node_B or np.isfinite(graph[node_A, node_B]):
                node_A, node_B = edge_rand.integers(0,node_count, size=2)
            weight = max(weight_rand.exponential(1.0),0.0001)
            graph[node_A, node_B] = weight
            if undirected:
                graph[node_B, node_A] = weight
            edge_count-=1
    else:
        graph = np.zeros((node_count, node_count))
        if undirected:
            for i in range(node_count):
                for j in range(i+1, node_count):
                    weight = max(weight_rand.exponential(1.0),0.0001)
                    graph[i,j] = weight
            graph+=np.transpose(graph)
        else:
            for i in range(node_count):
                for j in range(node_count):
                    weight = max(weight_rand.exponential(1.0),0.0001)
                    graph[i,j] = weight
            np.fill_diagonal(graph, 0.0)
        anti_edge_count = int((1.0 - edge_prop) * ((node_count * node_count - node_count) / (2 if undirected else 1)))
        while anti_edge_count:
            node_A, node_B = edge_rand.integers(0,node_count, size=2)
            while node_A == node_B or np.isinf(graph[node_A, node_B]):
                node_A, node_B = edge_rand.integers(0,node_count, size=2)
            graph[node_A, node_B] = np.inf
            if undirected:
                graph[node_B, node_A] = np.inf
            anti_edge_count-=1
    np.savetxt(output_file, graph, delimiter=',', fmt='%s', header=f'{node_count},{avg_degree},{edge_prop},{undirected},{edge_seed},{weight_seed}', comments='')
    return graph

def decrease_edge(seed: int, graph: np.ndarray) -> str:
    rand = np.random.default_rng(seed)
    node_A, node_B = rand.integers(0,len(graph), size=2)
    while graph[node_A, node_B] == 0.0 or np.isinf(graph[node_A, node_B]):
        node_A, node_B = rand.integers(0,len(graph), size=2)
    return f"{node_A},{node_B},{graph[node_A, node_B] * rand.uniform(0.1,0.9)}"

def increase_edge(seed: int, graph: np.ndarray) -> str:
    rand = np.random.default_rng(seed)
    node_A, node_B = rand.integers(0,len(graph), size=2)
    while graph[node_A, node_B] == 0.0 or np.isinf(graph[node_A, node_B]):
        node_A, node_B = rand.integers(0,len(graph), size=2)
    return f"{node_A},{node_B},{graph[node_A, node_B] * rand.uniform(1.1,5.0)}"

def add_edge(seed: int, graph: np.ndarray) -> str:
    rand = np.random.default_rng(seed)
    node_A, node_B = rand.integers(0,len(graph), size=2)
    while np.isfinite(graph[node_A, node_B]):
        node_A, node_B = rand.integers(0,len(graph), size=2)
    return f"{node_A},{node_B},{rand.exponential(1.0)}"

def remove_edge(seed: int, graph: np.ndarray) -> str:
    rand = np.random.default_rng(seed)
    node_A, node_B = rand.integers(0,len(graph), size=2)
    while graph[node_A, node_B] == 0.0 or np.isinf(graph[node_A, node_B]):
        node_A, node_B = rand.integers(0,len(graph), size=2)
    return f"{node_A},{node_B},Infinity"

def remove_best_edge(seed: int, graph: np.ndarray) -> str:
    graph[~np.isfinite(graph)] = 0
    G = nx.from_numpy_array(graph)
    nx.config.backends.parallel.active = True
    nx.config.backends.parallel.n_jobs = 16
    betweenness = nx.edge_betweenness_centrality(G)
    node_A, node_B = max(betweenness, key=betweenness.get)
    return f'{int(node_A)},{int(node_B)},Infinity'

def get_command_to_run(output_file: Path, graph_path: Path, pe_grid_size: int, algorithm: str, edge_update: str = "", compress: bool = False) -> list[str]:
    command = ["java", "-jar", "/home/andrei/Dev/RouteFindingSimulator/target/RouteFindingSimulator-1.0-SNAPSHOT-jar-with-dependencies.jar", 
                    "-o", f"{output_file}",
                    "-p", f"{int(pe_grid_size)}",
                    "-a", f"{algorithm}",
                    "-u", "true",
                    # "-g", f"{int(node_count)},{float(edge_percent)},{int(seeds[0])},{int(seeds[1])}"
                    "-i", f"{graph_path}",
                    "-c" if compress else "",
                ]
    if len(edge_update) > 0:
        command.append("-e")
        command.append(edge_update)
    return command

def set_output_heading(output_file: Path, compression_heading: bool, overwrite: bool = False):
    if not Path.exists(output_file) or overwrite:
        with open(output_file,'w') as outfile:
            if compression_heading:
                outfile.write("algorithm,pe_grid_size,node_count,avg_degree,edge_percentage,undirected,edge_seed,weight_seed,compressed_node_count,compressed_degree,compressed_density,is_compressed,runtime,commcount,commvolume,runtime2,total_read,total_write\n")
            else:
                outfile.write("algorithm,pe_grid_size,node_count,avg_degree,edge_percentage,undirected,edge_seed,weight_seed,is_compressed,runtime,commcount,commvolume,runtime2,total_read,total_write\n")


def run_experiments(output_path: Path, graph_configs: dict[str, Iterable[int|float|Callable]], simulator_configs: dict[str, Iterable[int|str]], compress: bool, overwrite: bool = False):
    Path.mkdir(output_path, parents=True, exist_ok=True)
    subprocess.run(["mvn", "clean", "compile", "assembly:single"], capture_output=True)
    if "edge_updates" not in graph_configs:
        graph_configs["edge_updates"] = [lambda x,y: ""]
    progress = 0
    total = math.prod([len(v) for v in graph_configs.values()]) * math.prod([len(v) for v in simulator_configs.values()])
    edge_update_funcs = graph_configs["edge_updates"]
    del graph_configs['edge_updates']

    for edge_update_func in edge_update_funcs:
        output_file = output_path / f"{edge_update_func.__name__ if edge_update_func else "none"}.csv"
        set_output_heading(output_file, compress, overwrite)
        if "graph_paths" in graph_configs:
            for graph_path in graph_configs["graph_paths"]:
                edge_update_str = edge_update_func(None, None)
                for pe_grid_size, algorithm in [simulator_config for simulator_config in product(*[v for v in simulator_configs.values()])]:
                    progress+=1
                    print(f"\r{progress}/{total}, {pe_grid_size, algorithm} {' '*50}",end='')
                    subprocess.run(get_command_to_run(output_file, graph_path, pe_grid_size, algorithm, edge_update_str, compress=compress), capture_output=True)
            print()
        else:
            for node_count, avg_degree, seeds in [graph_config for graph_config in product(*[v for v in graph_configs.values()])]:
                graph = generate_graph(Path("/home/andrei/Dev/RouteFindingSimulator/input_graphs/random_graph.txt"), node_count, avg_degree, True, seeds[0], seeds[1])
                edge_update_str = edge_update_func(seeds[0]+seeds[1], graph)
                for pe_grid_size, algorithm in [simulator_config for simulator_config in product(*[v for v in simulator_configs.values()])]:
                    progress+=1
                    print(f"\r{progress}/{total} {seeds, node_count, avg_degree, pe_grid_size, algorithm, edge_update_str} {' '*50}",end='')
                    subprocess.run(get_command_to_run(output_file, RANDOM_GRAPH, pe_grid_size, algorithm, edge_update_str, compress=compress), capture_output=True)

    
        

def main():
    graph_configs = {
        "node_counts": [200,300,400,600,800,1000,1200],#np.round(10 * (2 ** np.linspace(2, 6, 6))).astype(int),
        "avg_degrees": [2.5],#[2, 2.25, 2.5, 3.0, 3.5],#np.round(np.linspace(2,4,5),5),#np.round(np.linspace(0.0005,0.0095,5),5), # [0.1, 0.3, 0.5, 0.7, 0.9]
        "random_seeds": [[722, 2905], [4776, 3417], [6117, 6371]],#[73, 6135], [8804, 1854], [8224, 2195],[480, 5607], [5764, 4112]],# [722, 2905], [4776, 3417], [6117, 6371], [9242, 7314], [4399, 4691]],
        "edge_updates": [decrease_edge, increase_edge, add_edge, remove_edge]
        # "graph_paths": [Path("/home/andrei/Dev/RouteFindingSimulator/input_graphs/cal.cedge"),Path("/home/andrei/Dev/RouteFindingSimulator/input_graphs/cal.cedge"),Path("/home/andrei/Dev/RouteFindingSimulator/input_graphs/cal.cedge"),Path("/home/andrei/Dev/RouteFindingSimulator/input_graphs/cal.cedge"),Path("/home/andrei/Dev/RouteFindingSimulator/input_graphs/cal.cedge")]
    }
    simulator_configs = {
        "pe_grid_sizes": [8,10],#[1,2,3,4,5,6],#[4,7,10,13,16], #[4,7,10,13,16],#2 ** np.linspace(0,4,5, dtype=int), #[1,2,4,8,16]
        "algorithms": ['dijkstra', 'foxotto', 'cannons'] # dynamic
    }
    output_name = "dynamic_random"
    output_path = Path(f"measurements/{output_name}/")
    Path.mkdir(output_path, parents=True, exist_ok=True)
    run_experiments(output_path, graph_configs, simulator_configs, True)


if __name__ == "__main__":
    main()