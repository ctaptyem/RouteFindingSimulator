package uk.ac.cam.cl.ac2499.algorithms;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.Timer;

public class CannonsPE extends CodeBlock{
    public void run() {
        Timer timer = new Timer();
        pm.add_metrics(5,1);
        SimpleMatrix A =  sm.get(communications.receive_data(0,id));
        SimpleMatrix B =  sm.get(communications.receive_data(0,id));
        timer.resume();
        int dim = A.getNumRows();
        // pm.set("C", new SimpleMatrix(dim, dim));
        SimpleMatrix C = new SimpleMatrix(dim ,dim);

        pm.add_metrics(0, 1);
        for (int i = 0; i < dim; i++) {
            pm.add_metrics(3, 2);
            SimpleMatrix A_row = A.getRow(i);
            if (A_row.elementMin() < Double.POSITIVE_INFINITY) {
                for (int j = 0; j < dim; j++) {
                    pm.add_metrics(7, 1);
                    double best_value = A_row.plus(B.getRow(j)).elementMin();
                    C.set(i, j, best_value);
                }
            }
        }

        pm.add_metrics(22, 7);
        int x = (id-1)/peGridSize;
        int y = (id-1)%peGridSize;

        int A_next_id = x*peGridSize + ((y + peGridSize - 1) % peGridSize) + 1;
        int A_prev_id = x*peGridSize + ((y + 1) % peGridSize) + 1;
        int B_next_id = ((x + peGridSize - 1) % peGridSize) * peGridSize + y + 1;
        int B_prev_id = ((x + 1) % peGridSize) * peGridSize + y + 1;

        for (int num_iterations = 1; num_iterations < peGridSize; num_iterations++) {
            pm.add_metrics(15, 2);
            sm.set(String.format("%d_A", A_next_id), A);
            // Send A submatrix to neighbor
            communications.send_data(id, A_next_id, String.format("%d_A", A_next_id));
            // print("Sent A to neighbor");
            
            // Receive new A submatrix from neighbor
            timer.pause();
            pm.set("A", sm.get(communications.receive_data(A_prev_id, id)));
            // print("Received new A");
            timer.resume();

            sm.set(String.format("%d_B", B_next_id), B);
            // Send B submatrix to neighbor
            communications.send_data(id, B_next_id, String.format("%d_B", B_next_id));
            // print("Sent B to neighbor");

            // Receive new B submatrix from neighbor
            timer.pause();
            pm.set("B", sm.get(communications.receive_data(B_prev_id, id)));
            // print("Received new B");
            timer.resume();

            for (int i = 0; i < dim; i++) {
                pm.add_metrics(3, 2);
                SimpleMatrix A_row = A.getRow(i);
                if (A_row.elementMin() < Double.POSITIVE_INFINITY) {
                    for (int j = 0; j < dim; j++) {
                        pm.add_metrics(7, 1);
                        double best_value = Math.min(C.get(i,j), A_row.plus(B.getRow(j)).elementMin());
                        C.set(i, j, best_value);
                    }
                }
            }
            // print("Set new C");
        }   

        pm.add_metrics(3, 0);
        sm.set(String.format("%d_C",id), C);
        timer.pause();
        mm.set(String.format("%d", id), timer.get_time());
        communications.send_data(id, 0, String.format("%d_C",id));
        // print("Sent final C");
 
    }
}
