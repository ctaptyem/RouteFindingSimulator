package uk.ac.cam.cl.ac2499;

import org.ejml.simple.SimpleMatrix;

public class CannonsMCU extends CodeBlock{
    public void run() {
        SimpleMatrix graph = sm.get("graph");
        int dim = graph.getNumRows();
        int submatrix_dim = (int) Math.ceil((double) dim / peGridSize);
        SimpleMatrix padded_graph = SimpleMatrix.filled(submatrix_dim * peGridSize, submatrix_dim * peGridSize, Double.POSITIVE_INFINITY);
        padded_graph.insertIntoThis(0, 0, graph);

        for (int num_iterations = 0; num_iterations < (int)(Math.log(dim+1)/Math.log(2)); num_iterations++) {
            for (int i = 1; i<peGridSize * peGridSize + 1; i++) {
                CodeBlock peAlgo = new CannonsPE();
                peAlgo.peGridSize = peGridSize;
                communications.send_instruction(i,peAlgo);
            }

            for (int i = 0; i < peGridSize; i++) {
                for(int j = 0; j < peGridSize; j++) {
                    int k = (i + j) % peGridSize;
                    SimpleMatrix sub_A = padded_graph.extractMatrix(i*submatrix_dim,(i+1)*submatrix_dim,k*submatrix_dim,(k+1)*submatrix_dim);
                    SimpleMatrix sub_B = padded_graph.extractMatrix(k*submatrix_dim,(k+1)*submatrix_dim,j*submatrix_dim,(j+1)*submatrix_dim);
                    sm.set(String.format("%d_A",i*peGridSize+j+1), sub_A);
                    sm.set(String.format("%d_B",i*peGridSize+j+1), sub_B);
                    communications.send_data(0,i*peGridSize+j+1, String.format("%d_A",i*peGridSize+j+1));
                    communications.send_data(0,i*peGridSize+j+1, String.format("%d_B",i*peGridSize+j+1));
                }
            }

            for (int i = 0; i < peGridSize; i++) {
                for(int j = 0; j < peGridSize; j++) {
                    String received = communications.receive_data(i*peGridSize+j+1, 0);
                    print(String.format("Received %s", received));
                    SimpleMatrix sub = (SimpleMatrix) sm.get(received);
                    padded_graph.insertIntoThis(i*submatrix_dim, j*submatrix_dim, sub);
                }
            }
        }

        SimpleMatrix result = padded_graph.extractMatrix(0, dim, 0, dim);

        sm.set("output_dist", result);

        for (int i = 1; i<peGridSize * peGridSize + 1; i++) {
            communications.send_instruction(i, new Shutdown());
        }
        sm.set("times", new SimpleMatrix(1,1));
        communications.send_instruction(0, new Shutdown());
    }
}
