package uk.ac.cam.cl.ac2499;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.algorithms.CodeBlock;
import uk.ac.cam.cl.ac2499.algorithms.Cannons.CannonsMCU;
import uk.ac.cam.cl.ac2499.algorithms.Dijkstra.DijkstraMCU;
import uk.ac.cam.cl.ac2499.algorithms.Dynamic.DynamicMCU;
import uk.ac.cam.cl.ac2499.algorithms.FoxOtto.FoxOttoMCU;
import uk.ac.cam.cl.ac2499.simulator.Memory;
import uk.ac.cam.cl.ac2499.simulator.Simulator;


public class Main {

    public static void run_simulator() throws IOException, ExecutionException, InterruptedException {
        // Graph g = new Graph("testing/input/zerod_example_2.txt", true); //new Graph("testing/input/OL.cedge");
        // Graph g = new Graph(200,0.75,true,50.0,20.0,9063,3609);
        Graph g = new Graph(20,0.900000,true, 9242, 7314);
        // System.out.println(g.adjacency);
        // for (int i = 0; i < g.length; i++) {
        //     for (int j = 0; j < g.length; j++) {
        //         if (Double.isFinite(g.adjacency.get(i,j))) {
        //             System.out.printf("%f, ", g.adjacency.get(i,j));
        //         } else {
        //             System.out.printf("-8.0, ");
        //         }
        //     }
        //     System.out.println();
        // }

        

        System.out.println("Loaded graph...");
        int p = 2;
        Simulator s;

        System.out.println("Starting Dijkstra's algorithm...");
        s = new Simulator(p, g, new DijkstraMCU(), new Memory());
        s.execute();
        s.process_output("dijkstra");
        System.out.println("Finished Dijkstra's algorithm");
        // SimpleMatrix dijkstra_dist = s.get_shared_memory().get("output_dist");

        // System.out.println("Starting Cannon's algorithm...");
        // s = new Simulator(p, g, new CannonsMCU(), new Memory());
        // s.execute();
        // s.process_output("cannons");
        // System.out.println("Finished Cannon's algorithm");
        // System.out.println("Starting Fox-Otto's algorithm...");
        // s = new Simulator(p, g, new FoxOttoMCU(), new Memory());
        // s.execute();
        // s.process_output("foxotto");
        // System.out.println("Finished Fox-Otto's algorithm");
        

        // Memory sm = s.get_shared_memory();
        // int from = 0;
        // int to = 1;
        // sm.set("from_node", from);
        // sm.set("to_node", to);
        // // sm.set("old_weight", new SimpleMatrix(new double[][]{{g.adjacency.get(from,to)}}));
        // sm.set("new_weight", new SimpleMatrix(new double[][]{{1.565552}}));
        // sm.set("undirected", 1);
        // // g.update_edge(from, to,Double.POSITIVE_INFINITY, false);
        // System.out.println("Starting Dynamic algorithm...");
        // s = new Simulator(p, g, new DynamicMCU(), sm);
        // s.execute();
        // sm.set("to_node", new SimpleMatrix(new double[][]{{from}}));
        // sm.set("from_node", new SimpleMatrix(new double[][]{{to}}));
        // g.update_edge(to, from, Double.POSITIVE_INFINITY, false);
        // System.out.println("Starting Dynamic algorithm...");
        // s = new Simulator(p, g, new DynamicMCU(), sm);
        // s.execute();
        // s.process_output("dynamic");
        // System.out.println("Finished Dynamic algorithm");
        // SimpleMatrix dynamic_dist = sm.get("output_dist");
        // SimpleMatrix dynamic_pred = sm.get("output_pred");

        // System.out.println("Starting Dijkstra's algorithm...");
        // s = new Simulator(p, g, new DijkstraMCU(), new Memory());
        // s.execute();
        // s.process_output("dijkstra");
        // System.out.println("Finished Dijkstra's algorithm");

        // SimpleMatrix dijkstra_dist = sm.get("output_dist");
        // SimpleMatrix dijkstra_pred = s.get_shared_memory().get("output_pred");

    }
    
    public static void execute_with_arguments(String[] args) throws IOException, ExecutionException, InterruptedException {
        Options options = new Options();
        // Option input = new Option("i", "input", true, "input file path");
        // Option graph = new Option("g", "graph", true, "input random graph parameters");
        options.addOption("i", "input", true, "input file path");
        options.addOption("g", "graph", true, "input random graph parameters (e.g. node_count,edge_percent,weight_mean,weight_std,edge_seed,weight_seed)");
        options.addOption("u", "undirected", true, "whether the graph is undirected");
        options.addOption("e", "edge_update", true, "input parameters describing an edge to update");
        Option output = new Option("o", "output", true, "measurement output file path");
        output.setRequired(true);
        options.addOption(output);
        Option peGridSize = new Option("p", "peGridSize", true, "the length of the side of the processor array");
        peGridSize.setRequired(true);
        options.addOption(peGridSize);
        Option algorithm = new Option("a", "algorithm", true, "the algorithm to execute ('dijkstra', 'foxotto', or 'cannons')");
        algorithm.setRequired(true);
        options.addOption(algorithm);
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }
        
        Graph g = null;

        if (cmd.hasOption("input")) {
            g = new Graph(cmd.getOptionValue("input"), cmd.hasOption("undirected"));
        } else if (cmd.hasOption("graph")) {
            String[] graph_params = cmd.getOptionValue("graph").split(",");
            int node_count = Integer.parseInt(graph_params[0]);
            double edge_percent = Double.parseDouble(graph_params[1]);
            int edge_seed = Integer.parseInt(graph_params[2]);
            int weight_seed = Integer.parseInt(graph_params[3]);
            g = new Graph(node_count, edge_percent, cmd.hasOption("undirected"), edge_seed, weight_seed);
        } else {
            System.out.println("An input file or random graph parameters must be specified (-i <input file> or -g <parameters>)");
            System.exit(1);
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
        // String[] my_args = {"/home/andrei/Dev/RouteFindingSimulator/target/RouteFindingSimulator-1.0-SNAPSHOT-jar-with-dependencies.jar", "-i", "/home/andrei/Dev/RouteFindingSimulator/testing/input/random_graph.txt", "-u", "true", "-e", "6,9,Infinity", "-p", "1", "-a", "dynamic", "-o", "/home/andrei/Dev/RouteFindingSimulator/measurements/first_dynamic/first_dynamic_increase_edge.csv"};
        execute_with_arguments(args);
        // run_simulator();
    }
}