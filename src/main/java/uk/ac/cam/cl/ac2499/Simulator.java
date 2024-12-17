package uk.ac.cam.cl.ac2499;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.*;

public class Simulator {
    Graph graph;
    CodeBlock algorithm;
    int peGridSize;
    int processorCount;
    ProcessingElement[][] processors;
    ProcessingElement MCU;
    Memory sharedMemory;
    CommunicationManager communications;
    
    public Simulator(Parameters parameters, Graph input, CodeBlock algorithm) {
        this.graph = input;
        this.peGridSize = parameters.peGridSize;
        this.processorCount = peGridSize * peGridSize + 1;
        this.processors = new ProcessingElement[peGridSize][peGridSize];
        this.sharedMemory = new Memory();
        this.communications = new CommunicationManager(processorCount);
        this.MCU = new ProcessingElement(0, sharedMemory, communications);
        this.algorithm = algorithm;
        for (int i = 0; i < peGridSize; i++) {
            for (int j = 0; j < peGridSize; j++) {
                this.processors[i][j] = new ProcessingElement(i * peGridSize + j + 1, sharedMemory, communications);
            }
        }
        this.algorithm.peGridSize = this.peGridSize;
//        this.algorithm.PE_array = this.processors;
        sharedMemory.set("graph", this.graph);
        communications.send_instruction(0,algorithm);
//        this.MCU.code = algorithm;
    }
    
    public void start() throws ExecutionException, InterruptedException, FileNotFoundException {
        ExecutorService mcuExecutor = Executors.newSingleThreadExecutor();
        ExecutorService peExecutor = Executors.newFixedThreadPool(peGridSize * peGridSize);
        Future<?>[][] jobs = new Future[peGridSize][peGridSize];
        for (int i = 0; i < peGridSize; i++) {
            for (int j = 0; j < peGridSize; j++) {
                jobs[i][j] = peExecutor.submit(processors[i][j]);
            }
        }
        mcuExecutor.submit(this.MCU).get();
        for (int i = 0; i < peGridSize; i++) {
            for (int j = 0; j < peGridSize; j++) {
                jobs[i][j].get();
            }
        }
        mcuExecutor.shutdown();
        peExecutor.shutdown();
        
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
//        this.executor.shutdown();
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
