package uk.ac.cam.cl.ac2499;

import java.util.concurrent.BlockingQueue;

public class DijkstraMCU extends CodeBlock {
    Graph graph;
    BlockingQueue<Integer> data_queue;
    ProcessingElement[] PE_array;
    
    public void run() {
        int batch_size = processorCount * processorCount;
        for (int i = 0; i < graph.length; i+=batch_size) {
            for (int j = 0; j < batch_size && i+j < graph.length; j++) {
                DijkstraPE PE_dijkstra = new DijkstraPE();
                PE_dijkstra.source = i+j;
                PE_array[j].code = PE_dijkstra;
                PE_array[j].start();
            }
            int finished = 0;
            while (finished < batch_size) {
                synchronized (data_queue) {
                    try {
                        data_queue.take();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    finished++;
                }
            }
        }
    }
}
