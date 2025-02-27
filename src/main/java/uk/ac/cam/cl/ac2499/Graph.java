package uk.ac.cam.cl.ac2499;


import org.ejml.simple.SimpleMatrix;

import java.io.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

public class Graph { 
    public class Edge {
        int startNodeId;
        int endNodeId;
        double length;
        public Edge(int startNodeId, int endNodeId, double length) {
            this.startNodeId = startNodeId;
            this.endNodeId = endNodeId;
            this.length = length;
        }
    }
    public final SimpleMatrix adjacency;
    public int length;
    public boolean undirected;
    
    public Graph(String filepath, boolean undirected) throws IOException {
        this.undirected = undirected;
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        ArrayList<Edge> data = new ArrayList<>();
        int max_node_id = -1;
        try {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" ");
                // Assuming the format is (Edge ID, Start Node ID, End Node ID, Distance)
                if (tokens.length == 4) {
                    // int edgeId = Integer.parseInt(tokens[0].trim());
                    int startNodeId = Integer.parseInt(tokens[1].trim());
                    int endNodeId = Integer.parseInt(tokens[2].trim());
                    float length = Float.parseFloat(tokens[3].trim());
                    data.add(new Edge(startNodeId, endNodeId, length));

                    if (startNodeId > max_node_id) max_node_id = startNodeId;
                    if (endNodeId > max_node_id) max_node_id = endNodeId;
                }
            }
        } finally {
            br.close();
        }

        length = max_node_id;
        adjacency = SimpleMatrix.filled(max_node_id+1, max_node_id+1, Double.POSITIVE_INFINITY);
        for (int i = 0; i <= max_node_id; i++) adjacency.set(i,i,0.0);
        for (Edge e : data) {
            adjacency.set(e.startNodeId, e.endNodeId, e.length);
            if (undirected) adjacency.set(e.endNodeId, e.startNodeId, e.length);
        }
    }

    public Graph(int node_count, int edge_count, boolean undirected, double weight_mean, double weight_std, long edge_seed, long weight_seed) {
        this.undirected = undirected;
        Random edge_rand = new Random(edge_seed);
        Random weight_rand = new Random(weight_seed);
        if (edge_count <= ((node_count * node_count)/2)) { // sparse graph
            adjacency = SimpleMatrix.filled(node_count, node_count, Double.POSITIVE_INFINITY);
            for (int i = 0; i < node_count; i++) adjacency.set(i,i,0);
            edge_count -= node_count;
            while (edge_count > 0) {
                int node_A = edge_rand.nextInt(node_count);
                int node_B = edge_rand.nextInt(node_count);
                while (Double.isFinite(adjacency.get(node_A, node_B)) || node_A == node_B) {
                    node_A = edge_rand.nextInt(node_count);
                    node_B = edge_rand.nextInt(node_count);
                }
                double weight = Math.max(weight_rand.nextGaussian(weight_mean, weight_std), 0.0);
                adjacency.set(node_A, node_B, weight);
                if (undirected) {
                    adjacency.set(node_B, node_A, weight);
                }
                edge_count--;
            }
        } else { // dense graph
            adjacency = new SimpleMatrix(node_count, node_count);
            for (int i = 0; i < node_count; i++) adjacency.set(i,i,0);
            for (int i = 0; i < node_count * node_count; i++) {
                adjacency.set(i, Math.max(weight_rand.nextGaussian(weight_mean, weight_std), 0.0));
            }
            int anti_edges = node_count * node_count - edge_count;
            while (anti_edges > 0) {
                int node_A = edge_rand.nextInt(node_count);
                int node_B = edge_rand.nextInt(node_count);
                while (Double.isInfinite(adjacency.get(node_A, node_B)) || node_A == node_B) {
                    node_A = edge_rand.nextInt(node_count);
                    node_B = edge_rand.nextInt(node_count);
                }
                adjacency.set(node_A, node_B, Double.POSITIVE_INFINITY);
                if (undirected) {
                    adjacency.set(node_B, node_A, Double.POSITIVE_INFINITY);
                }
                anti_edges--;
            }
        }


    }

    public void update_edge(int node_A, int node_B, double new_weight) {
        adjacency.set(node_A, node_B, new_weight);
        if (undirected) adjacency.set(node_B, node_A, new_weight);
    }
}
