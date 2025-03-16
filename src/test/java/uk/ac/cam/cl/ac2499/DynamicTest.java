package uk.ac.cam.cl.ac2499;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import uk.ac.cam.cl.ac2499.algorithms.DijkstraMCU;
import uk.ac.cam.cl.ac2499.algorithms.DynamicMCU;

import java.util.Random;


public class DynamicTest {
    public static int[][] configs() {
        int[][] random_seeds =  new int[][]{{73, 6135}, {8804, 1854}, {8224, 2195}, {480, 5607}, {5764, 4112}, {722, 2905}, {4776, 3417}, {6117, 6371}, {9242, 7314}, {4399, 4691}};
        int[] node_counts = new int[]{4};
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

    @ParameterizedTest
    @MethodSource(value = "configs")
    void testFiniteChange(int[] config) throws InterruptedException, ExecutionException, FileNotFoundException {
        System.out.println(String.format("Starting test with config: [%d, %f, %d, %d, %d]", config[0], config[1]/10000.0, config[2], config[3], config[4]));
        Graph g = new Graph(config[0],config[1]/10000.0,true,50.0,20.0,config[2],config[3]);
        int p = config[4];
        Simulator s;
        Memory sm = new Memory();
        s = new Simulator(p, g, new DijkstraMCU(), sm);
        s.execute();
        SimpleMatrix dijkstra_dist = s.get_shared_memory().get("output_dist");
        SimpleMatrix dijkstra_pred = s.get_shared_memory().get("output_pred");
        
        Random rand = new Random(config[2]+config[3]);

        int from_node = rand.nextInt(config[0]);
        int to_node = rand.nextInt(config[0]);
        if (from_node == to_node)
            to_node = (from_node+1)%config[0];
        
        double new_weight = Math.max(rand.nextGaussian(50.0, 20.0), 0.1);
        sm.set("from_node", new SimpleMatrix(new double[][]{{from_node}}));
        sm.set("to_node", new SimpleMatrix(new double[][]{{to_node}}));
        sm.set("old_weight", new SimpleMatrix(new double[][]{{g.adjacency.get(from_node, to_node)}}));
        g.update_edge(from_node, to_node, new_weight, false);
        s = new Simulator(p, g, new DynamicMCU(), sm);
        s.execute();
        g.update_edge(to_node, from_node, new_weight, false);
        sm.set("from_node", new SimpleMatrix(new double[][]{{to_node}}));
        sm.set("to_node", new SimpleMatrix(new double[][]{{from_node}}));
        s = new Simulator(p, g, new DynamicMCU(), sm);
        s.execute();

        SimpleMatrix dynamic_dist = s.get_shared_memory().get("output_dist");
        SimpleMatrix dynamic_pred = s.get_shared_memory().get("output_pred");

        s = new Simulator(p, g, new DijkstraMCU(), sm);
        s.execute();
        dijkstra_dist = s.get_shared_memory().get("output_dist");
        dijkstra_pred = s.get_shared_memory().get("output_pred");

        for (int i = 0; i < g.length; i++) {
            for (int j = 0; j < g.length; j++) {
                double dijkstra_value = dijkstra_dist.get(i,j);
                double dynamic_value = dynamic_dist.get(i,j);
                if (!Double.isNaN(dijkstra_value - dynamic_value) && Math.abs(dijkstra_value - dynamic_value) > 1e-5) {
                    throw new RuntimeException(String.format("Distance mismatch. Dijkstra: %f, Dynamic: %f, Config: [%d, %f, %d, %d, %d, %d, %d, %f]", dijkstra_value, dynamic_value, config[0], config[1]/10000.0, config[2], config[3], config[4], from_node, to_node, new_weight));
                }
                dijkstra_value = dijkstra_pred.get(i,j);
                dynamic_value = dynamic_pred.get(i,j);
                if (!Double.isNaN(dijkstra_value - dynamic_value) && Math.abs(dijkstra_value - dynamic_value) > 1e-5) {
                    throw new RuntimeException(String.format("Predecessor mismatch. Dijkstra: %f, Dynamic: %f, Config: [%d, %f, %d, %d, %d, %d, %d, %f]", dijkstra_value, dynamic_value, config[0], config[1]/10000.0, config[2], config[3], config[4], from_node, to_node, new_weight));
                }
            }
        }
    }
}
