package uk.ac.cam.cl.ac2499.graph;


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
    public SimpleMatrix adjacency;
    public int length;
    public boolean undirected;
    public String descriptor_string;
    public boolean is_compressed;
    
    public Graph(String filepath, boolean undirected) throws IOException {
        this.undirected = undirected;
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        try {
            String line = br.readLine();
            String[] tokens = line.split(" ");
            if (tokens.length == 1) {
                // Graph file format is descriptor string followed by comma-separated adjacency array
                tokens = line.split(",");
                this.length = Integer.parseInt(tokens[0].trim());
                this.descriptor_string = line.trim();
                adjacency = new SimpleMatrix(length, length);
                for (int i = 0 ; i < length; i++) {
                    line = br.readLine();
                    tokens = line.split(",");
                    for (int j = 0; j < length; j++) {
                        double edge_weight = Double.POSITIVE_INFINITY;
                        if (!tokens[j].equals("inf")) edge_weight = Double.parseDouble(tokens[j]);
                        adjacency.set(i,j,edge_weight);
                    }
                }
            } else if (tokens.length == 4) {
                // Graph format is a list of edges, each in the form "edge_id start_node_id end_node_id length"
                ArrayList<Edge> data = new ArrayList<>();
                int max_node_id = -1;

                while (line != null) {
                    int start_node_id = Integer.parseInt(tokens[1].trim());
                    int end_node_id = Integer.parseInt(tokens[2].trim());
                    float length = Float.parseFloat(tokens[3].trim());
                    data.add(new Edge(start_node_id, end_node_id, length));

                    if (start_node_id > max_node_id) max_node_id = start_node_id;
                    if (end_node_id > max_node_id) max_node_id = end_node_id;
                    line = br.readLine();
                    if (line != null) tokens = line.split(" ");
                }

                this.length = max_node_id+1;
                int edge_count = undirected ? 2 * data.size() : data.size();
                this.descriptor_string = String.format("%d,%f,%f,%b,null,null", length, (float)edge_count/length, (float)edge_count/(length * length - length), undirected);
                adjacency = SimpleMatrix.filled(length, length, Double.POSITIVE_INFINITY);
                for (int i = 0; i <= max_node_id; i++) adjacency.set(i,i,0.0);
                for (Edge e : data) {
                    adjacency.set(e.startNodeId, e.endNodeId, e.length);
                    if (undirected) adjacency.set(e.endNodeId, e.startNodeId, e.length);
                }
            }
        } finally {
            br.close();
        }

        
    }

    public Graph(int node_count, double avg_degree, boolean undirected, long edge_seed, long weight_seed) {
        double edge_percent = Math.min(avg_degree/(node_count - 1),1.0);
        this.descriptor_string = String.format("%d,%f,%f,%b,%d,%d", node_count, avg_degree, edge_percent, undirected, edge_seed, weight_seed); 
        // if (edge_percent < 0.0 || edge_percent > 1.0) {
        //     throw new RuntimeException("Percentage of possible edges should be between 0.0 and 1.0");
        // } 
        this.length = node_count;
        this.undirected = undirected;
        Random edge_rand = new Random(edge_seed);
        Random weight_rand = new Random(weight_seed);
        if (edge_percent < 0.5) { // sparse graph
            adjacency = SimpleMatrix.filled(node_count, node_count, Double.POSITIVE_INFINITY);
            for (int i = 0; i < node_count; i++) adjacency.set(i,i,0);
            int edge_count = (int) (edge_percent * ((node_count * node_count - node_count) / (undirected ? 2 : 1)));
            while (edge_count > 0) {
                int node_A = edge_rand.nextInt(node_count);
                int node_B = edge_rand.nextInt(node_count);
                while (Double.isFinite(adjacency.get(node_A, node_B)) || node_A == node_B) {
                    node_A = edge_rand.nextInt(node_count);
                    node_B = edge_rand.nextInt(node_count);
                }
                double weight = Math.max(weight_rand.nextExponential(), 0.0001);
                adjacency.set(node_A, node_B, weight);
                if (undirected) {
                    adjacency.set(node_B, node_A, weight);
                }
                edge_count--;
            }
        } else { // dense graph
            adjacency = new SimpleMatrix(node_count, node_count);
            if (undirected) {
                for (int i = 0; i < node_count; i++) {
                    for (int j = i+1; j < node_count; j++) {
                        adjacency.set(i, j, Math.max(weight_rand.nextExponential(), 0.0001));
                    }
                }
                adjacency = adjacency.plus(adjacency.transpose());
            } else {
                for (int i = 0; i < node_count * node_count; i++)
                    adjacency.set(i, Math.max(weight_rand.nextExponential(), 0.0001));
            }
            
            for (int i = 0; i < node_count; i++) adjacency.set(i,i,0);
            int anti_edges = (int) ((1.0 - edge_percent) * ((node_count * node_count - node_count) / (undirected ? 2 : 1)));
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

    public Graph(SimpleMatrix adjacency, int length, boolean undirected, String descriptor_string) {
        this.adjacency = adjacency;
        this.length = length;
        this.undirected = undirected;
        this.descriptor_string = descriptor_string;
    }

    public void update_edge(int node_A, int node_B, double new_weight) {
        adjacency.set(node_A, node_B, new_weight);
        if (undirected) adjacency.set(node_B, node_A, new_weight);
    }

    public void update_edge(int node_A, int node_B, double new_weight, boolean undirected) {
        adjacency.set(node_A, node_B, new_weight);
        if (undirected) adjacency.set(node_B, node_A, new_weight);
    }

    public String get_descriptor() {
        return descriptor_string;
    }
}
