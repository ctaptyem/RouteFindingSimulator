package uk.ac.cam.cl.ac2499;

import java.io.IOException;
import java.util.concurrent.*;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.algorithms.CannonsMCU;
import uk.ac.cam.cl.ac2499.algorithms.CodeBlock;
import uk.ac.cam.cl.ac2499.algorithms.DijkstraMCU;
import uk.ac.cam.cl.ac2499.algorithms.DynamicMCU;
import uk.ac.cam.cl.ac2499.algorithms.FoxOttoMCU;


public class Main {
    public static void run_simulator() throws IOException, ExecutionException, InterruptedException {
        // Graph g = new Graph("testing/input/line_graph.txt", true); //new Graph("testing/input/OL.cedge");
        // Graph g = new Graph(200,0.75,true,50.0,20.0,9063,3609);
        Graph g = new Graph(6,0.100000,true, 50.0, 20.0, 9492, 6729);
        System.out.println(g.adjacency);
        for (int i = 0; i < g.length; i++) {
            for (int j = 0; j < g.length; j++) {
                if (Double.isFinite(g.adjacency.get(i,j))) {
                    System.out.printf("%f, ", g.adjacency.get(i,j));
                } else {
                    System.out.printf("-8.0, ");
                }
            }
            System.out.println();
        }

        

        System.out.println("Loaded graph...");
        Parameters p = new Parameters(4);
        Simulator s;
        System.out.println("Starting Cannon's algorithm...");
        s = new Simulator(p, g, new CannonsMCU(), new Memory());
        s.execute();
        s.process_output("cannons");
        System.out.println("Finished Cannon's algorithm");
        System.out.println("Starting Fox-Otto's algorithm...");
        s = new Simulator(p, g, new FoxOttoMCU(), new Memory());
        s.execute();
        s.process_output("foxotto");
        System.out.println("Finished Fox-Otto's algorithm");
        System.out.println("Starting Dijkstra's algorithm...");
        s = new Simulator(p, g, new DijkstraMCU(), new Memory());
        s.execute();
        s.process_output("dijkstra");
        System.out.println("Finished Dijkstra's algorithm");


        // SimpleMatrix dijkstra_dist = s.getSharedMemory().get("output_dist");

        // SimpleMatrix cannons_dist = s.getSharedMemory().get("output_dist");

        // for (int i = 0; i < g.length; i++) {
        //     for (int j = 0; j < g.length; j++) {
        //         double dijkstra_value = dijkstra_dist.get(i,j);
        //         double cannons_value = cannons_dist.get(i,j);
        //         System.out.print(String.format("[%d, %d], ", dijkstra_value, cannons_value));
        //         if (dijkstra_value != cannons_value) {
        //             System.out.println("Distances calculated by Dijkstra does not match Cannon's");
        //         } 
                
        //     }
        //     System.out.println();
        // }
        // Memory sm = s.getSharedMemory();
        // int from = 2;
        // int to = 5;
        // sm.set("from_node", new SimpleMatrix(new double[][]{{from}}));
        // sm.set("to_node", new SimpleMatrix(new double[][]{{to}}));
        // sm.set("old_weight", new SimpleMatrix(new double[][]{{g.adjacency.get(from,to)}}));
        // g.update_edge(from, to, 4.0);
        // System.out.println("Starting Dynamic algorithm...");
        // s = new Simulator(p, g, new DynamicMCU(), sm);
        // s.execute();
        // s.process_output("dynamic");
        // System.out.println("Finished Dynamic algorithm");
        // System.out.println("Starting Dijkstra's algorithm...");
        // s = new Simulator(p, g, new DijkstraMCU(), new Memory());
        // s.execute();
        // s.process_output("dijkstra");
        // System.out.println("Finished Dijkstra's algorithm");

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
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        run_simulator();
    }
}