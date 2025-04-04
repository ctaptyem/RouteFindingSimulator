package uk.ac.cam.cl.ac2499.algorithms;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.Timer;

public class DynamicMCU extends CodeBlock {
    public void update_edge(Timer timer, Timer communication_timer, int graph_length, int batch_size, SimpleMatrix dist, SimpleMatrix pred) {
        timer.pause();
        for (int i = 0; i < graph_length; i+=batch_size) {
            // System.out.printf("MA: [%s%s]\r", "#".repeat((int) (((double)i/graph_length)*80)), "-".repeat((int) (((double)(graph_length - i)/graph_length)*80)));
            pm.add_metrics(4, 3);
            for (int j = 0; j < batch_size && i+j < graph_length; j++) {
                pm.add_metrics(11, 1);
                DynamicPE PE_algo = new DynamicPE();
                this.communications.send_instruction(j+1,PE_algo);
                this.communications.send_data(0, j+1, String.format("%d", i+j));
                this.communications.send_data(0,j+1,"graph");
                this.communications.send_data(0, j+1, "from_node");
                this.communications.send_data(0, j+1, "to_node");
                this.communications.send_data(0, j+1, "old_weight");
                // sm.set(String.format("%d_dist", j+1), dist.getRow(i+j));
                this.communications.send_matrix(0,j+1, String.format("%d_dist", i+j), dist.getRow(i+j), sm);
                // sm.set(String.format("%d_pred", j+1), pred.getRow(i+j));
                this.communications.send_matrix(0,j+1, String.format("%d_pred", i+j), pred.getRow(i+j), sm);
            }
            long max_batch_time = -1;
            for (int j = 0; j < batch_size && i+j < graph_length; j++) {
                pm.add_metrics(7, 1);
                communication_timer.resume();
                this.communications.receive_data(j+1,0);
                this.communications.receive_data(j+1,0);
                communication_timer.pause();
                long pe_time = mm.get_long(String.format("%d", j+1));
                if (pe_time > max_batch_time)
                    max_batch_time = pe_time;
            }
            timer.add_time(max_batch_time);
        }
        timer.resume();
    }

    public void run() {
        Timer timer = new Timer();
        Timer communication_timer = new Timer();
        timer.resume();
        SimpleMatrix graph = sm.get("graph");//sm.get_graph("graph");
        int graph_length = graph.getNumCols();
        int batch_size = peGridSize * peGridSize;
        SimpleMatrix dist = sm.get("output_dist");
        SimpleMatrix pred = sm.get("output_pred");
        int node_A = (int) sm.get_long("from_node");
        int node_B = (int) sm.get_long("to_node");
        double new_weight = sm.get("new_weight").get(0);
        long undirected = sm.get_long("undirected");
        double old_weight = graph.get(node_A, node_B);
        sm.set("old_weight", new SimpleMatrix(new double[][]{{old_weight}}));

        graph.set(node_A, node_B, new_weight);

        pm.add_metrics(0, 1);

        update_edge(timer, communication_timer, graph_length, batch_size, dist, pred);

        pm.set("path_dists", new SimpleMatrix(graph_length, graph_length));
        pm.set("path_preds", new SimpleMatrix(graph_length, graph_length));

        pm.add_metrics(0, 1);
        for (int source = 0; source < graph_length; source++) {
            pm.add_metrics(7, 1);
            pm.get("path_dists").insertIntoThis(source, 0, sm.get(String.format("%d_dist", source)));
            pm.get("path_preds").insertIntoThis(source, 0, sm.get(String.format("%d_pred", source)));
            // dist.insertIntoThis(source, 0, sm.get(String.format("%d_dist", source)));
            // pred.insertIntoThis(source, 0, sm.get(String.format("%d_pred", source)));

        }

        if (undirected == 1) {
            dist = pm.get("path_dists");
            pred = pm.get("path_preds");
            graph.set(node_B, node_A, new_weight);
            sm.set("from_node", node_B);
            sm.set("to_node", node_A);

            update_edge(timer, communication_timer, graph_length, batch_size, dist, pred);

            pm.add_metrics(0, 1);
            for (int source = 0; source < graph_length; source++) {
                pm.add_metrics(7, 1);
                pm.get("path_dists").insertIntoThis(source, 0, sm.get(String.format("%d_dist", source)));
                pm.get("path_preds").insertIntoThis(source, 0, sm.get(String.format("%d_pred", source)));
                // dist.insertIntoThis(source, 0, sm.get(String.format("%d_dist", source)));
                // pred.insertIntoThis(source, 0, sm.get(String.format("%d_pred", source)));
            }

        }
        for (int j = 0; j < batch_size; j++) {
            communications.send_instruction(j+1,new Shutdown());
        }

        

        sm.set("output_dist", pm.get("path_dists"));
        sm.set("output_pred", pm.get("path_preds"));
        timer.pause();
        mm.set("runtime", timer.get_time());
        mm.set("commtime", communication_timer.get_time());
        System.out.printf("MA: [%s]%n","#".repeat(80));
        communications.send_instruction(0,new Shutdown());

        // if (old_weight < graph.get(node_A, node_B)) {
        //     // For every tree, run the decremental algorithm with the difference in weights
        // } else {
        //     // For every tree, run the incremental algorithm
        // }
    }
}
