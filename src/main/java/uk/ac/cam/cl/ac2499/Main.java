package uk.ac.cam.cl.ac2499;


import org.ejml.simple.SimpleMatrix;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.*;


public class Main {
    public static void run_simulator() throws IOException, ExecutionException, InterruptedException {
        Graph g = new Graph("zerod_example_2.txt");
        System.out.println(g.adjacency);
        Parameters p = new Parameters(2);
        CodeBlock algo = new DijkstraMCU();
        Simulator s = new Simulator(p, g, algo);
        s.start();
    }
    
    public static void run_matmul() throws IOException, ExecutionException, InterruptedException {
        Graph g = new Graph("zerod_example_2.txt");
        System.out.println(g.adjacency);
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Memory mem = new Memory();
        mem.set("A", g.adjacency);
        mem.set("B", g.adjacency);
        ProcessingElement pe = new ProcessingElement(0, mem);
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