package uk.ac.cam.cl.ac2499.algorithms.Dijkstra;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.algorithms.CodeBlock;
import uk.ac.cam.cl.ac2499.algorithms.Shutdown;

public class DijkstraMCU extends CodeBlock {
    public void run() {
        pm.set("graph", sm.get("graph"));
        int graph_length = pm.get("graph").getNumCols();
        int batch_size = pe_grid_size * pe_grid_size;
        pm.set("path_dists", new SimpleMatrix(graph_length, graph_length));
        pm.set("path_preds", new SimpleMatrix(graph_length, graph_length));

        for (int i = 0; i < graph_length; i+=batch_size) {
            System.out.printf("Dijkstra: [%s%s]\r", "#".repeat((int) (((double)i/graph_length)*80)), "-".repeat((int) (((double)(graph_length - i)/graph_length)*80)));
            for (int j = 0; j < batch_size && i+j < graph_length; j++) {
                CodeBlock PE_algo = new DijkstraPE();
                this.communications.send_instruction(j+1,PE_algo);
                this.communications.send_data(0, j+1, String.format("%d", i+j));
            }
            for (int j = 0; j < batch_size && i+j < graph_length; j++) {
                this.communications.receive_data(j+1,0);
                this.communications.receive_data(j+1,0);
            }
        }

        for (int j = 0; j < batch_size; j++) {
            communications.send_instruction(j+1,new Shutdown());
        }

        for (int source = 0; source < graph_length; source++) {
            pm.get("path_dists").insertIntoThis(source, 0, sm.get(String.format("%d_dist", source)));
            pm.get("path_preds").insertIntoThis(source, 0, sm.get(String.format("%d_pred", source)));
        }

        sm.set("output_dist", pm.get("path_dists"));
        sm.set("output_pred", pm.get("path_preds"));
        System.out.printf("Dijkstra: [%s]%n","#".repeat(80));
        communications.send_instruction(0,new Shutdown());
    }
}
