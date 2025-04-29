package uk.ac.cam.cl.ac2499.graph;

import java.util.ArrayList;
import java.util.HashMap;

import org.ejml.simple.SimpleMatrix;

public class CompressedGraph extends Graph{

    class Arc {
        ArrayList<Integer> nodes;
        ArrayList<Double> weights;
        double total_weight;

        public Arc(int node_A, int node_B, int node_C, double weight_AB, double weight_BC) {
            this.nodes = new ArrayList<>();
            nodes.add(node_A);
            nodes.add(node_B);
            nodes.add(node_C);
            this.weights = new ArrayList<>();
            weights.add(weight_AB);
            weights.add(weight_BC);
            total_weight = weight_AB + weight_BC;
        }
        public int get_end() {
            return nodes.getLast();
        }
        public int get_start() {
            return nodes.getFirst();
        }
        public double get_weight() {
            return total_weight;
        }

        public void append(int node, double weight) {
            nodes.add(node);
            weights.add(weight);
            total_weight += weight;
        }
        public void prepend(int node, double weight) {
            nodes.addFirst(node);
            weights.addFirst(weight);
            total_weight += weight;
        }
        public void join(Arc other) {
            nodes.addAll(other.nodes.subList(1, other.nodes.size()));
            weights.addAll(other.weights);
            total_weight += other.total_weight;
        }
    }

    HashMap<Integer, Arc> starts_with;
    HashMap<Integer, Arc> ends_with;
    int old_length;
    int[] decompressed_name;

    public CompressedGraph(Graph g) {
        super(g.adjacency, g.length, g.undirected, g.descriptor_string);
        starts_with = new HashMap<>();
        ends_with = new HashMap<>();
        is_compressed = true;
        compress();
    }

    private boolean edge_exists(int from, int i, int to, SimpleMatrix adj) {
        Arc tail = starts_with.get(i);
        Arc head = ends_with.get(i);
        return Double.isFinite(adjacency.get(head == null? from : head.get_start(), tail == null? to : tail.get_end()));
    }

    private void update_arcs(int from, int i, int to, SimpleMatrix adj) {
        Arc tail = starts_with.get(i);
        Arc head = ends_with.get(i);
        if (tail == null && head == null) {
            Arc new_edge = new Arc(from, i, to, adj.get(from, i), adj.get(i,to));
            starts_with.put(from, new_edge);
            ends_with.put(to, new_edge);
        } else if (tail == null) {
            ends_with.remove(i);
            head.append(to, adj.get(i, to));
            ends_with.put(to, head);
        } else if (head == null) {
            starts_with.remove(i);
            tail.prepend(from, adj.get(from, i));
            starts_with.put(from, tail);
        } else {
            head.join(tail);
            starts_with.remove(i);
            ends_with.remove(i);
            ends_with.put(tail.get_end(), head);
        }
    }
    
