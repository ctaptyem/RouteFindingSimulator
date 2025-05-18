package uk.ac.cam.cl.ac2499.algorithms.FoxOtto;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.algorithms.CodeBlock;
import uk.ac.cam.cl.ac2499.algorithms.Shutdown;
import uk.ac.cam.cl.ac2499.algorithms.utils.SubMatrix;

public class FoxOttoMCU extends CodeBlock {

    public void run() {
        pm.set("graph", sm.get("graph"));
        int dim = pm.get("graph").getNumRows();
        int submatrix_dim = (int) Math.ceil((double) dim / pe_grid_size);
        pm.set("padded_dist", SimpleMatrix.filled(submatrix_dim * pe_grid_size, submatrix_dim * pe_grid_size, Double.POSITIVE_INFINITY));
        pm.get("padded_dist").insertIntoThis(0, 0, pm.get("graph"));
        for (int i = 0; i < pm.get("padded_dist").getNumCols(); i++) {
            pm.get("padded_dist").set(i,i,0);
        }
        SimpleMatrix padded_pred = SimpleMatrix.filled(submatrix_dim * pe_grid_size, submatrix_dim * pe_grid_size, -1);
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if (Double.isFinite(pm.get("graph").get(i,j)))
                    padded_pred.set(i,j,i);
            }
        }
        int max_iterations = (int)(Math.log(dim+1)/Math.log(2))+1;
        for (int num_iterations = 0; num_iterations < max_iterations; num_iterations++) {
            for (int i = 1; i<pe_grid_size * pe_grid_size + 1; i++) {
                CodeBlock peAlgo = new FoxOttoPE();
                peAlgo.pe_grid_size = pe_grid_size;
                communications.send_instruction(i,peAlgo);
            }

            for (int i = 0; i < pe_grid_size; i++) {
                for(int j = 0; j < pe_grid_size; j++) {
                    int pe_id = i*pe_grid_size+j+1;
                    // pm.set("sub", pm.get("padded_dist").extractMatrix(i*submatrix_dim,(i+1)*submatrix_dim,j*submatrix_dim,(j+1)*submatrix_dim).transpose());
                    pm.set("sub", new SubMatrix(pm.get("padded_dist").extractMatrix(i*submatrix_dim,(i+1)*submatrix_dim,j*submatrix_dim,(j+1)*submatrix_dim), i*submatrix_dim, j*submatrix_dim));
                    communications.send_submatrix(0,pe_id, String.format("%d_B",pe_id), pm.get_submatrix("sub"), sm);
                    pm.set("sub_P", padded_pred.extractMatrix(i*submatrix_dim,(i+1)*submatrix_dim,j*submatrix_dim,(j+1)*submatrix_dim));
                    communications.send_matrix(0,pe_id, String.format("%d_P_init",pe_id), pm.get("sub_P"), sm);
                }
            }
            for (int round = 0; round < pe_grid_size; round++) {
                for (int i = 0; i < pe_grid_size; i++) {
                    pm.set("diagonal_submatrix", new SubMatrix(pm.get("padded_dist").extractMatrix(i*submatrix_dim, (i+1)*submatrix_dim, ((i+round)%pe_grid_size)*submatrix_dim, ((i+round)%pe_grid_size+1)*submatrix_dim), i*submatrix_dim, ((i+round)%pe_grid_size)*submatrix_dim));
                    sm.set(String.format("%d_A_%d", i, round), pm.get_submatrix("diagonal_submatrix"));
                    for (int j = 0; j< pe_grid_size; j++) {
                        communications.receive_data(i*pe_grid_size+j+1, 0);
                        communications.send_data(0, i*pe_grid_size+j+1, String.format("%d_A_%d", i, round));

                    }
                }
                System.out.printf("FoxOtto: [%s%s]\r", "#".repeat((int) (((double)(num_iterations*pe_grid_size+round)/(max_iterations*pe_grid_size))*80)), "-".repeat((int) ((1.0 - (double)(num_iterations*pe_grid_size+round)/(max_iterations*pe_grid_size))*80)));
            }
            for (int i = 0; i < pe_grid_size; i++) {
                for(int j = 0; j < pe_grid_size; j++) {
                    pm.set("sub_C", sm.get(communications.receive_data(i*pe_grid_size+j+1, 0)));
                    pm.get("padded_dist").insertIntoThis(i*submatrix_dim, j*submatrix_dim, pm.get("sub_C"));
                    pm.set("sub_P", sm.get(communications.receive_data(i*pe_grid_size+j+1, 0)));
                    padded_pred.insertIntoThis(i*submatrix_dim, j*submatrix_dim, pm.get("sub_P"));
                }
            }
        }

        pm.set("output_dist", pm.get("padded_dist").extractMatrix(0, dim, 0, dim));
        sm.set("output_dist", pm.get("output_dist"));
        pm.set("output_pred", padded_pred.extractMatrix(0, dim, 0, dim));
        sm.set("output_pred", pm.get("output_pred"));
        for (int i = 1; i<pe_grid_size * pe_grid_size + 1; i++) {
            communications.send_instruction(i, new Shutdown());
        }
        System.out.printf("FoxOtto: [%s]%n","#".repeat(80));
        communications.send_instruction(0, new Shutdown());
    }
}
