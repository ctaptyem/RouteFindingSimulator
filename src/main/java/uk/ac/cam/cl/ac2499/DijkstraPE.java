package uk.ac.cam.cl.ac2499;

import java.util.PriorityQueue;

import org.ejml.simple.SimpleMatrix;

public class DijkstraPE extends CodeBlock {
    public int source;
    public void run() {
        communications.receive_data(0,id);
        Graph graph = (Graph) sharedMemory.get("graph");
        SimpleMatrix dist = SimpleMatrix.filled(1, graph.length, Double.POSITIVE_INFINITY);
        SimpleMatrix prev = new SimpleMatrix(1, graph.length);
        // for (int i = 0; i < graph.length; i++)
        //     dist.set(i, Double.POSITIVE_INFINITY);
        dist.set(source, 0.0);
        prev.set(source, source);

        PriorityQueue<Integer> pq = new PriorityQueue<>((o1, o2) -> {
            if (dist.get(o1) > dist.get(o2)) {
                return -1;
            } else if (dist.get(o1) < dist.get(o2)) {
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
                if (weight != 0 && dist.get(v) > dist.get(u) + weight) {
                    dist.set(v, dist.get(u) + weight);
                    prev.set(v, u);
                    pq.offer(v);
                }
            }
        }
        
        sharedMemory.set(String.format("%d_dist", source), dist);
        sharedMemory.set(String.format("%d_prev", source), prev);
        communications.send_data(id,0,String.format("%d", source));
    }
    
}
