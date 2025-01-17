package uk.ac.cam.cl.ac2499;

import org.ejml.simple.SimpleMatrix;

public class FoxOttoMCU extends CodeBlock {

    public void run() {

        Graph g = (Graph) sharedMemory.get("graph");
        int dim = g.adjacency.getNumRows();
        int submatrix_dim = (int) Math.ceil((double) dim / peGridSize);
        SimpleMatrix padded_graph = SimpleMatrix.filled(submatrix_dim * peGridSize, submatrix_dim * peGridSize, Double.POSITIVE_INFINITY);
        padded_graph.insertIntoThis(0, 0, g.adjacency);


        for (int num_iterations = 0; num_iterations < (int)(Math.log(dim+1)/Math.log(2)); num_iterations++) {
            for (int i = 1; i<peGridSize * peGridSize + 1; i++) {
                CodeBlock peAlgo = new FoxOttoPE();
                peAlgo.peGridSize = peGridSize;
                communications.send_instruction(i,peAlgo);
            }

            for (int i = 0; i < peGridSize; i++) {
                for(int j = 0; j < peGridSize; j++) {
                    SimpleMatrix sub = padded_graph.extractMatrix(i*submatrix_dim,(i+1)*submatrix_dim,j*submatrix_dim,(j+1)*submatrix_dim);
                    sharedMemory.set(String.format("%d_B",i*peGridSize+j+1), sub);
                    communications.send_data(0,i*peGridSize+j+1, String.format("%d_B",i*peGridSize+j+1));
                    // sharedMemory.set(String.format("%d_A",i*peGridSize+j+1), sub);
                    // communications.send_data(0,i*peGridSize+j+1, String.format("%d_A",i*peGridSize+j+1));
                }
            }
            for (int round = 0; round < peGridSize; round++) {
                for (int i = 0; i < peGridSize; i++) {
                    SimpleMatrix diagonal_submatrix = padded_graph.extractMatrix(i*submatrix_dim, (i+1)*submatrix_dim, ((i+round)%peGridSize)*submatrix_dim, ((i+round)%peGridSize+1)*submatrix_dim);
                    sharedMemory.set(String.format("%d_A",i), diagonal_submatrix);
                    for (int j = 0; j< peGridSize; j++) {
                        communications.receive_data(i*peGridSize+j+1, 0);
                        communications.send_data(0, i*peGridSize+j+1, String.format("%d_A",i));
                    }
                }
            }
            // SimpleMatrix padded_result = new SimpleMatrix(submatrix_dim * peGridSize, submatrix_dim * peGridSize);
            for (int i = 0; i < peGridSize; i++) {
                for(int j = 0; j < peGridSize; j++) {
                    String received = communications.receive_data(i*peGridSize+j+1, 0);
                    print(String.format("Received %s", received));
                    SimpleMatrix sub = (SimpleMatrix) sharedMemory.get(received);
                    padded_graph.insertIntoThis(i*submatrix_dim, j*submatrix_dim, sub);
                }
            }
        }

        SimpleMatrix result = padded_graph.extractMatrix(0, dim, 0, dim);

        sharedMemory.set("output_dist", result);

        for (int i = 1; i<peGridSize * peGridSize + 1; i++) {
            communications.send_instruction(i, new Shutdown());
        }
        communications.send_instruction(0, new Shutdown());
    }
}
