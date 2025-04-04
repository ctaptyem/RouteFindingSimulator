from itertools import product
import math
from pathlib import Path
import subprocess
from typing import Callable, Iterable
import numpy as np

def generate_graph(output_file: Path, node_count: int, edge_prop: float, undirected: bool, edge_seed: int, weight_seed: int) -> np.ndarray:
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
    np.savetxt(output_file, graph, delimiter=',', fmt='%s', header=f'{node_count},{edge_prop},{undirected},{edge_seed},{weight_seed}', comments='')
    return graph

def get_command_to_run(output_file: Path, pe_grid_size: int, algorithm: str, edge_update: str = "") -> list[str]:
    command = ["java", "-jar", "/home/andrei/Dev/RouteFindingSimulator/target/RouteFindingSimulator-1.0-SNAPSHOT-jar-with-dependencies.jar", 
                    "-o", f"{output_file}",
                    "-p", f"{int(pe_grid_size)}",
                    "-a", f"{algorithm}",
                    "-u", "true",
                    # "-g", f"{int(node_count)},{float(edge_percent)},{int(seeds[0])},{int(seeds[1])}"
                    "-i", "/home/andrei/Dev/RouteFindingSimulator/testing/input/random_graph.txt",
                ]
    if len(edge_update) > 0:
        command.append("-e")
        command.append(edge_update)
    return command
    

def run_static_experiments(output_file: Path, graph_configs: dict[str, Iterable[int|float]], simulator_configs: dict[str, Iterable[int|str]]):
    with open(output_file, 'w') as outfile:
        outfile.write("algorithm,pe_grid_size,node_count,edge_percentage,undirected,edge_seed,weight_seed,runtime,commtime,commcount,commvolume,total_read,total_write\n")
    progress = 0
    total = math.prod([len(v) for v in graph_configs.values()]) * math.prod([len(v) for v in simulator_configs.values()])
    print("Starting...")
    subprocess.run(["mvn", "clean", "compile", "assembly:single"], capture_output=True)
    for node_count, edge_prop, seeds in [graph_config for graph_config in product(*[v for v in graph_configs.values()])]:
        generate_graph(Path("/home/andrei/Dev/RouteFindingSimulator/testing/input/random_graph.txt"), node_count, edge_prop, True, seeds[0], seeds[1])
        for pe_grid_size, algorithm in [simulator_config for simulator_config in product(*[v for v in simulator_configs.values()])]:
            result = subprocess.run(get_command_to_run(output_file, pe_grid_size,algorithm), capture_output=True)
            progress+=1
            print(f"\r{progress}/{total} {seeds, node_count, edge_prop, pe_grid_size, algorithm} {' '*50}",end='')
    print()

def run_dynamic_experiments(output_file: Path, graph_configs: dict[str, Iterable[int|float]], simulator_configs: dict[str, Iterable[int|str]], edge_update: Callable[[int, np.ndarray], str]):
    with open(output_file,'w') as outfile:
        outfile.write("algorithm,pe_grid_size,node_count,edge_percentage,undirected,edge_seed,weight_seed,runtime,commtime,commcount,commvolume,total_read,total_write\n")
    progress = 0
    total = math.prod([len(v) for v in graph_configs.values()]) * math.prod([len(v) for v in simulator_configs.values()])
    print("Starting...")
    subprocess.run(["mvn", "clean", "compile", "assembly:single"], capture_output=True)
    for node_count, edge_prop, seeds in [graph_config for graph_config in product(*[v for v in graph_configs.values()])]:
        graph = generate_graph(Path("/home/andrei/Dev/RouteFindingSimulator/testing/input/random_graph.txt"), node_count, edge_prop, True, seeds[0], seeds[1])
        edge_update_str = edge_update(seeds[0]+seeds[1], graph)
        for pe_grid_size, algorithm in [simulator_config for simulator_config in product(*[v for v in simulator_configs.values()])]:
            command = get_command_to_run(output_file, pe_grid_size, algorithm, edge_update_str)
            result = subprocess.run(command, capture_output=True)
            progress+=1
            print(f"\r{progress}/{total} {seeds, node_count, edge_prop, pe_grid_size, algorithm, edge_update_str} {' '*50}",end='')
    print()

