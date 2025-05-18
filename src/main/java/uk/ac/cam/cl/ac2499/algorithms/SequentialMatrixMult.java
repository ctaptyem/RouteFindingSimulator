package uk.ac.cam.cl.ac2499.algorithms;

import org.ejml.simple.SimpleMatrix;

public class SequentialMatrixMult extends CodeBlock {
    public void run() {
        communications.send_instruction(1, new Shutdown());
        SimpleMatrix g = sm.get("graph");
        int dim = g.getNumRows();
        SimpleMatrix C = g; 
        int max_iterations = (int)(Math.log(dim+1)/Math.log(2));
        for (int num_iterations = 0; num_iterations < max_iterations; num_iterations++) {
            SimpleMatrix A = C;
            SimpleMatrix B = C;
            for (int i = 0; i < dim; i++) {
                SimpleMatrix A_row = A.getRow(i);
                if (A_row.elementMin() < Double.POSITIVE_INFINITY) {
                    for (int j = 0; j < dim; j++) {
                        double best_value = Math.min(C.get(i,j), A_row.plus(B.getColumn(j).transpose()).elementMin());
                        C.set(i, j, best_value);
                    }
                }
            }
            System.out.printf("%d/%d\r", num_iterations, max_iterations);
        }
        sm.set("output_dist", C);
        communications.send_instruction(0, new Shutdown());
    }
}
