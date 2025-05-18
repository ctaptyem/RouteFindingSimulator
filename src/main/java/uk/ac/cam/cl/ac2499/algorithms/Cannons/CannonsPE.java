package uk.ac.cam.cl.ac2499.algorithms.Cannons;


import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.algorithms.CodeBlock;
import uk.ac.cam.cl.ac2499.algorithms.utils.SubMatrix;

public class CannonsPE extends CodeBlock{
    public void run() {
        SubMatrix A = sm.get_submatrix(communications.receive_data(0,id));
        SubMatrix B = sm.get_submatrix(communications.receive_data(0,id));
        SimpleMatrix P = sm.get(communications.receive_data(0,id));

        int dim = A.matrix.getNumRows();
        // pm.set("C", new SimpleMatrix(dim, dim));
        SimpleMatrix C = SimpleMatrix.filled(dim ,dim, Double.POSITIVE_INFINITY);

        int x = (id-1)/pe_grid_size;
        int y = (id-1)%pe_grid_size;

        int A_next_id = x*pe_grid_size + ((y + pe_grid_size - 1) % pe_grid_size) + 1;
        int A_prev_id = x*pe_grid_size + ((y + 1) % pe_grid_size) + 1;
        int B_next_id = ((x + pe_grid_size - 1) % pe_grid_size) * pe_grid_size + y + 1;
        int B_prev_id = ((x + 1) % pe_grid_size) * pe_grid_size + y + 1;

        for (int num_iterations = 0; num_iterations < pe_grid_size; num_iterations++) {
            for (int i = 0; i < dim; i++) {
                SimpleMatrix A_row = A.matrix.getRow(i);
                if (A_row.elementMin() < Double.POSITIVE_INFINITY) {
                    for (int j = 0; j < dim; j++) {
                        SimpleMatrix B_col = B.matrix.getColumn(j);
                        double best_value = C.get(i,j);
                        double best_pred = P.get(i,j);
                        for (int k = 0; k < dim; k++) {
                            if (A_row.get(k) + B_col.get(k) < best_value) {
                                best_value = A_row.get(k) + B_col.get(k);
                                // A.col == B.row
                                best_pred = A.col+k;
                            }
                        }
                        C.set(i, j, best_value);
                        P.set(i,j,best_pred);
                    }
                }
            }

            
            // Send A submatrix to neighbor
            communications.send_submatrix(id, A_next_id, String.format("%d_A_%d", A_next_id, num_iterations), A, sm);

            // Send B submatrix to neighbor
            communications.send_submatrix(id, B_next_id, String.format("%d_B_%d", B_next_id, num_iterations), B, sm);

            // Receive new A submatrix from neighbor
            A = sm.get_submatrix(communications.receive_data(A_prev_id, id));

            // Receive new B submatrix from neighbor
            B = sm.get_submatrix(communications.receive_data(B_prev_id, id));
        } 

        pm.add_metrics(3, 0);
        communications.send_matrix(id, 0, String.format("%d_C",id), C, sm); 
        communications.send_matrix(id, 0, String.format("%d_P",id), P, sm); 
    }
}