def run_all_dynamic_experiments(output_path: Path, output_name:str, graph_configs: dict[str, Iterable[int|float]], simulator_configs: dict[str, Iterable[int|str]]):
    def decrease_edge(seed: int, graph: np.ndarray) -> tuple[int, int, float]:
        rand = np.random.default_rng(seed)
        node_A, node_B = rand.integers(0,len(graph), size=2)
        while graph[node_A, node_B] == 0.0 or np.isinf(graph[node_A, node_B]):
            node_A, node_B = rand.integers(0,len(graph), size=2)
        return f"{node_A},{node_B},{graph[node_A, node_B] * rand.uniform(0.1,0.9)}"
    
    def increase_edge(seed: int, graph: np.ndarray) -> tuple[int, int, float]:
        rand = np.random.default_rng(seed)
        node_A, node_B = rand.integers(0,len(graph), size=2)
        while graph[node_A, node_B] == 0.0 or np.isinf(graph[node_A, node_B]):
            node_A, node_B = rand.integers(0,len(graph), size=2)
        return f"{node_A},{node_B},{graph[node_A, node_B] * rand.uniform(1.1,5.0)}"
    
    def add_edge(seed: int, graph: np.ndarray) -> tuple[int, int, float]:
        rand = np.random.default_rng(seed)
        node_A, node_B = rand.integers(0,len(graph), size=2)
        while np.isfinite(graph[node_A, node_B]):
            node_A, node_B = rand.integers(0,len(graph), size=2)
        return f"{node_A},{node_B},{rand.exponential(1.0)}"
    
    def remove_edge(seed: int, graph: np.ndarray) -> tuple[int, int, float]:
        rand = np.random.default_rng(seed)
        node_A, node_B = rand.integers(0,len(graph), size=2)
        while graph[node_A, node_B] == 0.0 or np.isinf(graph[node_A, node_B]):
            node_A, node_B = rand.integers(0,len(graph), size=2)
        return f"{node_A},{node_B},Infinity"
    
    run_dynamic_experiments(output_path / f'{output_name}_decrease_edge.csv', graph_configs, simulator_configs, decrease_edge)
    run_dynamic_experiments(output_path / f'{output_name}_increase_edge.csv', graph_configs, simulator_configs, increase_edge)
    run_dynamic_experiments(output_path / f'{output_name}_add_edge.csv', graph_configs, simulator_configs, add_edge)
    run_dynamic_experiments(output_path / f'{output_name}_remove_edge.csv', graph_configs, simulator_configs, remove_edge)




def main():
    output_name = "first_dynamic"

    graph_configs = {
        "node_counts": np.round(10 * (2 ** np.linspace(0, 5, 14))).astype(int), #[10,40,160,640]
        "edge_props": np.round(np.linspace(0.05,0.95,5),5), # [0.1, 0.3, 0.5, 0.7, 0.9]
        "random_seeds": [[73, 6135], [8804, 1854], [8224, 2195], ]#[480, 5607], [5764, 4112], [722, 2905], [4776, 3417], [6117, 6371], [9242, 7314], [4399, 4691]],
    }
    simulator_configs = {
        "pe_grid_sizes": 2 ** np.linspace(0,4,5, dtype=int), #[1,2,4,8,16]
        "algorithms": ['dijkstra', 'foxotto', 'cannons', 'dynamic']
    }

    output_path = Path(f"/home/andrei/Dev/RouteFindingSimulator/measurements/{output_name}/")
    Path.mkdir(output_path, parents=True, exist_ok=True)

    run_all_dynamic_experiments(output_path, output_name, graph_configs, simulator_configs)

if __name__ == "__main__":
    main()