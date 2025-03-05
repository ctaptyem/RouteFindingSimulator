package uk.ac.cam.cl.ac2499;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.algorithms.CannonsMCU;
import uk.ac.cam.cl.ac2499.algorithms.DijkstraMCU;
import uk.ac.cam.cl.ac2499.algorithms.FoxOttoMCU;

import java.util.Random;

public class StaticTest {
    public static int[][] configs() {
        int[][] random_seeds =  new int[][]{{73, 6135}, {8804, 1854}, {8224, 2195}, {480, 5607}, {5764, 4112}, {722, 2905}, {4776, 3417}, {6117, 6371}, {9242, 7314}, {4399, 4691}};
        // int[][] random_seeds = new int[1000][2];
        // Random rand = new Random(1940);
        // for (int i = 0 ; i < 1000; i++) {
        //     // random_seeds[i][0] = rand.nextInt(10000);
        //     random_seeds[i][0] = 1223;

        //     // random_seeds[i][1] = rand.nextInt(10000);
        //     random_seeds[i][1] = 3515;

        // }
        // random_seeds = new int[][]{{1223, 3515}};
        // int[][] random_seeds =  new int[][]{{722, 2905}};
        int[] node_counts = new int[]{2,20,200};
        // int[] node_counts = new int[]{6};
        // double[] edge_proportions = new double[]{0.1};
        double[] edge_proportions = new double[]{0.1, 0.3, 0.5, 0.7, 0.9};
        int[] peGridSizes = new int[]{4}; // 1 2 4 8
        int[][] configs = new int[random_seeds.length * node_counts.length * edge_proportions.length * peGridSizes.length][5];
        int idx = 0;
        for (int i = 0; i < node_counts.length; i++) {
            for (int j = 0; j < edge_proportions.length; j++) {
                for (int k = 0; k < random_seeds.length; k++) {
                    for (int l = 0; l < peGridSizes.length; l++) {
                        configs[idx] = new int[]{node_counts[i], (int) (10000*edge_proportions[j]), random_seeds[k][0], random_seeds[k][1], peGridSizes[l]};
                        idx++;
                    }
                }
            }
        }
        return configs;
    }

    

    @Disabled
    @ParameterizedTest
    @MethodSource(value = "configs")
    void testInfiniteMatches(int[] config) throws InterruptedException, ExecutionException, FileNotFoundException {
        Graph g = new Graph(config[0],config[1]/10000.0,true,50.0,20.0,config[2],config[3]);
        Parameters p = new Parameters(config[4]);
        Simulator s;
        s = new Simulator(p, g, new DijkstraMCU(), new Memory());
        s.execute();
        SimpleMatrix dijkstra_dist = s.getSharedMemory().get("output_dist");
        s = new Simulator(p, g, new CannonsMCU(), new Memory());
        s.execute();
        SimpleMatrix cannons_dist = s.getSharedMemory().get("output_dist");
        s = new Simulator(p, g, new FoxOttoMCU(), new Memory());
        s.execute();
        SimpleMatrix foxotto_dist = s.getSharedMemory().get("output_dist");

        for (int i = 0; i < g.length; i++) {
            for (int j = 0; j < g.length; j++) {
                double dijkstra_value = dijkstra_dist.get(i,j);
                double foxotto_value = foxotto_dist.get(i,j);
                double cannons_value = cannons_dist.get(i,j);
                if (Double.isInfinite(dijkstra_value + cannons_value + foxotto_value) && (Double.isFinite(dijkstra_value) || Double.isFinite(foxotto_value) || Double.isFinite(cannons_value))) {
                    PrintWriter pw = new PrintWriter("testing/output/weird_case.txt");
                    pw.println(dijkstra_dist);
                    pw.println();
                    pw.println(foxotto_dist);
                    pw.println();
                    pw.println(cannons_dist);
                    pw.println();
                    pw.println(g.adjacency);
                    pw.println();
                    pw.close();
                    throw new RuntimeException(String.format("Dijkstra: %f, FoxOtto: %f, Cannons: %f, Config: [%d, %f, %d, %d, %d]", dijkstra_value, foxotto_value, cannons_value, config[0], config[1]/10000.0, config[2], config[3], config[4]));
                }
            }
        }
    }

