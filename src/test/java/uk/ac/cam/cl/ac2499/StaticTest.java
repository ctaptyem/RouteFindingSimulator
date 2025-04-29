package uk.ac.cam.cl.ac2499;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.algorithms.Cannons.CannonsMCU;
import uk.ac.cam.cl.ac2499.algorithms.Dijkstra.DijkstraMCU;
import uk.ac.cam.cl.ac2499.algorithms.FoxOtto.FoxOttoMCU;
import uk.ac.cam.cl.ac2499.graph.Graph;
import uk.ac.cam.cl.ac2499.simulator.Memory;
import uk.ac.cam.cl.ac2499.simulator.Simulator;


public class StaticTest extends GenericTest{

    @Disabled
    @ParameterizedTest
    @MethodSource(value = "configs")
    void testInfiniteMatches(int[] config) throws InterruptedException, ExecutionException, FileNotFoundException {
        Graph g = new Graph(config[0],config[1]/10000.0,true,config[2],config[3]);
        int p = config[4];
        Simulator s;
        s = new Simulator(p, g, new DijkstraMCU(), new Memory());
        s.execute();
        SimpleMatrix dijkstra_dist = s.get_shared_memory().get("output_dist");
        s = new Simulator(p, g, new CannonsMCU(), new Memory());
        s.execute();
        SimpleMatrix cannons_dist = s.get_shared_memory().get("output_dist");
        s = new Simulator(p, g, new FoxOttoMCU(), new Memory());
        s.execute();
        SimpleMatrix foxotto_dist = s.get_shared_memory().get("output_dist");

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

    @Disabled
    @ParameterizedTest
    @MethodSource(value = "configs")
    void testAllMatch(int[] config) throws InterruptedException, ExecutionException, FileNotFoundException {
        Graph g = new Graph(config[0],config[1]/10000.0,true,config[2],config[3]);
        int p = config[4];
        Simulator s;
        s = new Simulator(p, g, new DijkstraMCU(), new Memory());
        s.execute();
        SimpleMatrix dijkstra_dist = s.get_shared_memory().get("output_dist");
        s = new Simulator(p, g, new CannonsMCU(), new Memory());
        s.execute();
        SimpleMatrix cannons_dist = s.get_shared_memory().get("output_dist");
        s = new Simulator(p, g, new FoxOttoMCU(), new Memory());
        s.execute();
        SimpleMatrix foxotto_dist = s.get_shared_memory().get("output_dist");

        double[][] mismatch_matrix = mismatch_percent(new SimpleMatrix[]{dijkstra_dist, cannons_dist, foxotto_dist});
        if (!check_mismatch_matrix(mismatch_matrix)) {
            throw new RuntimeException(String.format("%s\n%s", print_config(config), print_mismatch_matrix(mismatch_matrix)));
        }

    }
}
