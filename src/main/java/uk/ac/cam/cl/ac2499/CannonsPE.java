package uk.ac.cam.cl.ac2499;

import org.ejml.simple.SimpleMatrix;

public class CannonsPE extends CodeBlock{
    public void run() {
        SimpleMatrix A = (SimpleMatrix) sm.get(communications.receive_data(0,id));
        print("Received A");
        pm.set("A",A);
        SimpleMatrix B = (SimpleMatrix) sm.get(communications.receive_data(0,id));
        print("Received B");
        pm.set("B", B);
        int dim = A.getNumRows();
        SimpleMatrix C = new SimpleMatrix(dim, dim);
        pm.set("C", C);

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                C.set(i, j, A.getRow(i).plus(B.getColumn(j).transpose()).elementMin());
            }
        }

        print("Got initial C");

        int x = (id-1)/peGridSize;
        int y = (id-1)%peGridSize;

        int A_next_id = x*peGridSize + ((y + peGridSize - 1) % peGridSize) + 1;
        int A_prev_id = x*peGridSize + ((y + 1) % peGridSize) + 1;
        int B_next_id = ((x + peGridSize - 1) % peGridSize) * peGridSize + y + 1;
        int B_prev_id = ((x + 1) % peGridSize) * peGridSize + y + 1;

        for (int num_iterations = 1; num_iterations < peGridSize; num_iterations++) {
            sm.set(String.format("%d_A", A_next_id), A);
            // Send A submatrix to neighbor
            communications.send_data(id, A_next_id, String.format("%d_A", A_next_id));
            print("Sent A to neighbor");
            
            // Receive new A submatrix from neighbor
            A = (SimpleMatrix) sm.get(communications.receive_data(A_prev_id, id));
            print("Received new A");
            pm.set("A", A);

            sm.set(String.format("%d_B", B_next_id), B);
            // Send B submatrix to neighbor
            communications.send_data(id, B_next_id, String.format("%d_B", B_next_id));
            print("Sent B to neighbor");

            // Receive new B submatrix from neighbor
            B = (SimpleMatrix) sm.get(communications.receive_data(B_prev_id, id));
            print("Received new B");
            pm.set("B", B);

            for (int i = 0; i < dim; i++)
                for (int j = 0; j < dim; j++) {
                    C.set(i, j, Math.min(C.get(i,j),A.getRow(i).plus(B.getColumn(j).transpose()).elementMin()));
                }
            print("Set new C");
        }   

        sm.set(String.format("%d_C",id), C);
        communications.send_data(id, 0, String.format("%d_C",id));
        print("Sent final C");
 
    }
}
