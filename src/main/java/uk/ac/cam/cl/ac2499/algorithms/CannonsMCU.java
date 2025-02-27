package uk.ac.cam.cl.ac2499.algorithms;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.Timer;

public class CannonsMCU extends CodeBlock{
    public void run() {
        Timer timer = new Timer();
        timer.resume();
        pm.add_metrics(6, 2);
        pm.set("graph", sm.get("graph"));
        int dim = pm.get("graph").getNumRows();
        int submatrix_dim = (int) Math.ceil((double) dim / peGridSize);
        pm.set("padded_graph", SimpleMatrix.filled(submatrix_dim * peGridSize, submatrix_dim * peGridSize, Double.POSITIVE_INFINITY));
        pm.get("padded_graph").insertIntoThis(0, 0, pm.get("graph"));

        pm.add_metrics(0, 1);
        int max_iterations = (int)(Math.log(dim+1)/Math.log(2));
        for (int num_iterations = 0; num_iterations < max_iterations; num_iterations++) {
            pm.add_metrics(3, 2);

            for (int i = 1; i<peGridSize * peGridSize + 1; i++) {
                pm.add_metrics(4, 1);
                CodeBlock peAlgo = new CannonsPE();
                peAlgo.peGridSize = peGridSize;
                communications.send_instruction(i,peAlgo);
            }

            pm.add_metrics(0, 1);
            for (int i = 0; i < peGridSize; i++) {
                pm.add_metrics(3, 2);
                for(int j = 0; j < peGridSize; j++) {
                    pm.add_metrics(23, 3);
                    int k = (i + j) % peGridSize;
                    int pe_id = i*peGridSize+j+1;
                    pm.set("sub_A", pm.get("padded_graph").extractMatrix(i*submatrix_dim,(i+1)*submatrix_dim,k*submatrix_dim,(k+1)*submatrix_dim));
                    pm.set("sub_B", pm.get("padded_graph").extractMatrix(k*submatrix_dim,(k+1)*submatrix_dim,j*submatrix_dim,(j+1)*submatrix_dim));
                    sm.set(String.format("%d_A",pe_id), pm.get("sub_A"));
                    sm.set(String.format("%d_B",pe_id), pm.get("sub_B"));
                    communications.send_data(0,pe_id, String.format("%d_A",pe_id));
                    communications.send_data(0,pe_id, String.format("%d_B",pe_id));
                }
            }
            timer.pause();
            long max_batch_time = -1;
            pm.add_metrics(0,1);
            for (int i = 0; i < peGridSize; i++) {
                pm.add_metrics(3, 2);
                for(int j = 0; j < peGridSize; j++) {
                    pm.add_metrics(10, 1);
                    pm.set("sub", sm.get(communications.receive_data(i*peGridSize+j+1, 0)));
                    pm.get("padded_graph").insertIntoThis(i*submatrix_dim, j*submatrix_dim, pm.get("sub"));
                    long pe_time = mm.get_long(String.format("%d", i*peGridSize+j+1));
                    if (pe_time > max_batch_time)
                        max_batch_time = pe_time;
                }
                System.out.printf("Cannon: [%s%s]\r", "#".repeat((int) (((double)(num_iterations*peGridSize+i)/(max_iterations*peGridSize))*80)), "-".repeat((int) ((1.0 - (double)(num_iterations*peGridSize+i)/(max_iterations*peGridSize))*80)));

            }
            timer.add_time(max_batch_time);
            timer.resume();
        }

        pm.add_metrics(2, 1);
        pm.set("output_dist", pm.get("padded_graph").extractMatrix(0, dim, 0, dim));
        sm.set("output_dist", pm.get("output_dist"));
        for (int i = 1; i<peGridSize * peGridSize + 1; i++) {
            pm.add_metrics(4, 1);
            communications.send_instruction(i, new Shutdown());
        }
        timer.pause();
        mm.set("runtime", timer.get_time());
        communications.send_instruction(0, new Shutdown());
    }
}
