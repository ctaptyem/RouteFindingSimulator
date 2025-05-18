package uk.ac.cam.cl.ac2499;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import uk.ac.cam.cl.ac2499.algorithms.Cannons.CannonsMCU;
import uk.ac.cam.cl.ac2499.algorithms.Dijkstra.DijkstraMCU;
import uk.ac.cam.cl.ac2499.algorithms.Dynamic.DynamicMCU;
import uk.ac.cam.cl.ac2499.algorithms.FoxOtto.FoxOttoMCU;
import uk.ac.cam.cl.ac2499.graph.Graph;
import uk.ac.cam.cl.ac2499.simulator.Memory;
import uk.ac.cam.cl.ac2499.simulator.Simulator;

import java.util.Random;


public class DynamicTest extends GenericTest{

    @Disabled
    @ParameterizedTest
    @MethodSource(value = "configs")
    void testFiniteChange(int[] config) throws InterruptedException, ExecutionException, FileNotFoundException {
        System.out.println(print_config(config));
        Graph g = new Graph(config[0],config[1],true,config[2],config[3]);
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
        
        double percent_change = rand.nextDouble(0.1,1.9);
        sm.set("from_node", from_node);
        sm.set("to_node", to_node);
        sm.set("new_weight", new SimpleMatrix(new double[]{percent_change * g.adjacency.get(from_node, to_node)}));
        sm.set("undirected", 1);
        // sm.set("old_weight", new SimpleMatrix(new double[][]{{g.adjacency.get(from_node, to_node)}}));
        s = new Simulator(p, g, new DynamicMCU(), sm);
        s.execute();

        SimpleMatrix dynamic_dist = s.get_shared_memory().get("output_dist");
        SimpleMatrix dynamic_pred = s.get_shared_memory().get("output_pred");

        s = new Simulator(p, g, new DijkstraMCU(), sm);
        s.execute();
        dijkstra_dist = s.get_shared_memory().get("output_dist");
        dijkstra_pred = s.get_shared_memory().get("output_pred");

        for (int i = 0; i < config[0]; i++) {
            for (int j = 0; j < config[0]; j++) {
                // SimpleMatrix adjacency = s.get_shared_memory().get("graph");
                // adjacency.set(from_node, to_node, new_weight);
                // adjacency.set(to_node, from_node, new_weight);
                String dijkstra_path = reconstruct_path(dijkstra_pred, i, j);
                String dynamic_path = reconstruct_path(dynamic_pred, i, j);
                assertEquals(dijkstra_path, dynamic_path);
            }
        }


        double[][] mismatch_matrix = mismatch_percent(new SimpleMatrix[]{dijkstra_dist, dynamic_dist});

        if (!check_mismatch_matrix(mismatch_matrix))
            throw new RuntimeException(String.format("Distance mismatch\n%sFrom: %d\nTo: %d\nChange: %f\n%s", print_config(config), from_node, to_node, percent_change, print_mismatch_matrix(mismatch_matrix)));

        mismatch_matrix = mismatch_percent(new SimpleMatrix[]{dijkstra_pred, dynamic_pred});
        if (!check_mismatch_matrix(mismatch_matrix))
            throw new RuntimeException(String.format("Predecessor mismatch\n%sFrom: %d\nTo: %d\nChange: %f\n%s", print_config(config), from_node, to_node, percent_change, print_mismatch_matrix(mismatch_matrix)));
    }


    @Disabled
    @ParameterizedTest
    @MethodSource(value = "configs")
    void testInfiniteChange(int[] config) throws InterruptedException, ExecutionException, FileNotFoundException {
        if (config[0] == 2) return;
        // System.out.println(String.format("Starting test with config: [%d, %f, %d, %d, %d]", config[0], config[1], config[2], config[3], config[4]));
        System.out.println(print_config(config));
        Graph g = new Graph(config[0],config[1],true,config[2],config[3]);
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
        
        double percent_change = Double.POSITIVE_INFINITY;
        sm.set("from_node", from_node);
        sm.set("to_node", to_node);
        double new_weight = percent_change * g.adjacency.get(from_node, to_node);
        sm.set("new_weight", new SimpleMatrix(new double[]{new_weight}));
        sm.set("undirected", 1);
        // g.update_edge(from_node, to_node, percent_change, false);
        s = new Simulator(p, g, new DynamicMCU(), sm);
        s.execute();
        // sm.set("from_node", new SimpleMatrix(new double[][]{{to_node}}));
        // sm.set("to_node", new SimpleMatrix(new double[][]{{from_node}}));
        // s = new Simulator(p, g, new DynamicMCU(), sm);
        // s.execute();

        SimpleMatrix dynamic_dist = s.get_shared_memory().get("output_dist");
        SimpleMatrix dynamic_pred = s.get_shared_memory().get("output_pred");

        s = new Simulator(p, g, new DijkstraMCU(), sm);
        s.execute();
        dijkstra_dist = s.get_shared_memory().get("output_dist");
        dijkstra_pred = s.get_shared_memory().get("output_pred");

        for (int i = 0; i < config[0]; i++) {
            for (int j = 0; j < config[0]; j++) {
                // SimpleMatrix adjacency = s.get_shared_memory().get("graph");
                // adjacency.set(from_node, to_node, new_weight);
                // adjacency.set(to_node, from_node, new_weight);
                String dijkstra_path = reconstruct_path(dijkstra_pred, i, j);
                String dynamic_path = reconstruct_path(dynamic_pred, i, j);
                assertEquals(dijkstra_path, dynamic_path, String.format("<%s> does not match <%s> for %d to %d", dijkstra_path, dynamic_path, i, j));
            }
        }

        double[][] mismatch_matrix = mismatch_percent(new SimpleMatrix[]{dijkstra_dist, dynamic_dist});

        if (!check_mismatch_matrix(mismatch_matrix))
            throw new RuntimeException(String.format("Distance mismatch\n%sFrom: %d\nTo: %d\nChange: %f\n%s", print_config(config), from_node, to_node, percent_change, print_mismatch_matrix(mismatch_matrix)));

        mismatch_matrix = mismatch_percent(new SimpleMatrix[]{dijkstra_pred, dynamic_pred});
        if (!check_mismatch_matrix(mismatch_matrix))
            throw new RuntimeException(String.format("Predecessor mismatch\n%sFrom: %d\nTo: %d\nChange: %f\n%s", print_config(config), from_node, to_node, percent_change, print_mismatch_matrix(mismatch_matrix)));
    }
}
