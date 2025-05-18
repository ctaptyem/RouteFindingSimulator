# RouteFindingSimulator
This is a project for Part II of the Cambridge Computer Science Tripos.

## Project Aim
The goal of this project is to explore the performance of APSP algorithms in a parallel execution environment. In particular, I focus on the time spent on computation and communication and how these quantities scale with access to more processing elements or larger input datasets. To this end, I develop a simulator which supports the execution of a variety of parallel algorithms and integrates tracking for computation and communication behavior. Using the simulator, I implement and evaluate Dijkstra's algorithm, Cannon's algorithm, the Fox-Otto algorithm, and the MA algorithm on varied input graphs to capture a breadth of approaches for the static and dynamic problem variations. In addition to examining the algorithms' performance on real-world road network datasets, I incorporate randomly generated graphs into the evaluation to build a holistic characterization of each algorithm's behavior. 

## Setup
This project relies on both Java and Python libraries in order to deliver its results. A Maven `pom.xml` is provided for installing the Java packages (JUnit Testing and the Efficient Java Matrix Library), so executing `mvn install` downloads and sets up all the required Java libraries. The `.jar` file for the Java simulator can be built by executing:

```
mvn clean compile assembly:single
```

This will create the `target/RouteFindingSimulator-1.0-SNAPSHOT-jar-with-dependencies.jar` file which facilitates the command line interactions described in "Usage".

The exact versions of the Python packages used (and their dependencies) can be found in `python_requirements.txt`. They can be installed into a Python virtual environment as follows:

```
// Create virtual environment
python -m venv ./<virtual environment name>

// Activate virtual environment
source ./<virtual environment>/bin/activate

// Install packages
pip install -r python_requirements.txt

// To deactivate the Python environment when finished with the project:
deactivate
```

## Usage
The `Main.java` file provides a command-line interface for interacting with the simulator. The input arguments are shown below:

```
usage: RouteFindingSimulator
 -a,--algorithm <arg>     The algorithm to execute ('dijkstra', 'foxotto',
                          'cannons', or 'dynamic')
 -c,--compress            Whether to compress the graph before executing
                          the algorithms
 -e,--edge_update <arg>   The input parameters describing an edge to
                          update (i.e. node_A,node_B,new_weight)
 -g,--graph <arg>         The input random graph parameters (i.e.
                          node_count,avg_degree,edge_seed,weight_seed)
 -i,--input <arg>         The file path of the input graph
 -o,--output <arg>        The file path for measurement output
 -p,--peGridSize <arg>    The length of the side of the processor array
 -u,--undirected <arg>    Whether the graph is undirected
 ```

 The command line input to the simulator executes a single run with the specified parameters. I have automated the process of executing simulator runs with different configurations in `collect_measurements.py`. The script is set up as desired, with all combinations of simulator and graph configuration options, and executed with `python python/collect_measuremnts.py`.

 Some code for generating the plots used in the report can be found in `plot_graphs.py`, used in a similar way to `collect_measurements.py`. 

 Please note that these Python scripts generate files in the `measurements/` and `plots/` directories.