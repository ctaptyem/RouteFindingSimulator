package uk.ac.cam.cl.ac2499.algorithms.Dynamic;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.algorithms.CodeBlock;
import uk.ac.cam.cl.ac2499.algorithms.utils.Heap;

public class DynamicIncPE extends CodeBlock{
    public void run() {
        int source = Integer.parseInt(communications.receive_data(0, id));
        if (!pm.contains("graph"))
            pm.set("graph", sm.get("graph"));
        int graph_length = pm.get("graph").getNumCols();
        int from_node = (int) sm.get_long(communications.receive_data(0, id));
        int to_node = (int) sm.get_long(communications.receive_data(0, id));
        double old_weight = sm.get(communications.receive_data(0, id)).get(0);

        pm.set("dist", sm.get(communications.receive_data(0, id)));
        pm.set("pred", sm.get(communications.receive_data(0, id)));

        double new_weight = pm.get("graph").get(from_node, to_node);

        pm.set("pq", new SimpleMatrix(1, graph_length)); 
        pm.get("dist").set(to_node, pm.get("dist").get(from_node) + new_weight);
        pm.get("pred").set(to_node, from_node);
        pm.get("pq").set(0, to_node);
        int pq_size = 1;

        while (pq_size > 0) {
            pm.add_metrics(2, 3);
            int u = Heap.heap_pop("pq", pq_size, pm, sm);
            pq_size--;
            pm.add_metrics(0, 1);
            for (int v = 0; v < graph_length; v++) {
                pm.add_metrics(9, 2);
                double weight = pm.get("graph").get(u, v);
                if (weight != 0 && pm.get("dist").get(v) > pm.get("dist").get(u) + weight) {
                    pm.add_metrics(9, 2);
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
