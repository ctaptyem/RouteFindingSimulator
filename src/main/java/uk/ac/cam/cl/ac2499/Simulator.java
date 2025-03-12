package uk.ac.cam.cl.ac2499;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.*;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.algorithms.CodeBlock;

public class Simulator {
    Graph graph;
    CodeBlock algorithm;
    int peGridSize;
    int processorCount;
    ProcessingElement[][] processors;
    ProcessingElement MCU;
    Memory sharedMemory;
    Memory metricMemory;
    CommunicationManager communications;
    
    public Simulator(int peGridSize, Graph input, CodeBlock algorithm, Memory sharedMemory) {
        this.graph = input;
        this.peGridSize = peGridSize;
        this.processorCount = peGridSize * peGridSize + 1;
        this.processors = new ProcessingElement[peGridSize][peGridSize];
        this.sharedMemory = sharedMemory;
        this.metricMemory = new Memory();
        this.communications = new CommunicationManager(processorCount);
        this.MCU = new ProcessingElement(0, sharedMemory, metricMemory, communications);
        this.algorithm = algorithm;
        for (int i = 0; i < peGridSize; i++) {
            for (int j = 0; j < peGridSize; j++) {
                this.processors[i][j] = new ProcessingElement(i * peGridSize + j + 1, sharedMemory, metricMemory, communications);
            }
        }
        this.algorithm.peGridSize = this.peGridSize;
        sharedMemory.set("graph", this.graph.adjacency);
        communications.send_instruction(0,algorithm);
    }
    
    public void execute() throws InterruptedException, ExecutionException {
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
    }

    public void process_output(String outputName) throws FileNotFoundException {
        String outputFileName = String.format("testing/output/output-%s.csv", outputName);
        String logFileName = String.format("testing/output/log-%s.txt", outputName);
        // Integer[][] prevs = (Integer[][]) sharedMemory.get("output_prev");
        double[][] dists = ((SimpleMatrix) sharedMemory.get("output_dist")).toArray2();
        PrintWriter pw_out = new PrintWriter(outputFileName);
        PrintWriter pw_log = new PrintWriter(logFileName);

        pw_log.print(String.format("%d ms of estimated execution time%n", metricMemory.get_long("runtime")));
        pw_log.println();
        pw_log.print(String.format("%d values read, %d values written to shared memory%n", sharedMemory.total_read, sharedMemory.total_write));
        pw_log.println();

        for (int i = 0; i < dists.length; i++) {
            for (int j = 0; j < dists[0].length; j++) {
                if (dists[i][j] < Double.POSITIVE_INFINITY) {
                    pw_out.print(dists[i][j]);
                    pw_out.print(",");
                } else {
                    pw_out.print("Inf,");
                }
            }
            pw_out.println();
        }

        if (sharedMemory.contains("output_prev")) {
            String prevFileName = String.format("testing/output/predecessors-%s.csv", outputName);
            PrintWriter pw_prev = new PrintWriter(prevFileName);
            double[][] prev = ((SimpleMatrix) sharedMemory.get("output_prev")).toArray2();
            for (int i = 0; i < dists.length; i++) {
                for (int j = 0; j < dists[0].length; j++) {
                    pw_prev.print((int)prev[i][j]+1);
                    pw_prev.print(",");
                }
                pw_prev.println();
            }
            pw_prev.close();
        }
        pw_out.close();
        pw_log.close();
    }

    public void record_measurement(String outputName) throws IOException {
        FileWriter fw = new FileWriter(outputName, true);
        // algorithm, peGridSize, node_count, edge_percent, undirected, weight_mean, weight_std, edge_seed, weight_seed, runtime, commtime, commcount, total_read, total_write
        fw.write(String.format("%s,%d,%s,%d,%d,%d,%d,%d%n", algorithm.getClass().getSimpleName(), peGridSize, graph.get_descriptor(), metricMemory.get_long("runtime"), metricMemory.get_long("commtime"), communications.get_total_communications(), sharedMemory.total_read, sharedMemory.total_write));
        fw.close();
    }

    public Memory get_shared_memory() {
        return sharedMemory;
    }

    public CommunicationManager get_communication_manager() {
        return communications;
    }
}
