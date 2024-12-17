package uk.ac.cam.cl.ac2499;

import org.ejml.simple.SimpleMatrix;

public class FoxOttoMCU extends CodeBlock {
    public void run() {
        Graph g = (Graph) sharedMemory.get("graph");
        int dim = g.adjacency.getNumRows();
        int submatrix_dim = (int) Math.ceil((double) dim / peGridSize);
        SimpleMatrix padded_graph = SimpleMatrix.filled(submatrix_dim * peGridSize, submatrix_dim * peGridSize, Double.POSITIVE_INFINITY);
        padded_graph.insertIntoThis(0, 0, g.adjacency);
        for (int i = 0; i < peGridSize; i++) {
            for(int j = 0; j < peGridSize; j++) {
                SimpleMatrix sub = padded_graph.extractMatrix(i * submatrix_dim,j*submatrix_dim,(i+1)*submatrix_dim,(j+1)*submatrix_dim);
                sharedMemory.set(String.format("%d_A",i+j+1), sub);
                communications.send_data(0,i+j+1, String.format("%d_A",i+j+1));
            }
        }
        for (int i = 1; i<peGridSize * peGridSize + 1; i++) {
            communications.send_instruction(i,new FoxOttoPE());
        }

        for (int i = 1; i<peGridSize * peGridSize + 1; i++) {
            communications.send_instruction(i, new Shutdown());
        }
        communications.send_instruction(0, new Shutdown());
    }
}
