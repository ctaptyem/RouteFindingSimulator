package uk.ac.cam.cl.ac2499.algorithms;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.algorithms.utils.Heap;

public class SequentialDijkstra extends CodeBlock {
    public void run() {
        communications.send_instruction(1,new Shutdown());
        SimpleMatrix g = sm.get("graph");
        int dim = g.getNumCols();
        SimpleMatrix path_dists = new SimpleMatrix(dim, dim);
        SimpleMatrix path_preds = new SimpleMatrix(dim, dim);
        for (int source = 0; source < dim; source++) {
            if (source % 100 == 0) {
                System.out.printf("%d/%d\r",source,dim);
            }
            pm.set("dist", SimpleMatrix.filled(1, dim, Double.POSITIVE_INFINITY));
            pm.set("pred", SimpleMatrix.filled(1, dim, -1));
            
            pm.set("pq", new SimpleMatrix(1, dim)); 
            pm.get("pq").set(0, source);
            int pq_size = 1;

            pm.get("dist").set(source, 0.0);
            pm.get("pred").set(source, source);

            while (pq_size > 0) {
                pm.add_metrics(2, 3);
                int u = Heap.heap_pop("pq", pq_size, pm, sm);
                pq_size--;
                pm.add_metrics(0, 1);
                for (int v = 0; v < dim; v++) {
                    pm.add_metrics(9, 2);
                    double weight = g.get(u, v);
                    if (weight != 0 && pm.get("dist").get(v) > pm.get("dist").get(u) + weight) {
                        pm.add_metrics(9, 2);
                        pm.get("dist").set(v, pm.get("dist").get(u) + weight);
                        pm.get("pred").set(v, u);

                        boolean inserted = Heap.heap_update_or_insert(v, "pq", pq_size, pm, sm);
                        if (inserted) pq_size++;
                    }
                }
            }
            path_dists.insertIntoThis(source, 0, pm.get("dist"));
            path_preds.insertIntoThis(source, 0, pm.get("pred"));
        }
        sm.set("output_dist", path_dists);
        sm.set("output_pred", path_preds);
        System.out.println("Done");
        communications.send_instruction(0,new Shutdown());
    }
}
