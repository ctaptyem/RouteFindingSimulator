package uk.ac.cam.cl.ac2499.algorithms;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.Timer;

public class CannonsPE extends CodeBlock{
    public void run() {
        Timer timer = new Timer();
        Timer communication_timer = new Timer();
        pm.add_metrics(5,1);
        SimpleMatrix A =  sm.get(communications.receive_data(0,id));
        SimpleMatrix B =  sm.get(communications.receive_data(0,id));

        timer.resume();
        int dim = A.getNumRows();
        // pm.set("C", new SimpleMatrix(dim, dim));
        SimpleMatrix C = SimpleMatrix.filled(dim ,dim, Double.POSITIVE_INFINITY);

        // pm.add_metrics(0, 1);
        // for (int i = 0; i < dim; i++) {
        //     pm.add_metrics(3, 2);
        //     SimpleMatrix A_row = A.getRow(i);
        //     if (true || A_row.elementMin() < Double.POSITIVE_INFINITY) {
        //         for (int j = 0; j < dim; j++) {
        //             pm.add_metrics(7, 1);
        //             double best_value = A_row.plus(B.getColumn(j).transpose()).elementMin();
        //             C.set(i, j, best_value);
        //         }
        //     }
        // }

        pm.add_metrics(22, 7);
        int x = (id-1)/peGridSize;
        int y = (id-1)%peGridSize;

        int A_next_id = x*peGridSize + ((y + peGridSize - 1) % peGridSize) + 1;
        int A_prev_id = x*peGridSize + ((y + 1) % peGridSize) + 1;
        int B_next_id = ((x + peGridSize - 1) % peGridSize) * peGridSize + y + 1;
        int B_prev_id = ((x + 1) % peGridSize) * peGridSize + y + 1;

        // if (A_next_id == 5 || A_prev_id == 5 || B_next_id == 5 || B_prev_id == 5 || id == 5)
        //     print(String.format("%nA: %d -> %d -> %d%nB: %d -> %d -> %d", A_prev_id, id, A_next_id, B_prev_id, id, B_next_id));

        for (int num_iterations = 0; num_iterations < peGridSize; num_iterations++) {
            for (int i = 0; i < dim; i++) {
                pm.add_metrics(3, 2);
                SimpleMatrix A_row = A.getRow(i);
                if (A_row.elementMin() < Double.POSITIVE_INFINITY) {
                    for (int j = 0; j < dim; j++) {
                        pm.add_metrics(7, 1);
                        double best_value = Math.min(C.get(i,j), A_row.plus(B.getColumn(j).transpose()).elementMin());
                        C.set(i, j, best_value);
                    }
                }
            }
            
            pm.add_metrics(15, 2);
            // Send A submatrix to neighbor
            communications.send_matrix(id, A_next_id, String.format("%d_A_%d", A_next_id, num_iterations), A, sm);
            // print("Sent A to neighbor");

            // Send B submatrix to neighbor
            communications.send_matrix(id, B_next_id, String.format("%d_B_%d", B_next_id, num_iterations), B, sm);
            // print("Sent B to neighbor");

            // Receive new A submatrix from neighbor
            timer.pause();
            communication_timer.resume();
            A = sm.get(communications.receive_data(A_prev_id, id));
            // print("Received new A");

            // Receive new B submatrix from neighbor
            B = sm.get(communications.receive_data(B_prev_id, id));
            communication_timer.pause();
            // print("Received new B");
            timer.resume();

            // if (id == 2) {
            //     System.out.println("AAAAAAAAAAAAAAAAAAAAA");
            //     System.out.println(A);
            //     System.out.println("BBBBBBBBBBBBBBBBBBBBB");
            //     System.out.println(B);
            // }
            // if (id == 2 && num_iterations == 2) {
            //     System.out.println(A);
            //     System.out.println(B);
            // }

            
            // print("Set new C");
        } 
        // boolean t = false;
        // for (int i = 0; i < A.getNumElements(); i++) {
        //     if (A.get(i) != A_copy.get(i)) {
        //         t = true;
        //     }
        // }
        // if (t) {
        //     print("A mismatch");
        //     System.out.println(A);
        //     System.out.println(A_copy);
        // }
        // // t = false;
        // // for (int i = 0; i < B.getNumElements(); i++) {
        // //     if (B.get(i) != B_copy.get(i)) {
        // //         t = true;
        // //     }
        // // }
        // // if (t) {
        // //     print("B mismatch");
        // //     System.out.println(B);
        // //     System.out.println(B_copy);
        // // }

        pm.add_metrics(3, 0);
        timer.pause();
        mm.set(String.format("%d_runtime", id), timer.get_time());
        mm.set(String.format("%d_commtime", id), communication_timer.get_time());
        communications.send_matrix(id, 0, String.format("%d_C",id), C, sm);
        // print("Sent final C");
 
    }
}
