package uk.ac.cam.cl.ac2499;

import java.util.PriorityQueue;

public class DijkstraPE extends CodeBlock {
    public int source;
    public void run() {
        communications.receive_data(0,id);
        Graph graph = (Graph) sharedMemory.get("graph");
        Double[] dist = new Double[graph.length];
        Integer[] prev = new Integer[graph.length];
        for (int i = 0; i < graph.length; i++)
            dist[i] = Double.POSITIVE_INFINITY;
        dist[source] = 0.0;
        prev[source] = source;

        PriorityQueue<Integer> pq = new PriorityQueue<>((o1, o2) -> {
            if (dist[o1] > dist[o2]) {
                return -1;
            } else if (dist[o1] < dist[o2]) {
                return 1;
            } else {
                return 0;
            }
        });
        pq.offer(source);

        while (!pq.isEmpty()) {
            int u = pq.poll();

            for (int v = 0; v < graph.length; v++) {
                double weight = graph.adjacency.get(u, v);
                if (weight != 0 && dist[v] > dist[u] + weight) {
                    dist[v] = dist[u] + weight;
                    prev[v] = u;
                    pq.offer(v);
                }
            }
        }
        
        sharedMemory.set(String.format("%d_dist", source), dist);
        sharedMemory.set(String.format("%d_prev", source), prev);
        communications.send_data(id,0,String.format("%d", source));
    }
    
}
