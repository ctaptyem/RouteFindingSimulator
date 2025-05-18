package uk.ac.cam.cl.student2435G.algorithms.FoxOtto;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.student2435G.algorithms.CodeBlock;
import uk.ac.cam.cl.student2435G.algorithms.utils.SubMatrix;

public class FoxOttoPE extends CodeBlock{
    
    public void run() {
        SubMatrix B = sm.get_submatrix(communications.receive_data(0,id));
        SimpleMatrix P = sm.get(communications.receive_data(0,id));

        communications.send_data(id,0,"need new A");
        SubMatrix A = sm.get_submatrix(communications.receive_data(0,id));

        int dim = A.matrix.getNumRows();
        SimpleMatrix C = SimpleMatrix.filled(dim, dim, Double.POSITIVE_INFINITY);

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
                            best_pred = A.col+k;
                        }
                    }
                    C.set(i, j, best_value);
                    P.set(i,j,best_pred);
                }
            }
        }


        int B_next_id = Math.floorMod(id-1-pe_grid_size, pe_grid_size*pe_grid_size)+1;
        // int A_next_id = Math.floorMod(id, peGridSize)+(id-((id-1)%peGridSize));
        int B_prev_id = Math.floorMod(id-1+pe_grid_size, pe_grid_size*pe_grid_size)+1;
        // int A_prev_id = Math.floorMod(id-2, peGridSize)+(id-((id-1)%peGridSize));

        for (int num_iterations = 1; num_iterations < pe_grid_size; num_iterations++) {
            // Send B submatrix to neighbor
            communications.send_submatrix(id, B_next_id, String.format("%d_B_%d", B_next_id, num_iterations), B, sm);

            // Receive new B submatrix from neighbor
            B =  sm.get_submatrix(communications.receive_data(B_prev_id, id));

            communications.send_data(id,0,"need new A");
            A = sm.get_submatrix(communications.receive_data(0, id));

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
                                best_pred = A.col+k;
                            }
                        }
                        C.set(i, j, best_value);
                        P.set(i,j,best_pred);
                    }
                }
            }
        }        
        
        communications.send_matrix(id, 0, String.format("%d_C",id), C, sm);
        communications.send_matrix(id, 0, String.format("%d_P",id), P, sm);

    }
}