    public void compress() {
        int n = adjacency.getNumCols();
        ArrayList<Integer> kept_nodes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            ArrayList<Integer> in = new ArrayList<>();
            ArrayList<Integer> out = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                if (Double.isFinite(adjacency.get(i,j)) && i!=j)
                    out.add(j);
                if (Double.isFinite(adjacency.get(j,i)) && i!=j)
                    in.add(j);
            }
            if (in.size() == 1 && out.size() == 1 && in.get(0).intValue() != out.get(0).intValue()) {
                // directed case
                int from = in.get(0);
                int to = out.get(0);
                if (!edge_exists(from, i, to, adjacency))
                    update_arcs(from, i, to, adjacency);
                else
                    kept_nodes.add(i);
            } else if (in.size() == 2 && out.size() == 2 && (in.get(0).intValue() == out.get(0).intValue() && in.get(1).intValue() == out.get(1).intValue())) {
                // undirected case
                if (!edge_exists(in.get(0), out.get(1), i, adjacency) && !edge_exists(in.get(1), out.get(0), i, adjacency)) {
                    update_arcs(in.get(0), out.get(1), i, adjacency);
                    update_arcs(in.get(1), out.get(0), i, adjacency);
                } else
                    kept_nodes.add(i);
            } else {
                // some other edge configuration
                kept_nodes.add(i);
            }
        }
        int new_n = kept_nodes.size();
        decompressed_name = new int[new_n]; 
        int[] compressed_name = new int[n];
        for (int i = 0; i < n; i++) compressed_name[i] = -1;

        SimpleMatrix new_adj = new SimpleMatrix(new_n, new_n);
        for (int i = 0; i < new_n; i++) {
            int old_i = kept_nodes.get(i);
            decompressed_name[i] = old_i;
            compressed_name[old_i] = i;
        }
        for (int i = 0; i < new_n; i++) {
            for (int j = 0; j < new_n; j++) {
                new_adj.set(i,j,adjacency.get(kept_nodes.get(i), kept_nodes.get(j)));
            }
        }
        for (Integer start : starts_with.keySet()) {
            Arc new_edge = starts_with.get(start);
            new_adj.set(compressed_name[start], compressed_name[new_edge.get_end()], new_edge.get_weight());
        }
        this.adjacency = new_adj;
        this.old_length = length;
        this.length = new_n;
    }

    public SimpleMatrix[] decompress(SimpleMatrix dist, SimpleMatrix pred) {
        SimpleMatrix new_dist = SimpleMatrix.filled(old_length, old_length, Double.POSITIVE_INFINITY);
        // new_dist.insertIntoThis(0, 0, dist);
        SimpleMatrix new_pred = SimpleMatrix.filled(old_length, old_length, Double.POSITIVE_INFINITY);
        // new_pred.insertIntoThis(0, 0, pred);
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                new_dist.set(decompressed_name[i], decompressed_name[j], dist.get(i,j));
                new_pred.set(decompressed_name[i], decompressed_name[j], pred.get(i,j));
            }
        }

        for (Arc edge : starts_with.values()) {
            double weight_to_end = 0;
            double weight_from_start = 0;
            int end_id = edge.get_end();
            int start_id = edge.get_start();

            // Update the shortest paths from all uncompressed nodes to all compressed nodes
            for (int i = 1; i < edge.nodes.size()-1; i++) {
                int id = edge.nodes.get(i);
                weight_from_start += edge.weights.get(i-1);
                for (int k = 0; k < length; k++) {
                    double new_weight =  new_dist.get(decompressed_name[k], start_id) + weight_from_start;
                    if (Double.isFinite(new_weight) && new_weight < new_dist.get(decompressed_name[k], id)) {
                        new_dist.set(decompressed_name[k], id, new_weight);
                        new_pred.set(decompressed_name[k], id, start_id);
                    }
                }
            }

             // Update all shortest paths from all compressed nodes to all uncompressed nodes
            for (int i = edge.nodes.size()-2; i > 0; i--) {
                int id = edge.nodes.get(i);
                weight_to_end += edge.weights.get(i);
                for (int k = 0; k < length; k++) {
                    double new_weight = weight_to_end + new_dist.get(end_id, decompressed_name[k]);
                    if (Double.isFinite(new_weight) && new_weight < new_dist.get(id, decompressed_name[k])) {
                        new_dist.set(id, decompressed_name[k], new_weight);
                        new_pred.set(id, decompressed_name[k], end_id);
                    }
                }
            }

            // Connect within an arc
            for (int i = 1; i < edge.nodes.size()-1; i++) {
                double between_weight = edge.weights.get(i);
                for (int j = i+1; j < edge.nodes.size()-1; j++) {
                    new_dist.set(edge.nodes.get(i), edge.nodes.get(j), between_weight);
                    new_pred.set(edge.nodes.get(i), edge.nodes.get(j), edge.nodes.get(j-1));
                }
            }
            
            // Connect all arcs
            for (Arc other_edge : starts_with.values()) {
                if (edge.get_start() == other_edge.get_start() && edge.get_end() == other_edge.get_end()) {
                    // Same arc
                    continue;
                }
                double dist_between_arcs = new_dist.get(edge.get_end(), other_edge.get_start());
                double arc_weight = 0;
                for (int i = edge.nodes.size()-2; i > 0; i--) {
                    arc_weight += edge.weights.get(i);
                    double other_arc_weight = 0;
                    for (int j = 1; j < other_edge.nodes.size()-1; j++) {
                        other_arc_weight += other_edge.weights.get(j-1);
                        double total_path_weight = dist_between_arcs + arc_weight + other_arc_weight;
                        int node_A = edge.nodes.get(i);
                        int node_B = other_edge.nodes.get(j);
                        if (Double.isFinite(total_path_weight) && total_path_weight < new_dist.get(node_A, node_B)) {
                            new_dist.set(node_A, node_B, total_path_weight);
                            new_pred.set(node_A, node_B, other_edge.get_start());
                        }
                    }
                }
            }
        }
        return new SimpleMatrix[]{new_dist, new_pred};
    }
}
