package uk.ac.cam.cl.ac2499.algorithms;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.Timer;

public class FoxOttoMCU extends CodeBlock {

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
                CodeBlock peAlgo = new FoxOttoPE();
                peAlgo.peGridSize = peGridSize;
                communications.send_instruction(i,peAlgo);
            }

            pm.add_metrics(0, 1);
            for (int i = 0; i < peGridSize; i++) {
                pm.add_metrics(3, 2);
                for(int j = 0; j < peGridSize; j++) {
                    pm.add_metrics(12, 1);
                    pm.set("sub", pm.get("padded_graph").extractMatrix(i*submatrix_dim,(i+1)*submatrix_dim,j*submatrix_dim,(j+1)*submatrix_dim).transpose());
                    sm.set(String.format("%d_B",i*peGridSize+j+1), pm.get("sub"));
                    communications.send_data(0,i*peGridSize+j+1, String.format("%d_B",i*peGridSize+j+1));
                }
            }
            pm.add_metrics(0, 1);
            for (int round = 0; round < peGridSize; round++) {
                pm.add_metrics(3,2);
                for (int i = 0; i < peGridSize; i++) {
                    pm.add_metrics(3,2);
                    pm.set("diagonal_submatrix", pm.get("padded_graph").extractMatrix(i*submatrix_dim, (i+1)*submatrix_dim, ((i+round)%peGridSize)*submatrix_dim, ((i+round)%peGridSize+1)*submatrix_dim));
                    sm.set(String.format("%d_A",i), pm.get("diagonal_submatrix"));
                    timer.pause();
                    for (int j = 0; j< peGridSize; j++) {
                        pm.add_metrics(10, 1);
                        communications.receive_data(i*peGridSize+j+1, 0);
                        communications.send_data(0, i*peGridSize+j+1, String.format("%d_A",i));
                    }
                    timer.resume();
                }
                System.out.printf("FoxOtto: [%s%s]\r", "#".repeat((int) (((double)(num_iterations*peGridSize+round)/(max_iterations*peGridSize))*80)), "-".repeat((int) ((1.0 - (double)(num_iterations*peGridSize+round)/(max_iterations*peGridSize))*80)));
            }
            timer.pause();
            // SimpleMatrix padded_result = new SimpleMatrix(submatrix_dim * peGridSize, submatrix_dim * peGridSize);
            long max_batch_time = -1;
            pm.add_metrics(0, 1);
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
        System.out.printf("FoxOtto: [%s]%n","#".repeat(80));
        communications.send_instruction(0, new Shutdown());
    }
}
