package uk.ac.cam.cl.ac2499.algorithms;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.Heap;
import uk.ac.cam.cl.ac2499.Timer;

public class DynamicPE extends CodeBlock {
    public void run() {
        Timer timer = new Timer();
        timer.resume();
        int source = Integer.parseInt(communications.receive_data(0, id));
        pm.set("graph", sm.get(communications.receive_data(0,id)));
        int graph_length = pm.get("graph").getNumCols();
        int from_node = (int) sm.get(communications.receive_data(0, id)).get(0);
        int to_node = (int) sm.get(communications.receive_data(0, id)).get(0);
        double old_weight = sm.get(communications.receive_data(0, id)).get(0);

        pm.set("dist", sm.get(communications.receive_data(0, id)));
        pm.set("pred", sm.get(communications.receive_data(0, id)));
        pm.set("visited", new SimpleMatrix(1, graph_length));

        double new_weight = pm.get("graph").get(from_node, to_node);
        double weight_change = new_weight - old_weight;

        if (weight_change > 0) {
            // decremental algorithm
            if (pm.get("pred").get(to_node) == from_node) {
                pm.set("visited", SimpleMatrix.filled(1, graph_length, 0));
                boolean change = true;
                pm.get("visited").set(to_node, 1);

                while (change) {
                    change = false;
                    for (int v = 0; v < graph_length; v++) {
                        int predecessor = (int) pm.get("pred").get(v);
                        if (pm.get("visited").get(v)!=1 && predecessor > -1 && pm.get("visited").get(predecessor) != 0) {
                            pm.get("visited").set(v, 1);
                            change = true;
                        }
                    }
                }

                for (int v = 0; v < graph_length; v++) {
                    if (pm.get("visited").get(v) != 0) {
                        pm.get("dist").set(v,pm.get("dist").get(v) + weight_change);
                    }
                }


                pm.set("pq", new SimpleMatrix(1, graph_length)); 
                pm.get("pq").set(0, to_node);
                int pq_size = 1;


                while (pq_size > 0) {
                    pm.add_metrics(2, 3);
                    int u = Heap.heap_pop("pq", pq_size, pm, sm);
                    pq_size--;
                    // pred_min()
                    for (int v = 0; v < graph_length; v++) {
                        double weight = pm.get("graph").get(v, u);
                        if (weight != 0 && pm.get("dist").get(u) > pm.get("dist").get(v) + weight) {
                            pm.get("pred").set(u, v);
                            if (u == 0 && v == 6) {
                                print("\n\nSomething\n\n");
                            }
                            pm.get("dist").set(u, pm.get("dist").get(v) + weight);
                        }
                    }

                    pm.add_metrics(0, 1);
                    for (int v = 0; v < graph_length; v++) {
                        pm.add_metrics(9, 2);
                        double weight = pm.get("graph").get(u, v);
                        if ((weight != 0 && pm.get("dist").get(v) > pm.get("dist").get(u) + weight) || ((int) pm.get("pred").get(v) == u)) {
                            pm.add_metrics(9, 2);
                            pm.get("dist").set(v, pm.get("dist").get(u) + weight);
                            // pm.get("pred").set(v, u);

                            boolean inserted = Heap.heap_update_or_insert(v, "pq", pq_size, pm, sm);
                            if (inserted) pq_size++;
                        }
                    }
                }
            }
        } else {
            // incremental algorithm
            if (pm.get("dist").get(to_node) >= pm.get("dist").get(from_node) + new_weight) { // no improvement can be accomplished otherwise
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
                            // print(String.format("%d", pq_size));
                            boolean inserted = Heap.heap_update_or_insert(v, "pq", pq_size, pm, sm);
                            if (inserted) pq_size++;
                        }
                    }
                }
            } 
        }
        
        pm.add_metrics(3, 0);
        sm.set(String.format("%d_dist", source), pm.get("dist"));
        sm.set(String.format("%d_prev", source), pm.get("pred"));
        timer.pause();
        mm.set(String.format("%d", id), timer.get_time());
        communications.send_data(id,0,String.format("%d", source));

    }
}
