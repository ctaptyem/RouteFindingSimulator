package uk.ac.cam.cl.ac2499;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.algorithms.CodeBlock;
import uk.ac.cam.cl.ac2499.algorithms.SequentialMatrixMult;
import uk.ac.cam.cl.ac2499.algorithms.SequentialDijkstra;
import uk.ac.cam.cl.ac2499.algorithms.Cannons.CannonsMCU;
import uk.ac.cam.cl.ac2499.algorithms.Dijkstra.DijkstraMCU;
import uk.ac.cam.cl.ac2499.algorithms.Dynamic.DynamicMCU;
import uk.ac.cam.cl.ac2499.algorithms.FoxOtto.FoxOttoMCU;
import uk.ac.cam.cl.ac2499.graph.CompressedGraph;
import uk.ac.cam.cl.ac2499.graph.Graph;
import uk.ac.cam.cl.ac2499.simulator.Memory;
import uk.ac.cam.cl.ac2499.simulator.Simulator;


public class Main {
    
    public static void execute_with_arguments(String[] args) throws IOException, ExecutionException, InterruptedException {
        Options options = new Options();
        // Option input = new Option("i", "input", true, "input file path");
        // Option graph = new Option("g", "graph", true, "input random graph parameters");
        options.addOption("i", "input", true, "The file path of the input graph");
        options.addOption("g", "graph", true, "The input random graph parameters (i.e. node_count,avg_degree,edge_seed,weight_seed)");
        options.addOption("u", "undirected", true, "Whether the graph is undirected");
        options.addOption("c", "compress", false, "Whether to compress the graph before executing the algorithms");
        options.addOption("e", "edge_update", true, "The input parameters describing an edge to update (i.e. node_A,node_B,new_weight)");
        Option output = new Option("o", "output", true, "The file path for measurement output");
        output.setRequired(true);
        options.addOption(output);
        Option peGridSize = new Option("p", "peGridSize", true, "The length of the side of the processor array");
        peGridSize.setRequired(true);
        options.addOption(peGridSize);
        Option algorithm = new Option("a", "algorithm", true, "The algorithm to execute ('dijkstra', 'foxotto', 'cannons', or 'dynamic')");
        algorithm.setRequired(true);
        options.addOption(algorithm);
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("RouteFindingSimulator", options);
            System.exit(1);
        }
        
        Graph g = null;

        if (cmd.hasOption("input")) {
            g = new Graph(cmd.getOptionValue("input"), cmd.hasOption("undirected"));
        } else if (cmd.hasOption("graph")) {
            String[] graph_params = cmd.getOptionValue("graph").split(",");
            int node_count = Integer.parseInt(graph_params[0]);
            double avg_degree = Double.parseDouble(graph_params[1]);
            int edge_seed = Integer.parseInt(graph_params[2]);
            int weight_seed = Integer.parseInt(graph_params[3]);
            g = new Graph(node_count, avg_degree, cmd.hasOption("undirected"), edge_seed, weight_seed);
        } else {
            System.out.println("An input file or random graph parameters must be specified (-i <input file> or -g <parameters>)");
            System.exit(1);
        }

        if (cmd.hasOption("compress")) {
            g = new CompressedGraph(g);
        }

        CodeBlock algo = null;
        boolean is_dynamic = false;

        Memory shared_memory = new Memory();


        if (cmd.getOptionValue("algorithm").equals("dijkstra")) {
            algo = new DijkstraMCU();
        } else if (cmd.getOptionValue("algorithm").equals("foxotto")) {
            algo = new FoxOttoMCU();
        } else if (cmd.getOptionValue("algorithm").equals("cannons")) {
            algo = new CannonsMCU();
        } else if (cmd.getOptionValue("algorithm").equals("dynamic")) {
            if (!cmd.hasOption("edge_update")) {
                System.out.println("If using the dynamic graph algorithm, you must specify an update to an edge with -e.");
                System.exit(1);
            }
            Simulator dijkstra_sim = new Simulator(Integer.parseInt(cmd.getOptionValue("peGridSize")), g, new DijkstraMCU(), shared_memory);
            dijkstra_sim.execute();
            is_dynamic = true;
            algo = new DynamicMCU();
        } else {
            System.out.println("Please specify a valid algorithm ('dijkstra', 'foxotto', 'cannons', or 'dynamic')");
            System.exit(1);
        }

        if (cmd.hasOption("edge_update")) {
            String[] edge_update_params = cmd.getOptionValue("edge_update").split(",");
            int from_node = Integer.parseInt(edge_update_params[0]);
            int to_node = Integer.parseInt(edge_update_params[1]);
            double new_weight = Double.parseDouble(edge_update_params[2]);
            shared_memory.set("from_node", from_node);
            shared_memory.set("to_node", to_node);
            shared_memory.set("new_weight", new SimpleMatrix(new double[][]{{new_weight}}));
            if (cmd.hasOption("undirected")) {
                shared_memory.set("undirected", 1);
            } else {
                shared_memory.set("undirected", 0);
            }
            if (!is_dynamic) {
                g.update_edge(from_node, to_node, new_weight);
            }
        }

        Simulator s = new Simulator(Integer.parseInt(cmd.getOptionValue("peGridSize")), g, algo, shared_memory);
        s.execute();
        s.record_measurement(cmd.getOptionValue("output"));
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        execute_with_arguments(args);
    }
}
