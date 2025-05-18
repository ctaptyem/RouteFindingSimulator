package uk.ac.cam.cl.ac2499.algorithms.Dynamic;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.algorithms.CodeBlock;
import uk.ac.cam.cl.ac2499.algorithms.Shutdown;

public class DynamicMCU extends CodeBlock {
    public void update_edge(int graph_length, int batch_size, SimpleMatrix dist, SimpleMatrix pred, int from_node, int to_node, double weight_change, double new_weight) {
        int source = 0;
        while (source < graph_length) {
            int dispatch = 1;
            while (source < graph_length && dispatch <= batch_size) {
                CodeBlock PE_algo;
                if (weight_change > 0 && pred.get(source, to_node) == from_node) {
                    //dispatch decremental
                    PE_algo = new DynamicDecPE();
                } else if (weight_change < 0 && dist.get(source, to_node) > dist.get(source, from_node) + new_weight) { // was >=
                    // dispatch incremental
                    PE_algo = new DynamicIncPE();
                } else {
                    source++;
                    continue;
                }
                communications.send_instruction(dispatch,PE_algo);
                communications.send_data(0, dispatch, String.format("%d", source));
                communications.send_data(0, dispatch, "from_node");
                communications.send_data(0, dispatch, "to_node");
                communications.send_data(0, dispatch, "old_weight");
                communications.send_matrix(0,dispatch, String.format("%d_dist", source), dist.getRow(source), sm);
                communications.send_matrix(0,dispatch, String.format("%d_pred", source), pred.getRow(source), sm);
                dispatch++;
                source++;
            }
            for (int p = 1; p < dispatch; p++) {
                pm.add_metrics(7, 1);
                this.communications.receive_data(p,0);
                this.communications.receive_data(p,0);
            }
        }
    }

    public void run() {
        SimpleMatrix graph = sm.get("graph");
        int graph_length = graph.getNumCols();
        int batch_size = pe_grid_size * pe_grid_size;
        SimpleMatrix dist = sm.get("output_dist");
        SimpleMatrix pred = sm.get("output_pred");
        int node_A = (int) sm.get_long("from_node");
        int node_B = (int) sm.get_long("to_node");
        double new_weight = sm.get("new_weight").get(0);
        long undirected = sm.get_long("undirected");
        double old_weight = graph.get(node_A, node_B);
        sm.set("old_weight", new SimpleMatrix(new double[][]{{old_weight}}));

        graph.set(node_A, node_B, new_weight);

        update_edge(graph_length, batch_size, dist, pred, node_A, node_B, new_weight - old_weight, new_weight);

        pm.set("path_dists", new SimpleMatrix(graph_length, graph_length));
        pm.set("path_preds", new SimpleMatrix(graph_length, graph_length));

        for (int source = 0; source < graph_length; source++) {
            pm.get("path_dists").insertIntoThis(source, 0, sm.get(String.format("%d_dist", source)));
            pm.get("path_preds").insertIntoThis(source, 0, sm.get(String.format("%d_pred", source)));
        }

        if (undirected == 1) {
            dist = pm.get("path_dists");
            pred = pm.get("path_preds");
            graph.set(node_B, node_A, new_weight);
            sm.set("from_node", node_B);
            sm.set("to_node", node_A);

            update_edge(graph_length, batch_size, dist, pred, node_B, node_A, new_weight - old_weight, new_weight);

            for (int source = 0; source < graph_length; source++) {
                pm.get("path_dists").insertIntoThis(source, 0, sm.get(String.format("%d_dist", source)));
                pm.get("path_preds").insertIntoThis(source, 0, sm.get(String.format("%d_pred", source)));
            }

        }
        for (int j = 0; j < batch_size; j++) {
            communications.send_instruction(j+1,new Shutdown());
        }

        sm.set("output_dist", pm.get("path_dists"));
        sm.set("output_pred", pm.get("path_preds"));
        System.out.printf("MA: [%s]%n","#".repeat(80));
        communications.send_instruction(0,new Shutdown());
    }
}
