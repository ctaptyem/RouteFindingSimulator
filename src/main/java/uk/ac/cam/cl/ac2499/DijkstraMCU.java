package uk.ac.cam.cl.ac2499;

import java.beans.SimpleBeanInfo;

import org.ejml.simple.SimpleMatrix;

public class DijkstraMCU extends CodeBlock {
    public void run() {
        Graph graph = (Graph) sharedMemory.get("graph");
        int batch_size = peGridSize * peGridSize;
        for (int i = 0; i < graph.length; i+=batch_size) {
            for (int j = 0; j < batch_size && i+j < graph.length; j++) {
                DijkstraPE PE_dijkstra = new DijkstraPE();
                PE_dijkstra.source = i+j;
                this.communications.send_instruction(j+1,PE_dijkstra);
                this.communications.send_data(0,j+1,"graph");
            }
            for (int j = 0; j < batch_size && i+j < graph.length; j++) {
                this.communications.receive_data(j+1,0);
            }
        }
        for (int j = 0; j < batch_size; j++) {
            communications.send_instruction(j+1,new Shutdown());
        }
        SimpleMatrix path_dists = new SimpleMatrix(graph.length, graph.length);
        SimpleMatrix path_prevs = new SimpleMatrix(graph.length, graph.length);

        
        for (int source = 0; source < graph.length; source++) {
            path_dists.insertIntoThis(source, 0, (SimpleMatrix) sharedMemory.get(String.format("%d_dist", source)));
            path_prevs.insertIntoThis(source, 0, (SimpleMatrix) sharedMemory.get(String.format("%d_prev", source)));
            // path_prevs[source] = (Integer[]) sharedMemory.get(String.format("%d_prev", source));
            // path_dists[source] = (Double[]) sharedMemory.get(String.format("%d_dist", source));
        }

        sharedMemory.set("output_dist", path_dists);
        sharedMemory.set("output_prev", path_prevs);
        communications.send_instruction(0,new Shutdown());
    }
}
