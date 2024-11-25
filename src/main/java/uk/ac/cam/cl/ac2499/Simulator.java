package uk.ac.cam.cl.ac2499;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.concurrent.*;

public class Simulator {
    Graph graph;
    CodeBlock algorithm;
    int processorCount;
    ProcessingElement[][] processors;
    ProcessingElement MCU;
    Memory sharedMemory;
    ExecutorService executor;
    
    public Simulator(Parameters parameters, Graph input, CodeBlock algorithm) {
        this.graph = input;
        this.processorCount = parameters.processingElementCount;
        this.processors = new ProcessingElement[processorCount][processorCount];
        this.sharedMemory = new Memory();
        this.MCU = new ProcessingElement(0, sharedMemory);
        this.algorithm = algorithm;
        for (int i = 0; i < processorCount; i++) {
            for (int j = 0; j < processorCount; j++) {
                this.processors[i][j] = new ProcessingElement(i * processorCount + j + 1, sharedMemory);
            }
        }
        this.algorithm.processorCount = this.processorCount;
        this.algorithm.PE_array = this.processors;
        MCU.privateMemory.set("graph", this.graph);
        this.MCU.code = algorithm;
    }
    
    public void start() throws ExecutionException, InterruptedException, FileNotFoundException {
        this.executor = Executors.newSingleThreadExecutor();
        this.executor.submit(this.MCU).get();
        Integer[][] prevs = (Integer[][]) sharedMemory.get("output_prev");
        Double[][] dists = (Double[][]) sharedMemory.get("output_dist");
        PrintWriter pw = new PrintWriter("output.csv");
        for (int i = 0; i < prevs.length; i++) {
            for (int j = 0; j < prevs[0].length; j++) {
                pw.print(prevs[i][j]);
                pw.print(",");
            }
            pw.println();
        }
        pw.println();
        for (int i = 0; i < dists.length; i++) {
            for (int j = 0; j < dists[0].length; j++) {
                pw.print(dists[i][j]);
                pw.print(",");
            }
            pw.println();
        }
        System.out.println("Finished writing");
        pw.close();
        this.executor.shutdown();
        return;
//        MCU.start();
//        try {
//            MCU.join();
//           
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }
}
