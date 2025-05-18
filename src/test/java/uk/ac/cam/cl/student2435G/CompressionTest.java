package uk.ac.cam.cl.student2435G;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import uk.ac.cam.cl.student2435G.algorithms.Dijkstra.DijkstraMCU;
import uk.ac.cam.cl.student2435G.graph.CompressedGraph;
import uk.ac.cam.cl.student2435G.graph.Graph;
import uk.ac.cam.cl.student2435G.simulator.Memory;
import uk.ac.cam.cl.student2435G.simulator.Simulator;

public class CompressionTest extends GenericTest {
    @Disabled
    @ParameterizedTest
    @MethodSource(value = "configs")
    void testRandomCompressionMatches(int[] config) throws InterruptedException, ExecutionException, FileNotFoundException {
        Graph g = new Graph(config[0],config[1],true,config[2],config[3]);
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

    @Disabled
    @Test
    void testRealCompressionMatches() throws InterruptedException, ExecutionException, IOException {
        // Graph g = new Graph(21048, 2.061, true, 2381, 9782);
        Graph g = new Graph("input_graphs/cal.cedge", true); //new Graph(100, 2.5, true, 79832, 2104);//new Graph("input_graphs/OL.cedge", true);
        int p = 6;
        Simulator s;
        s = new Simulator(p, g, new DijkstraMCU(), new Memory());
        s.execute();
        SimpleMatrix dijkstra_dist1 = s.get_shared_memory().get("output_dist");
        long edge_count = 0;
        for (int i = 0; i < g.length; i++) {
            for (int j = 0; j < g.length; j++) {
                if (Double.isFinite(g.adjacency.get(i,j)) && i != j) {
                    edge_count++;
                }
            }
        }
        // System.out.println((float)edge_count/(g.length));

        CompressedGraph cg = new CompressedGraph(g);

        edge_count = 0;
        for (int i = 0; i < cg.length; i++) {
            for (int j = 0; j < cg.length; j++) {
                if (Double.isFinite(cg.adjacency.get(i,j)) && i != j) {
                    edge_count++;
                }
            }
        }
        System.out.println((float)edge_count/(cg.length));

        System.out.println(g.length);
        System.out.println(cg.length);

        System.out.println(1.0 - (float)cg.length/g.length);

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
