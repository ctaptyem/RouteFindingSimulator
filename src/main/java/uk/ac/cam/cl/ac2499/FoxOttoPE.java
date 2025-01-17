package uk.ac.cam.cl.ac2499;

import org.ejml.simple.SimpleMatrix;

public class FoxOttoPE extends CodeBlock{
    
    public void run() {
        SimpleMatrix B = (SimpleMatrix) sharedMemory.get(communications.receive_data(0,id));
        print("Received B");
        privateMemory.set("B", B);
        communications.send_data(id,0,"need new A");
        SimpleMatrix A = (SimpleMatrix) sharedMemory.get(communications.receive_data(0,id));
        print("Received A");
        privateMemory.set("A",A);
        // SimpleMatrix A_diag = (SimpleMatrix) sharedMemory.get(communications.receive_data(0,id));
        // print("Received A diagonal");
        int dim = A.getNumRows();
        SimpleMatrix C = new SimpleMatrix(dim, dim);
        privateMemory.set("C", C);


        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                C.set(i, j, A.getRow(i).plus(B.getColumn(j).transpose()).elementMin());
                // C.set(i, j, A_diag.getRow(i).elementMult(B.getColumn(j).transpose()).elementSum());
            }
        }

        print("Got initial C");

        int B_next_id = Math.floorMod(id-1-peGridSize, peGridSize*peGridSize)+1;
        // int A_next_id = Math.floorMod(id, peGridSize)+(id-((id-1)%peGridSize));
        int B_prev_id = Math.floorMod(id-1+peGridSize, peGridSize*peGridSize)+1;
        // int A_prev_id = Math.floorMod(id-2, peGridSize)+(id-((id-1)%peGridSize));

        for (int num_iterations = 1; num_iterations < peGridSize; num_iterations++) {
            sharedMemory.set(String.format("%d_B", B_next_id), B);
            // Send B submatrix to neighbor
            communications.send_data(id, B_next_id, String.format("%d_B", B_next_id));
            print("Sent B to neighbor");

            // Receive new B submatrix from neighbor
            B = (SimpleMatrix) sharedMemory.get(communications.receive_data(B_prev_id, id));
            print("Received new B");
            privateMemory.set("B", B);

            // sharedMemory.set(String.format("%d_A", A_next_id), A);
            // // Send A submatrix to neighbor
            // communications.send_data(id, A_next_id, String.format("%d_A", A_next_id));
            // print("Sent A to neighbor");
            
            // Receive new A submatrix from neighbor
            // A = (SimpleMatrix) sharedMemory.get(communications.receive_data(A_prev_id, id));
            communications.send_data(id,0,"need new A");
            A = (SimpleMatrix) sharedMemory.get(communications.receive_data(0, id));
            print("Received new A");
            privateMemory.set("A", A);

            for (int i = 0; i < dim; i++)
                for (int j = 0; j < dim; j++) {
                    C.set(i, j, Math.min(C.get(i,j),A.getRow(i).plus(B.getColumn(j).transpose()).elementMin()));
                    // C.set(i, j, C.get(i,j) + A.getRow(i).elementMult(B.getColumn(j).transpose()).elementSum());
                }
            print("Set new C");

            
        }        
        sharedMemory.set(String.format("%d_C",id), C);
        communications.send_data(id, 0, String.format("%d_C",id));
        print("Sent final C");
    }
}
