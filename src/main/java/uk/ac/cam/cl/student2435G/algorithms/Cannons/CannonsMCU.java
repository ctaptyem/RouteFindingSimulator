package uk.ac.cam.cl.student2435G.algorithms.Cannons;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.student2435G.algorithms.CodeBlock;
import uk.ac.cam.cl.student2435G.algorithms.Shutdown;
import uk.ac.cam.cl.student2435G.algorithms.utils.SubMatrix;

public class CannonsMCU extends CodeBlock{
    public void run() {
        pm.set("graph", sm.get("graph"));
        int dim = pm.get("graph").getNumRows();
        int submatrix_dim = (int) Math.ceil((double) dim / pe_grid_size);
        pm.set("padded_dist", SimpleMatrix.filled(submatrix_dim * pe_grid_size, submatrix_dim * pe_grid_size, Double.POSITIVE_INFINITY));
        pm.set("padded_pred", SimpleMatrix.filled(submatrix_dim * pe_grid_size, submatrix_dim * pe_grid_size, -1));
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if (Double.isFinite(pm.get("graph").get(i,j)))
                    pm.get("padded_pred").set(i,j,i);
            }
        }

        for (int i = 0; i < pm.get("padded_dist").getNumCols(); i++) pm.get("padded_dist").set(i,i,0);
        pm.get("padded_dist").insertIntoThis(0, 0, pm.get("graph"));

        int max_iterations = (int)(Math.log(dim+1)/Math.log(2))+1;
        for (int num_iterations = 0; num_iterations < max_iterations; num_iterations++) {

            for (int i = 1; i<pe_grid_size * pe_grid_size + 1; i++) {
                CodeBlock peAlgo = new CannonsPE();
                peAlgo.pe_grid_size = pe_grid_size;
                communications.send_instruction(i,peAlgo);
            }

            for (int i = 0; i < pe_grid_size; i++) {
                for(int j = 0; j < pe_grid_size; j++) {
                    int k = (i + j) % pe_grid_size;
                    int pe_id = i*pe_grid_size+j+1;
                    pm.set("sub_A", new SubMatrix(pm.get("padded_dist").extractMatrix(i*submatrix_dim,(i+1)*submatrix_dim,k*submatrix_dim,(k+1)*submatrix_dim), i*submatrix_dim, k*submatrix_dim));
                    pm.set("sub_B", new SubMatrix(pm.get("padded_dist").extractMatrix(k*submatrix_dim,(k+1)*submatrix_dim,j*submatrix_dim,(j+1)*submatrix_dim), k*submatrix_dim, j*submatrix_dim));
                    communications.send_submatrix(0,pe_id, String.format("%d_A_init",pe_id), pm.get_submatrix("sub_A"), sm);
                    communications.send_submatrix(0,pe_id, String.format("%d_B_init",pe_id), pm.get_submatrix("sub_B"), sm);
                    pm.set("sub_P", pm.get("padded_pred").extractMatrix(i*submatrix_dim,(i+1)*submatrix_dim,j*submatrix_dim,(j+1)*submatrix_dim));
                    communications.send_matrix(0,pe_id, String.format("%d_P_init",pe_id), pm.get("sub_P"), sm);
                }
            }
            pm.set("padded_dist", SimpleMatrix.filled(submatrix_dim * pe_grid_size, submatrix_dim * pe_grid_size, Double.POSITIVE_INFINITY));
            for (int i = 0; i < pm.get("padded_dist").getNumCols(); i++) pm.get("padded_dist").set(i,i,0);
            for (int i = 0; i < pe_grid_size; i++) {
                for(int j = 0; j < pe_grid_size; j++) {
                    communications.receive_data(i*pe_grid_size+j+1, 0);
                    pm.set("sub_C", sm.get(String.format("%d_C",i*pe_grid_size+j+1)));
                    pm.get("padded_dist").insertIntoThis(i*submatrix_dim, j*submatrix_dim, pm.get("sub_C"));
                    communications.receive_data(i*pe_grid_size+j+1, 0);
                    pm.set("sub_P", sm.get(String.format("%d_P",i*pe_grid_size+j+1)));
                    pm.get("padded_pred").insertIntoThis(i*submatrix_dim, j*submatrix_dim, pm.get("sub_P"));
                }
                System.out.printf("Cannon: [%s%s]\r", "#".repeat((int) (((double)(num_iterations*pe_grid_size+i)/(max_iterations*pe_grid_size))*80)), "-".repeat((int) ((1.0 - (double)(num_iterations*pe_grid_size+i)/(max_iterations*pe_grid_size))*80)));
            }
        }

        pm.set("output_dist", pm.get("padded_dist").extractMatrix(0, dim, 0, dim));
        sm.set("output_dist", pm.get("output_dist"));
        pm.set("output_pred", pm.get("padded_pred").extractMatrix(0, dim, 0, dim));
        sm.set("output_pred", pm.get("output_pred"));
        // System.out.println(pm.get("output_pred"));
        for (int i = 1; i<pe_grid_size * pe_grid_size + 1; i++) {
            pm.add_metrics(4, 1);
            communications.send_instruction(i, new Shutdown());
        }
        System.out.printf("Cannons: [%s]%n","#".repeat(80));
        communications.send_instruction(0, new Shutdown());
    }
}
