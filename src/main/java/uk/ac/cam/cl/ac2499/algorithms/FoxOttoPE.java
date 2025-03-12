package uk.ac.cam.cl.ac2499.algorithms;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.Timer;

public class FoxOttoPE extends CodeBlock{
    
    public void run() {
        Timer timer = new Timer();
        Timer communication_timer = new Timer();
        pm.add_metrics(5,1);
        SimpleMatrix B = sm.get(communications.receive_data(0,id));
        communications.send_data(id,0,"need new A");
        SimpleMatrix A = sm.get(communications.receive_data(0,id));
        timer.resume();
        int dim = A.getNumRows();
        SimpleMatrix C = new SimpleMatrix(dim, dim);

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

        pm.add_metrics(8, 3);
        int B_next_id = Math.floorMod(id-1-peGridSize, peGridSize*peGridSize)+1;
        // int A_next_id = Math.floorMod(id, peGridSize)+(id-((id-1)%peGridSize));
        int B_prev_id = Math.floorMod(id-1+peGridSize, peGridSize*peGridSize)+1;
        // int A_prev_id = Math.floorMod(id-2, peGridSize)+(id-((id-1)%peGridSize));

        for (int num_iterations = 1; num_iterations < peGridSize; num_iterations++) {
            pm.add_metrics(9,2);
            sm.set(String.format("%d_B_%d", B_next_id, num_iterations), B);
            // Send B submatrix to neighbor
            communications.send_data(id, B_next_id, String.format("%d_B_%d", B_next_id, num_iterations));
            // print("Sent B to neighbor");

            // Receive new B submatrix from neighbor
            timer.pause();  
            communication_timer.resume();
            B =  sm.get(communications.receive_data(B_prev_id, id));
            // print("Received new B");

            communications.send_data(id,0,"need new A");
            A = sm.get(communications.receive_data(0, id));
            // print("Received new A");
            communication_timer.pause();
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
            
            // print("Set new C;
        }        
        
        pm.add_metrics(3, 0);
        sm.set(String.format("%d_C",id), C);
        timer.pause();
        mm.set(String.format("%d_runtime", id), timer.get_time());
        mm.set(String.format("%d_commtime", id), communication_timer.get_time());
        communications.send_data(id, 0, String.format("%d_C",id));
        // print("Sent final C");
    }
}
