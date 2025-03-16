package uk.ac.cam.cl.ac2499;

import java.io.IOException;
import java.util.concurrent.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.algorithms.CannonsMCU;
import uk.ac.cam.cl.ac2499.algorithms.CodeBlock;
import uk.ac.cam.cl.ac2499.algorithms.DijkstraMCU;
import uk.ac.cam.cl.ac2499.algorithms.DynamicMCU;
import uk.ac.cam.cl.ac2499.algorithms.FoxOttoMCU;


public class Main {
    public static void run_simulator() throws IOException, ExecutionException, InterruptedException {
        Graph g = new Graph("testing/input/zerod_example_2.txt", true); //new Graph("testing/input/OL.cedge");
        // Graph g = new Graph(200,0.75,true,50.0,20.0,9063,3609);
        // Graph g = new Graph(4,0.300000,true, 50.0, 20.0, 4776, 3417);
        System.out.println(g.adjacency);
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
        int p = 4;
        Simulator s;
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
        System.out.println("Starting Dijkstra's algorithm...");
        s = new Simulator(p, g, new DijkstraMCU(), new Memory());
        s.execute();
        s.process_output("dijkstra");
        System.out.println("Finished Dijkstra's algorithm");

        Memory sm = s.get_shared_memory();
        int from = 5;
        int to = 1;
        sm.set("from_node", new SimpleMatrix(new double[][]{{from}}));
        sm.set("to_node", new SimpleMatrix(new double[][]{{to}}));
        sm.set("old_weight", new SimpleMatrix(new double[][]{{g.adjacency.get(from,to)}}));
        g.update_edge(from, to,3.0, false);
        System.out.println("Starting Dynamic algorithm...");
        s = new Simulator(p, g, new DynamicMCU(), sm);
        s.execute();
        // sm.set("to_node", new SimpleMatrix(new double[][]{{from}}));
        // sm.set("from_node", new SimpleMatrix(new double[][]{{to}}));
        // g.update_edge(to, from, 65.306870, false);
        // System.out.println("Starting Dynamic algorithm...");
        // s = new Simulator(p, g, new DynamicMCU(), sm);
        // s.execute();
        // s.process_output("dynamic");
        // System.out.println("Finished Dynamic algorithm");
        // System.out.println("Starting Dijkstra's algorithm...");
        // s = new Simulator(p, g, new DijkstraMCU(), new Memory());
        // s.execute();
        // s.process_output("dijkstra");
        System.out.println("Finished Dijkstra's algorithm");

        System.out.println(g.adjacency);

    }
    
    public static void run_matmul() throws IOException, ExecutionException, InterruptedException {
        Graph g = new Graph("zerod_example_2.txt", false);
        System.out.println(g.adjacency);
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Memory mem = new Memory();
        mem.set("A", g.adjacency);
        mem.set("B", g.adjacency);
        ProcessingElement pe = new ProcessingElement(0, mem, mem, new CommunicationManager(1));
        CodeBlock algo = new MatMulPE();
        pe.code = algo;
        ex.submit(pe).get();
        System.out.println(mem.get("C"));
        ex.shutdown();
    }
    
    public static void execute_with_arguments(String[] args) throws IOException, ExecutionException, InterruptedException {
        Options options = new Options();
        // Option input = new Option("i", "input", true, "input file path");
        // Option graph = new Option("g", "graph", true, "input random graph parameters");
        options.addOption("i", "input", true, "input file path");
        options.addOption("g", "graph", true, "input random graph parameters (e.g. node_count,edge_percent,weight_mean,weight_std,edge_seed,weight_seed)");
        options.addOption("u", "undirected", true, "whether the graph is undirected");
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

        CodeBlock algo = null;
        if (cmd.getOptionValue("algorithm").equals("dijkstra")) {
            algo = new DijkstraMCU();
        } else if (cmd.getOptionValue("algorithm").equals("foxotto")) {
            algo = new FoxOttoMCU();
        } else if (cmd.getOptionValue("algorithm").equals("cannons")) {
            algo = new CannonsMCU();
        } else {
            System.out.println("Please specify a valid algorithm ('dijkstra', 'foxotto', or 'cannons')");
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
            g = new Graph(node_count, edge_percent, cmd.hasOption("undirected"), 50.0, 20.0, edge_seed, weight_seed);
        } else {
            System.out.println("An input file or random graph parameters must be specified (-i <input file> or -g <parameters>)");
            System.exit(1);
        }

        Simulator s = new Simulator(Integer.parseInt(cmd.getOptionValue("peGridSize")), g, algo, new Memory());
        s.execute();
        s.record_measurement(cmd.getOptionValue("output"));
    }
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        // execute_with_arguments(args);
        run_simulator();
    }
}