package uk.ac.cam.cl.ac2499;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import uk.ac.cam.cl.ac2499.algorithms.Dijkstra.DijkstraMCU;
import uk.ac.cam.cl.ac2499.graph.CompressedGraph;
import uk.ac.cam.cl.ac2499.graph.Graph;
import uk.ac.cam.cl.ac2499.simulator.Memory;
import uk.ac.cam.cl.ac2499.simulator.Simulator;

public class CompressionTest extends GenericTest {
    @Disabled
    @ParameterizedTest
    @MethodSource(value = "configs")
    void testCompressionMatches(int[] config) throws InterruptedException, ExecutionException, FileNotFoundException {
        Graph g = new Graph(config[0],config[1]/10000.0,true,config[2],config[3]);
        int p = config[4];
        Simulator s;
        s = new Simulator(p, g, new DijkstraMCU(), new Memory());
        s.execute();
        SimpleMatrix dijkstra_dist1 = s.get_shared_memory().get("output_dist");
        CompressedGraph cg = new CompressedGraph(g);
        s = new Simulator(p, cg, new DijkstraMCU(), new Memory());
        s.execute();
        SimpleMatrix compressed_dist = s.get_shared_memory().get("output_dist");
        SimpleMatrix compressed_pred = s.get_shared_memory().get("output_pred");
        SimpleMatrix dijkstra_dist2 = cg.decompress(compressed_dist, compressed_pred)[0];

        double[][] mismatch_matrix = mismatch_percent(new SimpleMatrix[]{dijkstra_dist1, dijkstra_dist2});
        
        if (!check_mismatch_matrix(mismatch_matrix)) {
            throw new RuntimeException(String.format("%s\n%s", print_config(config), print_mismatch_matrix(mismatch_matrix)));
        }

    }

    // @Disabled
    @Test
    void testCompressionMatches() throws InterruptedException, ExecutionException, IOException {
        Graph g = new Graph("input_graphs/OL.cedge", false);
        int p = 4;
        Simulator s;
        s = new Simulator(p, g, new DijkstraMCU(), new Memory());
        s.execute();
        SimpleMatrix dijkstra_dist1 = s.get_shared_memory().get("output_dist");
        CompressedGraph cg = new CompressedGraph(g);

        s = new Simulator(p, cg, new DijkstraMCU(), new Memory());
        s.execute();
        SimpleMatrix compressed_dist = s.get_shared_memory().get("output_dist");
        SimpleMatrix compressed_pred = s.get_shared_memory().get("output_pred");
        SimpleMatrix dijkstra_dist2 = cg.decompress(compressed_dist, compressed_pred)[0];

        double[][] mismatch_matrix = mismatch_percent(new SimpleMatrix[]{dijkstra_dist1, dijkstra_dist2});
        
        if (!check_mismatch_matrix(mismatch_matrix)) {
            throw new RuntimeException(String.format("%s", print_mismatch_matrix(mismatch_matrix)));
        }

    }
}
