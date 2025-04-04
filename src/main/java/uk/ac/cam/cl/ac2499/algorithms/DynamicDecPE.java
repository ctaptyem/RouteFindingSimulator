package uk.ac.cam.cl.ac2499.algorithms;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.Heap;
import uk.ac.cam.cl.ac2499.Timer;

public class DynamicDecPE extends CodeBlock{
    public void run() {
        Timer timer = new Timer();
        timer.resume();
        int source = Integer.parseInt(communications.receive_data(0, id));
        pm.set("graph", sm.get(communications.receive_data(0,id)));
        int graph_length = pm.get("graph").getNumCols();
        int from_node = (int) sm.get_long(communications.receive_data(0, id));
        int to_node = (int) sm.get_long(communications.receive_data(0, id));
        double old_weight = sm.get(communications.receive_data(0, id)).get(0);

        pm.set("dist", sm.get(communications.receive_data(0, id)));
        pm.set("pred", sm.get(communications.receive_data(0, id)));


        double new_weight = pm.get("graph").get(from_node, to_node);
        double weight_change = new_weight - old_weight;
        pm.set("visited", SimpleMatrix.filled(1, graph_length, 0));
        boolean change = true;
        pm.get("visited").set(to_node, 1);

        while (change) {
            // print("IN PREDECESSOR LOOP");
            change = false;
            for (int v = 0; v < graph_length; v++) {
                // print(String.format("ITERATION %d", v));
                int predecessor = (int) pm.get("pred").get(v);
                // print("GOT PREDECESSOR STATUS");
                if (pm.get("visited").get(v)<1 && predecessor > -1 && pm.get("visited").get(predecessor) != 0) {
                    // print("DID IF STATEMENT");
                    pm.get("visited").set(v, 1);
                    change = true;
                }
            }
        }
        // print("EXITED PREDECESSOR LOOP");

        for (int v = 0; v < graph_length; v++) {
            if (pm.get("visited").get(v) != 0) {
                pm.get("dist").set(v,pm.get("dist").get(v) + weight_change);
            }
        }
        // print("UPDATED PREDECESSORS");

        pm.set("pq", new SimpleMatrix(1, graph_length)); 
        pm.get("pq").set(0, to_node);
        int pq_size = 1;

        while (pq_size > 0) {
            // print(String.format("PQ SIZE IS %d", pq_size));
            pm.add_metrics(2, 3);
            int u = Heap.heap_pop("pq", pq_size, pm, sm);
            pq_size--;
            // pred_min()
            for (int v = 0; v < graph_length; v++) {
                double weight = pm.get("graph").get(v, u);
                double candidate_distance = pm.get("dist").get(v) + weight;
                // if (source == 9) {
                //     print(String.format("%d -> %d: %f+%f=%f vs %f", v, u, pm.get("dist").get(v), weight, pm.get("dist").get(v) + weight, pm.get("dist").get(u)));
                // }
                if (weight != 0 && Double.isFinite(candidate_distance) && pm.get("dist").get(u) >= candidate_distance && Double.isFinite(pm.get("dist").get(v))) {
                    // if (source == 5 && u == 18) {
                    //     print(String.format("\t%d -> 18: %f+%f=%f vs %f", v, pm.get("dist").get(v), weight, pm.get("dist").get(v) + weight, pm.get("dist").get(u)));
                    // }
                    // print(String.format("%d -> %d: %f+%f=%f vs %f", u, v, pm.get("dist").get(v), weight, pm.get("dist").get(v) + weight, pm.get("dist").get(u)));
                    pm.get("pred").set(u, v);
                    pm.get("dist").set(u, pm.get("dist").get(v) + weight);
                }
            }

            pm.add_metrics(0, 1);
            for (int v = 0; v < graph_length; v++) {
                pm.add_metrics(9, 2);
                double weight = pm.get("graph").get(u, v);
                double candidate_distance = pm.get("dist").get(u) + weight;

                if ((weight != 0 && Double.isFinite(candidate_distance) && pm.get("dist").get(v) >= candidate_distance) || ((int) pm.get("pred").get(v) == u)) {
                    pm.add_metrics(9, 2);
                    // if (source == 9) {
                    //     print(String.format("%d -> %d: %f+%f=%f vs %f", u, v, pm.get("dist").get(u), weight, pm.get("dist").get(u) + weight, pm.get("dist").get(v)));
                    // }
                    pm.get("dist").set(v, pm.get("dist").get(u) + weight);
                    // pm.get("pred").set(v, u);

                    boolean inserted = Heap.heap_update_or_insert(v, "pq", pq_size, pm, sm);
                    if (inserted) pq_size++;
                }
            }
        }
        
        // pm.add_metrics(3, 0);
        // sm.set(String.format("%d_dist", source), pm.get("dist"));
        // sm.set(String.format("%d_pred", source), pm.get("pred"));
        timer.pause();
        mm.set(String.format("%d", id), timer.get_time());
        // communications.send_data(id,0,String.format("%d", source));
        communications.send_matrix(id,0,String.format("%d_dist", source), pm.get("dist"), sm);
        communications.send_matrix(id,0,String.format("%d_pred", source), pm.get("pred"), sm);
    }
    
}
