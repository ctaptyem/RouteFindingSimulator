package uk.ac.cam.cl.ac2499;

import java.io.IOException;
import java.util.concurrent.*;


public class Main {
    public static void run_simulator() throws IOException, ExecutionException, InterruptedException {
        Graph g = new Graph("testing/input/zerod_example_2.txt");
        System.out.println(g.adjacency);
        Parameters p = new Parameters(1);
        Simulator s = new Simulator(p, g, new FoxOttoMCU());
        s.start("testing/output/output_foxotto_1.csv");
        p = new Parameters(3);
        s = new Simulator(p, g, new FoxOttoMCU());
        s.start("testing/output/output_foxotto_2.csv");
        s = new Simulator(p, g, new DijkstraMCU());
        s.start("testing/output/output_dijkstra.csv");
    }
    
    public static void run_matmul() throws IOException, ExecutionException, InterruptedException {
        Graph g = new Graph("zerod_example_2.txt");
        System.out.println(g.adjacency);
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Memory mem = new Memory();
        mem.set("A", g.adjacency);
        mem.set("B", g.adjacency);
        ProcessingElement pe = new ProcessingElement(0, mem, new CommunicationManager(1));
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