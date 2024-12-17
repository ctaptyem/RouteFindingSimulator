package uk.ac.cam.cl.ac2499;

import org.ejml.simple.SimpleMatrix;

public class CannonsMCU extends CodeBlock{
    public void run() {
        Graph g = (Graph) sharedMemory.get("graph");
        int dim = g.adjacency.getNumRows();
        int submatrix_dim = (int) Math.ceil((double) dim / peGridSize);
        SimpleMatrix padded_graph = SimpleMatrix.filled(submatrix_dim * peGridSize, submatrix_dim * peGridSize, Double.POSITIVE_INFINITY);
        padded_graph.insertIntoThis(0, 0, g.adjacency);
    }
}
