package uk.ac.cam.cl.ac2499.algorithms;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.Heap;
import uk.ac.cam.cl.ac2499.Timer;

public class DijkstraPE extends CodeBlock {

    public void run() {
        Timer timer = new Timer();
        timer.resume();
        pm.add_metrics(9,3);
        int source = Integer.parseInt(communications.receive_data(0, id));
        pm.set("graph", sm.get(communications.receive_data(0,id)));
        int graph_length = pm.get("graph").getNumCols();
        pm.set("dist", SimpleMatrix.filled(1, graph_length, Double.POSITIVE_INFINITY));
        pm.set("pred", SimpleMatrix.filled(1, graph_length, -1));
        
        pm.set("pq", new SimpleMatrix(1, graph_length)); 
        pm.get("pq").set(0, source);
        int pq_size = 1;

        pm.get("dist").set(source, 0.0);
        pm.get("pred").set(source, source);

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

        pm.add_metrics(3, 0);
        timer.pause();
        mm.set(String.format("%d", id), timer.get_time());
        communications.send_matrix(id,0,String.format("%d_dist", source), pm.get("dist"), sm);
        communications.send_matrix(id,0,String.format("%d_pred", source), pm.get("pred"), sm);
    }
    
}



// public class DijkstraPE extends CodeBlock {

//     public void run() {
//         long start = System.currentTimeMillis();
//         pm.set("source", Integer.parseInt(communications.receive_data(0, id)));
//         pm.set("graph", sm.get(communications.receive_data(0,id)));
//         pm.set("graph_length", pm.get("graph").getNumCols());
//         pm.set("dist", SimpleMatrix.filled(1, pm.get_int("graph_length"), Double.POSITIVE_INFINITY));
//         pm.set("prev", new SimpleMatrix(1, pm.get_int("graph_length")));
        
//         pm.set("pq", new SimpleMatrix(1, pm.get_int("graph_length"))); 
//         pm.get("pq").set(0, pm.get_int("source"));
//         pm.set("pq_size", 1);

//         pm.get("dist").set(pm.get_int("source"), 0.0);
//         pm.get("prev").set(pm.get_int("source"), pm.get_int("source"));

//         while (pm.get_int("pq_size") > 0) {
//             pm.set("u", (int) pm.get("pq").get(0));
//             pm.get("pq").set(0, pm.get("pq").get(pm.get_int("pq_size")-1));
//             pm.set("pq_size", pm.get_int("pq_size")-1);
//             pm.set("current", 0);
//             while (true) {
//                 pm.set("left", 2 * pm.get_int("current") + 1);
//                 pm.set("right", 2 * pm.get_int("current") + 2);
//                 pm.set("best", pm.get_int("current"));
                
//                 // Identify which child node has smaller distance, if any
//                 if (pm.get_int("left") < pm.get_int("pq_size") && pm.get("dist").get((int) pm.get("pq").get(pm.get_int("left"))) < pm.get("dist").get((int) pm.get("pq").get(pm.get_int("best")))) {
//                     pm.set("best", pm.get_int("left"));
//                 }
//                 if (pm.get_int("right") < pm.get_int("pq_size") && pm.get("dist").get((int) pm.get("pq").get(pm.get_int("right"))) < pm.get("dist").get((int) pm.get("pq").get(pm.get_int("best")))) {
//                     pm.set("best", pm.get_int("right"));
//                 }
                
//                 // Break if all nodes below have larger distance
//                 if (pm.get_int("best") == pm.get_int("current")) {
//                     break;
//                 }
                
//                 // Swap with the smallest child
//                 double temp = pm.get("pq").get(pm.get_int("current"));
//                 pm.get("pq").set(pm.get_int("current"), pm.get("pq").get(pm.get_int("best")));
//                 pm.get("pq").set(pm.get_int("best"), temp);
                
//                 pm.set("current", pm.get_int("best"));
//             }

//             for (pm.set("v", 0); pm.get_int("v") < pm.get_int("graph_length"); pm.set("v", pm.get_int("v")+1)) {
//                 pm.set("weight", pm.get("graph").get(pm.get_int("u"), pm.get_int("v")));
//                 if (pm.get_db("weight") != 0 && pm.get("dist").get(pm.get_int("v")) > pm.get("dist").get(pm.get_int("u")) + pm.get_db("weight")) {
//                     pm.get("dist").set(pm.get_int("v"), pm.get("dist").get(pm.get_int("u")) + pm.get_db("weight"));
//                     pm.get("prev").set(pm.get_int("v"), pm.get_int("u"));

//                     pm.get("pq").set(pm.get_int("pq_size"), pm.get_int("v"));
//                     // Move the newest node to its correct position
//                     pm.set("current", pm.get_int("pq_size"));
//                     while (pm.get_int("current") > 0) {
//                         pm.set("parent", (pm.get_int("current") - 1) / 2);
                        
//                         // If parent has a shorter distance than current, heap property is satisfied
//                         if (pm.get("dist").get((int) pm.get("pq").get(pm.get_int("parent"))) <= pm.get("dist").get((int) pm.get("pq").get(pm.get_int("current")))) {
//                             break;
//                         }
                        
//                         // Swap with parent node if the heap property does not hold
//                         double temp = pm.get("pq").get(pm.get_int("current"));
//                         pm.get("pq").set(pm.get_int("current"), pm.get("pq").get(pm.get_int("parent")));
//                         pm.get("pq").set(pm.get_int("parent"), temp);
                        
//                         pm.set("current", pm.get_int("parent"));
//                     }
//                     pm.set("pq_size", pm.get_int("pq_size")+1);
//                 }
//             }

//         }
        
//         sm.set(String.format("%d_dist", pm.get_int("source")), pm.get("dist"));
//         sm.set(String.format("%d_prev", pm.get_int("source")), pm.get("prev"));
//         long end = System.currentTimeMillis();
//         mm.set(String.format("%d_time", pm.get_int("source")), end-start);
//         communications.send_data(id,0,String.format("%d", pm.get_int("source")));
//     }
    
// }




        // PriorityQueue<Integer> pq = new PriorityQueue<>((o1, o2) -> {
        //     if (dist.get(o1) > dist.get(o2)) {
        //         return -1;
        //     } else if (dist.get(o1) < dist.get(o2)) {
        //         return 1;
        //     } else {
        //         return 0;
        //     }
        // });
        // pq.offer(source);

        // while (!pq.isEmpty()) {
        //     u = pq.poll();

        //     for (v = 0; v < graph_length; v++) {
        //         weight = graph.get(u, v);
        //         if (weight != 0 && dist.get(v) > dist.get(u) + weight) {
        //             dist.set(v, dist.get(u) + weight);
        //             prev.set(v, u);
        //             pq.offer(v);
        //         }
        //     }
        // }