    // @Disabled
    @ParameterizedTest
    @MethodSource(value = "configs")
    void testAllMatch(int[] config) throws InterruptedException, ExecutionException, FileNotFoundException {
        Graph g = new Graph(config[0],config[1],true,50.0,20.0,config[2],config[3]);
        Parameters p = new Parameters(config[4]);
        Simulator s;
        s = new Simulator(p, g, new DijkstraMCU(), new Memory());
        s.execute();
        SimpleMatrix dijkstra_dist = s.getSharedMemory().get("output_dist");
        s = new Simulator(p, g, new CannonsMCU(), new Memory());
        s.execute();
        SimpleMatrix cannons_dist = s.getSharedMemory().get("output_dist");
        s = new Simulator(p, g, new FoxOttoMCU(), new Memory());
        s.execute();
        SimpleMatrix foxotto_dist = s.getSharedMemory().get("output_dist");
        boolean mismatch_1 = false;
        boolean mismatch_2 = false;
        boolean mismatch_3 = false;


        for (int i = 0; i < g.length; i++) {
            for (int j = 0; j < g.length; j++) {
                double dijkstra_value = dijkstra_dist.get(i,j);
                double foxotto_value = foxotto_dist.get(i,j);
                double cannons_value = cannons_dist.get(i,j);
                if (!Double.isNaN(dijkstra_value - foxotto_value) && Math.abs(dijkstra_value - foxotto_value) > 1e-5) {
                    mismatch_1 = true;
                }
                if (!Double.isNaN(dijkstra_value - cannons_value) && Math.abs(dijkstra_value - cannons_value) > 1e-5) {
                    mismatch_2 = true;
                }
                if (!Double.isNaN(foxotto_value - cannons_value) && Math.abs(foxotto_value - cannons_value) > 1e-5) {
                    mismatch_3 = true;
                }

                if ((mismatch_1 || mismatch_2 || mismatch_3)){//} && (dijkstra_value == 0.0 && foxotto_value != 0.0)) {
                    // PrintWriter pw = new PrintWriter("testing/output/weird_case.txt");
                    // pw.println(dijkstra_dist);
                    // pw.println();
                    // pw.println(foxotto_dist);
                    // pw.println();
                    // pw.println(cannons_dist);
                    // pw.println();
                    // pw.println(g.adjacency);
                    // pw.println();
                    // pw.close();
                    throw new RuntimeException(String.format("Dijkstra: %f, FoxOtto: %f, Cannons: %f, Config: [%d, %f, %d, %d, %d]", dijkstra_value, foxotto_value, cannons_value, config[0], config[1]/10000.0, config[2], config[3], config[4]));
                }
                
                // assertAll(
                //     () -> assertTrue(Double.isNaN(dijkstra_value - foxotto_value) || Math.abs(dijkstra_value - foxotto_value) < 1e-5, String.format("Dijkstra: %f, FoxOtto: %f", dijkstra_value, foxotto_value)), 
                //     () -> assertTrue(Double.isNaN(dijkstra_value - cannons_value) || Math.abs(dijkstra_value - cannons_value) < 1e-5, String.format("Dijkstra: %f, Cannons: %f", dijkstra_value, cannons_value)),
                //     () -> assertTrue(Double.isNaN(foxotto_value - cannons_value) || Math.abs(foxotto_value - cannons_value) < 1e-5, String.format("FoxOtto: %f, Cannons: %f", foxotto_value, cannons_value))
                // );
            }
        }
        // final boolean m1 = mismatch_1;
        // final boolean m2 = mismatch_2;
        // final boolean m3 = mismatch_3;
        // assertAll(
        //     () -> assertFalse(m1, String.format("Dijkstra and FoxOtto mismatch. Config: [%d, %d, %d, %d, %d]", config[0], config[1], config[2], config[3], config[4])),
        //     () -> assertFalse(m2, String.format("Dijkstra and Cannons mismatch. Config: [%d, %d, %d, %d, %d]", config[0], config[1], config[2], config[3], config[4])),
        //     () -> assertFalse(m3, String.format("FoxOtto and Cannons mismatch. Config: [%d, %d, %d, %d, %d]", config[0], config[1], config[2], config[3], config[4]))
        // );


    }
}
