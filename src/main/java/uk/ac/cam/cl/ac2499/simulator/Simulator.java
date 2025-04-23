package uk.ac.cam.cl.ac2499.simulator;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.*;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.Graph;
import uk.ac.cam.cl.ac2499.algorithms.CodeBlock;

public class Simulator {
    Graph graph;
    CodeBlock algorithm;
    int peGridSize;
    int processorCount;
    ProcessingElement[][] processors;
    ProcessingElement MCU;
    Memory sharedMemory;
    // Memory metricMemory;
    CommunicationManager communications;
    
    public Simulator(int peGridSize, Graph input, CodeBlock algorithm, Memory sharedMemory) {
        this.graph = input;
        this.peGridSize = peGridSize;
        this.processorCount = peGridSize * peGridSize + 1;
        this.processors = new ProcessingElement[peGridSize][peGridSize];
        this.sharedMemory = sharedMemory;
        // this.metricMemory = new Memory();
        this.communications = new CommunicationManager(processorCount);
        this.MCU = new ProcessingElement(0, sharedMemory, communications);
        this.algorithm = algorithm;
        for (int i = 0; i < peGridSize; i++) {
            for (int j = 0; j < peGridSize; j++) {
                this.processors[i][j] = new ProcessingElement(i * peGridSize + j + 1, sharedMemory, communications);
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

    public long[] extract_metrics() {
        long runtime = MetricTracker.get_log_sum(MCU.metric_tracker.runtime_log);
        long comm_count = MetricTracker.get_log_sum(MCU.metric_tracker.comm_count_log);
        long comm_volume = MetricTracker.get_log_sum(MCU.metric_tracker.comm_volume_log);
        long[] PE_metrics = MetricTracker.process_metrics(processors);
        return new long[]{PE_metrics[0]+runtime, PE_metrics[1]+comm_count, PE_metrics[2]+comm_volume};
    }

    public void process_output(String outputName) throws FileNotFoundException {
        String outputFileName = String.format("testing/output/output-%s.csv", outputName);
        String logFileName = String.format("testing/output/log-%s.txt", outputName);
        double[][] dists = ((SimpleMatrix) sharedMemory.get("output_dist")).toArray2();
        PrintWriter pw_out = new PrintWriter(outputFileName);
        PrintWriter pw_log = new PrintWriter(logFileName);

        // pw_log.print(String.format("%d ms of estimated execution time%n", metricMemory.get_long("runtime")));
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

        if (sharedMemory.contains("output_pred")) {
            String predFileName = String.format("testing/output/predecessors-%s.csv", outputName);
            PrintWriter pw_pred = new PrintWriter(predFileName);
            double[][] pred = ((SimpleMatrix) sharedMemory.get("output_pred")).toArray2();
            for (int i = 0; i < dists.length; i++) {
                for (int j = 0; j < dists[0].length; j++) {
                    pw_pred.print((int)pred[i][j]);
                    pw_pred.print(",");
                }
                pw_pred.println();
            }
            pw_pred.close();
        }
        pw_out.close();
        pw_log.close();
    }

    public void record_measurement(String outputName) throws IOException {
        FileWriter fw = new FileWriter(outputName, true);
        long[] metrics = extract_metrics();
        // algorithm, peGridSize, node_count, edge_percent, undirected, edge_seed, weight_seed, runtime, commtime, commcount, commvolume, total_read, total_write
        fw.write(String.format("%s,%d,%s,%d,%d,%d,%d,%d%n", 
            algorithm.getClass().getSimpleName(), 
            peGridSize, 
            graph.get_descriptor(), 
            metrics[0], 
            // metricMemory.get_long("commtime"), 
            metrics[1], 
            metrics[2], 
            sharedMemory.total_read, 
            sharedMemory.total_write)
        );
        fw.close();
    }

    public Memory get_shared_memory() {
        return sharedMemory;
    }

    public CommunicationManager get_communication_manager() {
        return communications;
    }
}
