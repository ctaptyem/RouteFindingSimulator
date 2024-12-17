package uk.ac.cam.cl.ac2499;

import org.ejml.simple.SimpleMatrix;

public class MatMulPE extends CodeBlock {
    public void run() {
        SimpleMatrix A = (SimpleMatrix) sharedMemory.get("A");
        SimpleMatrix B = (SimpleMatrix) sharedMemory.get("B");
        int dim = A.getNumRows();
        SimpleMatrix C = new SimpleMatrix(dim, dim);
        // A and B should both be square matrices
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                C.set(i, j, A.getRow(i).plus(B.getColumn(j).transpose()).elementMin());
            }
        }
        sharedMemory.set("C", C);
    }
}
