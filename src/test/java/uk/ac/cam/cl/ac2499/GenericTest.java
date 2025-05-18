package uk.ac.cam.cl.ac2499;

import org.ejml.simple.SimpleMatrix;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import java.util.Stack;

public class GenericTest {
    public static int[][] configs() {
        int[][] random_seeds =  new int[][]{{73, 613}, {8804, 1854}, {8224, 2195}, {480, 5607}, {5764, 4112}, {722, 2905}, {4776, 3417}, {6117, 6371}, {9242, 7314}, {4399, 4691}};

        int[] node_counts = new int[]{5, 20, 200};
        int[] avg_degrees = new int[]{2,4,8,16};
        int[] peGridSizes = new int[]{2,4}; // 1 2 4 8
        int[][] configs = new int[random_seeds.length * node_counts.length * avg_degrees.length * peGridSizes.length][5];
        int idx = 0;
        for (int i = 0; i < node_counts.length; i++) {
            for (int j = 0; j < avg_degrees.length; j++) {
                for (int k = 0; k < random_seeds.length; k++) {
                    for (int l = 0; l < peGridSizes.length; l++) {
                        configs[idx] = new int[]{node_counts[i], avg_degrees[j], random_seeds[k][0], random_seeds[k][1], peGridSizes[l]};
                        idx++;
                    }
                }
            }
        }
        return configs;
    }

    public double[][] mismatch_percent(SimpleMatrix[] results) {
        int length = results.length;
        int mat_length = results[0].getNumElements();
        double[][] output = new double[length][length];
        for (int mat1 = 0; mat1 < length; mat1++) {
            output[mat1][mat1] = 0.0;
            for (int mat2 = mat1+1; mat2 < length; mat2++) {
                int mismatches = 0;
                for (int idx = 0; idx < mat_length; idx++) {
                    double value1 = results[mat1].get(idx);
                    double value2 = results[mat2].get(idx);
                    if (!Double.isNaN(value1 - value2) && Math.abs(value1 - value2) > 1e-5)
                        mismatches++;
                }
                double mismatch_percent = Math.round((100.0*mismatches) / mat_length)/100.0;
                output[mat1][mat2] = mismatch_percent;
                output[mat2][mat1] = mismatch_percent;
            }
        }
        return output;
    }

    public boolean check_mismatch_matrix(double[][] mismatch_output) {
        for (int i = 0; i < mismatch_output.length; i++) {
            for (int j = i+1; j < mismatch_output.length; j++) {
                if (mismatch_output[i][j] != 0.0) return false;
            }
        }
        return true;
    }

    public String print_mismatch_matrix(double[][] mismatch_output) {
        String output = "";
        for (int i = 0; i < mismatch_output.length; i++) {
            for (int j = 0; j < mismatch_output.length; j++) {
                output+=String.format("%.2f,", mismatch_output[i][j]);
            }
            output+="\n";
        }
        return output;
    }

    public String print_config(int[] config) {
        return String.format("Node count: %d\nAvg Degree: %d\nEdge seed: %d\nWeight seed: %d\nPE grid size: %d\n", config[0], config[1], config[2], config[3], config[4]);
    }

    public String reconstruct_path(SimpleMatrix pred, int node_A, int node_B) {
        String result = ""; //String.format("%d", node_A);
        Deque<int[]> stack = new ArrayDeque<>();
        stack.add(new int[]{node_A, node_B});
        while (!stack.isEmpty()) {
            int[] nodes = stack.pop();
            int middle = (int) pred.get(nodes[0], nodes[1]);
            if (nodes[0] == middle) {
                result += String.format("%d,", nodes[0]);
                continue;
            }
            if (middle == -1) return "";
            stack.add(new int[]{middle, nodes[1]});
            stack.add(new int[]{nodes[0], middle});
        }

        return result+String.format("%d",node_B);
    }
}
