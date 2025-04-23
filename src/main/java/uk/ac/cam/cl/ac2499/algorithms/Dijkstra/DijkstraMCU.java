package uk.ac.cam.cl.ac2499.algorithms.Dijkstra;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.algorithms.CodeBlock;
import uk.ac.cam.cl.ac2499.algorithms.Shutdown;

public class DijkstraMCU extends CodeBlock {
    public void run() {
        // Timer timer = new Timer();
        // Timer communication_timer = new Timer();
        // timer.resume();
        pm.add_metrics(6, 2);
        pm.set("graph", sm.get("graph"));
        int graph_length = pm.get("graph").getNumCols();
        int batch_size = peGridSize * peGridSize;
        pm.set("path_dists", new SimpleMatrix(graph_length, graph_length));
        pm.set("path_preds", new SimpleMatrix(graph_length, graph_length));
        // timer.pause();

        pm.add_metrics(0, 1);
        for (int i = 0; i < graph_length; i+=batch_size) {
            System.out.printf("Dijkstra: [%s%s]\r", "#".repeat((int) (((double)i/graph_length)*80)), "-".repeat((int) (((double)(graph_length - i)/graph_length)*80)));
            pm.add_metrics(4, 3);
            for (int j = 0; j < batch_size && i+j < graph_length; j++) {
                pm.add_metrics(11, 1);
                CodeBlock PE_algo = new DijkstraPE();
                this.communications.send_instruction(j+1,PE_algo);
                this.communications.send_data(0, j+1, String.format("%d", i+j));
                this.communications.send_data(0,j+1,"graph");
            }
            // long max_batch_time = -1;
            for (int j = 0; j < batch_size && i+j < graph_length; j++) {
                pm.add_metrics(7, 1);
                // communication_timer.resume();
                this.communications.receive_data(j+1,0);
                this.communications.receive_data(j+1,0);
                // communication_timer.pause();
                // long pe_time = mm.get_long(String.format("%d", j+1));
                // if (pe_time > max_batch_time)
                //     max_batch_time = pe_time;
            }
            // timer.add_time(max_batch_time);
        }
        // timer.resume();

        for (int j = 0; j < batch_size; j++) {
            communications.send_instruction(j+1,new Shutdown());
        }

        pm.add_metrics(0, 1);
        for (int source = 0; source < graph_length; source++) {
            pm.add_metrics(7, 1);
            pm.get("path_dists").insertIntoThis(source, 0, sm.get(String.format("%d_dist", source)));
            pm.get("path_preds").insertIntoThis(source, 0, sm.get(String.format("%d_pred", source)));
        }

        sm.set("output_dist", pm.get("path_dists"));
        sm.set("output_pred", pm.get("path_preds"));
        // timer.pause();
        // mm.set("runtime", timer.get_time());
        // mm.set("commtime", communication_timer.get_time());
        System.out.printf("Dijkstra: [%s]%n","#".repeat(80));
        communications.send_instruction(0,new Shutdown());
    }
}

// public class DijkstraMCU extends CodeBlock {
//     public void run() {
        
//         pm.set("graph", sm.get("graph"));
//         pm.set("graph_length", pm.get("graph").getNumCols());
//         pm.set("batch_size", peGridSize * peGridSize);
//         pm.set("i", 0);
//         pm.set("j",0);
//         pm.set("path_dists", new SimpleMatrix(pm.get_int("graph_length"), pm.get_int("graph_length")));
//         pm.set("path_prevs", new SimpleMatrix(pm.get_int("graph_length"), pm.get_int("graph_length")));

//         for (pm.set("i", 0); pm.get_int("i") < pm.get_int("graph_length"); pm.set("i", pm.get_int("i")+pm.get_int("batch_size"))) {
//             for (pm.set("j", 0); pm.get_int("j") < pm.get_int("batch_size") && pm.get_int("i")+pm.get_int("j") < pm.get_int("graph_length"); pm.set("j", pm.get_int("j")+1)) {
//                 this.communications.send_instruction(pm.get_int("j")+1,new DijkstraPE());
//                 this.communications.send_data(0, pm.get_int("j")+1, String.format("%d", pm.get_int("i")+pm.get_int("j")));
//                 this.communications.send_data(0,pm.get_int("j")+1,"graph");
//             }
//             long[] run_times = new long[pm.get_int("batch_size")];

//             for (pm.set("j", 0); pm.get_int("j") < pm.get_int("batch_size") && pm.get_int("i")+pm.get_int("j") < pm.get_int("graph_length"); pm.set("j", pm.get_int("j")+1)) {
//                 this.communications.receive_data(pm.get_int("j")+1,0);

//             }
//         }
//         for (pm.set("i", 0); pm.get_int("i") < pm.get_int("batch_size"); pm.set("i", pm.get_int("i")+1)) {
//             communications.send_instruction(pm.get_int("i")+1,new Shutdown());
//         }

        
        
//         for (pm.set("i", 0); pm.get_int("i") < pm.get_int("graph_length"); pm.set("i", pm.get_int("i")+1)) {
//             pm.get("path_dists").insertIntoThis(pm.get_int("i"), 0, (SimpleMatrix) sm.get(String.format("%d_dist", pm.get_int("i"))));
//             pm.get("path_prevs").insertIntoThis(pm.get_int("i"), 0, (SimpleMatrix) sm.get(String.format("%d_prev", pm.get_int("i"))));
//             run_times[pm.get_int("i")] =  mm.get_long(String.format("%d_time", pm.get_int("i")));
//         }

//         sm.set("output_dist", pm.get("path_dists"));
//         sm.set("output_pred", pm.get("path_prevs"));
//         mm.set("times", run_times);
//         communications.send_instruction(0,new Shutdown());
//     }
// }





// public class DijkstraMCU extends CodeBlock {
//     public void run() {
//         Graph graph = (Graph) sharedMemory.get("graph");
//         int pm.get_int("batch_size") = peGridSize * peGridSize;
//         for (int i = 0; i < graph.length; i+=pm.get_int("batch_size")) {
//             for (int j = 0; j < pm.get_int("batch_size") && i+j < graph.length; j++) {
//                 DijkstraPE PE_dijkstra = new DijkstraPE();
//                 PE_dijkstra.source = i+j;
//                 this.communications.send_instruction(j+1,PE_dijkstra);
//                 this.communications.send_data(0,j+1,"graph");
//             }
//             for (int j = 0; j < pm.get_int("batch_size") && i+j < graph.length; j++) {
//                 this.communications.receive_data(j+1,0);
//             }
//         }
//         for (int j = 0; j < pm.get_int("batch_size"); j++) {
//             communications.send_instruction(j+1,new Shutdown());
//         }
//         SimpleMatrix path_dists = new SimpleMatrix(graph.length, graph.length);
//         SimpleMatrix path_prevs = new SimpleMatrix(graph.length, graph.length);

        
//         for (int source = 0; source < graph.length; source++) {
//             path_dists.insertIntoThis(source, 0, (SimpleMatrix) sharedMemory.get(String.format("%d_dist", source)));
//             path_prevs.insertIntoThis(source, 0, (SimpleMatrix) sharedMemory.get(String.format("%d_prev", source)));
//             // path_prevs[source] = (Integer[]) sharedMemory.get(String.format("%d_prev", source));
//             // path_dists[source] = (Double[]) sharedMemory.get(String.format("%d_dist", source));
//         }

//         sharedMemory.set("output_dist", path_dists);
//         sharedMemory.set("output_pred", path_prevs);
//         communications.send_instruction(0,new Shutdown());
//     }
// }