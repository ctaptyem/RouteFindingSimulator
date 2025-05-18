package uk.ac.cam.cl.student2435G.algorithms.Dijkstra;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.student2435G.algorithms.CodeBlock;
import uk.ac.cam.cl.student2435G.algorithms.utils.Heap;

public class DijkstraPE extends CodeBlock {

    public void run() {
        int source = Integer.parseInt(communications.receive_data(0, id));
        if (!pm.contains("graph"))
            pm.set("graph", sm.get("graph"));
        int graph_length = pm.get("graph").getNumCols();
        pm.set("dist", SimpleMatrix.filled(1, graph_length, Double.POSITIVE_INFINITY));
        pm.set("pred", SimpleMatrix.filled(1, graph_length, -1));
        
        pm.set("pq", new SimpleMatrix(1, graph_length)); 
        pm.get("pq").set(0, source);
        int pq_size = 1;

        pm.get("dist").set(source, 0.0);
        pm.get("pred").set(source, source);

        while (pq_size > 0) {
            int u = Heap.heap_pop("pq", pq_size, pm, sm);
            pq_size--;
            for (int v = 0; v < graph_length; v++) {
                double weight = pm.get("graph").get(u, v);
                if (weight != 0 && pm.get("dist").get(v) > pm.get("dist").get(u) + weight) {
                    pm.get("dist").set(v, pm.get("dist").get(u) + weight);
                    pm.get("pred").set(v, u);

                    boolean inserted = Heap.heap_update_or_insert(v, "pq", pq_size, pm, sm);
                    if (inserted) pq_size++;
                }
            }
        }

        communications.send_matrix(id,0,String.format("%d_dist", source), pm.get("dist"), sm);
        communications.send_matrix(id,0,String.format("%d_pred", source), pm.get("pred"), sm);
    }
    
}