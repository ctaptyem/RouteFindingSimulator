package uk.ac.cam.cl.ac2499;

import org.ejml.simple.SimpleMatrix;

import java.lang.reflect.Array;
import java.util.List;
import java.util.concurrent.*;

public class DijkstraMCU extends CodeBlock {
    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(processorCount * processorCount);
        Graph graph = (Graph) this.privateMemory.get("graph");
        int batch_size = processorCount * processorCount;
        for (int i = 0; i < graph.length; i+=batch_size) {
            Future<?>[] jobs = new Future[batch_size];
            for (int j = 0; j < batch_size && i+j < graph.length; j++) {
                DijkstraPE PE_dijkstra = new DijkstraPE();
                PE_dijkstra.source = i+j;
                int x = j / processorCount;
                int y = j % processorCount;
                PE_array[x][y].privateMemory.set("graph", graph);
                PE_array[x][y].code = PE_dijkstra;
                jobs[j] = executor.submit(PE_array[x][y]);
            }
            for (int j = 0; j < batch_size && i+j < graph.length; j++) {
                try {
                    jobs[j].get();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        Double[][] path_dists = new Double[graph.length][];
        Integer[][] path_prevs = new Integer[graph.length][];
        
        for (int source = 0; source < graph.length; source++) {
            path_prevs[source] = (Integer[]) sharedMemory.get(String.format("%d_prev", source));
            path_dists[source] = (Double[]) sharedMemory.get(String.format("%d_dist", source));
        }

        sharedMemory.set("output_dist", path_dists);
        sharedMemory.set("output_prev", path_prevs);
        executor.shutdown();
    }
}
