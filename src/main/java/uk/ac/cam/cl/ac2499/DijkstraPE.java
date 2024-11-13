package uk.ac.cam.cl.ac2499;

import java.util.Comparator;
import java.util.PriorityQueue;

public class DijkstraPE extends CodeBlock {
    public int source;
    public ControlUnit MCU;
    
    public void run() {
        Graph graph = (Graph) privateMemory.get("graph");
        double[] dist = new double[graph.length];
        for ( int i = 0; i < graph.length ; i++) 
            dist[i] = Double.POSITIVE_INFINITY;
        dist[source] = 0;

        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> dist[a]));
        pq.offer(source);

        while (!pq.isEmpty()) {
            int u = pq.poll();

            for (int v = 0; v < graph.length; v++) {
                double weight = graph.adjacency.get(u, v);
                if (weight != 0 && dist[v] > dist[u] + weight) {
                    dist[v] = dist[u] + weight;
                    pq.offer(v);
                }
            }
        }
        
        sharedMemory.set(String.format("%d", source), dist);
        synchronized (MCU.data_queue) {
            try {
                MCU.data_queue.put(source);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
}
