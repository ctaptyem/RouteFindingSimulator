package uk.ac.cam.cl.ac2499;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.algorithms.CannonsMCU;
import uk.ac.cam.cl.ac2499.algorithms.DijkstraMCU;
import uk.ac.cam.cl.ac2499.algorithms.FoxOttoMCU;

public class ManualTest {
    void testAlgorithms() throws IOException, InterruptedException, ExecutionException {
        Graph g = new Graph("testing/input/zerod_example_2.txt", false);
        int p = 1;
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
                assertAll(
                    () -> assertEquals(dijkstra_value, foxotto_value, "Distances calculated by Dijkstra does not match FoxOtto"), 
                    () -> assertEquals(dijkstra_value, cannons_value, "Distances calculated by Dijkstra does not match Cannon's"),
                    () -> assertEquals(foxotto_value, cannons_value, "Distances calculated by FoxOtto does not match Cannon's")
                );
            }
        }
    }
}
