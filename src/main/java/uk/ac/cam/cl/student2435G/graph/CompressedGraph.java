package uk.ac.cam.cl.student2435G.graph;

import java.util.ArrayList;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

public class CompressedGraph extends Graph{

    static class EdgeChain {
        List<Integer> nodes;
        List<Double> weights;
        double total_weight;
        boolean undirected;

        public EdgeChain(int node_A, int node_B, int node_C, double weight_AB, double weight_BC, boolean undirected) {
            this.nodes = new ArrayList<>();
            nodes.add(node_A);
            nodes.add(node_B);
            nodes.add(node_C);
            this.weights = new ArrayList<>();
            weights.add(weight_AB);
            weights.add(weight_BC);
            total_weight = weight_AB + weight_BC;
            this.undirected = undirected;
        }
        public EdgeChain(List<Integer> nodes, List<Double> weights, double total_weight, boolean undirected) {
            this.nodes = nodes;
            this.weights = weights;
            this.total_weight = total_weight;
            this.undirected = undirected;
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
        public void join(EdgeChain other) {
            nodes.addAll(other.nodes.subList(1, other.nodes.size()));
            weights.addAll(other.weights);
            total_weight += other.total_weight;
        }
        public void reverse() {
            this.nodes = nodes.reversed();
            this.weights = weights.reversed();
        }
        public static EdgeChain reversed(EdgeChain a) {
            return new EdgeChain(a.nodes.reversed(), a.weights.reversed(), a.total_weight, a.undirected); 
        }
        public static EdgeChain join(EdgeChain from_chain, EdgeChain to_chain, int from, int i, int to, double weight_from_i, double weight_i_to, boolean undirected) {
            if (from_chain == null && to_chain == null) {
                return new EdgeChain(from, i, to, weight_from_i, weight_i_to, undirected);
            } else if (from_chain != null && to_chain == null) {
                if (from_chain.get_start() == i) from_chain.reverse();
                from_chain.append(to, weight_i_to);
                return from_chain;
            } else if (from_chain == null && to_chain != null) {
                if (to_chain.get_end() == i) to_chain.reverse();
                to_chain.prepend(from, weight_from_i);
                return to_chain;
            } else {
                if (from_chain.undirected && to_chain.undirected) {
                    if (from_chain.get_end() == to_chain.get_end()) {
                        // reverse to
                        to_chain.reverse();
                    } else if (from_chain.get_start() == to_chain.get_start()) {
                        // reverse from
                        from_chain.reverse();
                    } else if (from_chain.get_start() == to_chain.get_end()) {
                        from_chain.reverse();
                        to_chain.reverse();
                    }
                }
                from_chain.join(to_chain);
                return from_chain;
            }
        }
    }

    EdgeChain[][] chains;
    int[] decompressed_name;
    int old_length;

    public CompressedGraph(Graph g) {
        super(g.adjacency, g.length, g.undirected, g.descriptor_string);
        is_compressed = true;
        chains = new EdgeChain[length][length];
        compress();
        int edge_count = 0;
        for (int i = 0 ; i < length; i++)
            for (int j = 0; j < length; j++)
                if (Double.isFinite(adjacency.get(i,j)) && i != j)
                    edge_count++;
        if (undirected) edge_count /= 2;
        this.descriptor_string += String.format(",%d,%f,%f", length, (float)edge_count/length, (float)edge_count/(length * length - length));
    }

    public void compress() {
        ArrayList<Integer> kept_nodes = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            ArrayList<Integer> in = new ArrayList<>();
            ArrayList<Integer> out = new ArrayList<>();
            for (int j = 0; j < length; j++) {
                if (Double.isFinite(adjacency.get(i,j)) && i!=j)
                    out.add(j);
                if (Double.isFinite(adjacency.get(j,i)) && i!=j)
                    in.add(j);
            }
            if (in.size() == 0 && out.size() == 0) {
                // No neighbors case
                // The node will be removed in the compressed form, and its distance and predecessor values will be reconstructed by default
                continue;
            } else if (in.size() == 1 && out.size() == 1 && in.get(0).intValue() != out.get(0).intValue()) {
                // directed case
                int from = in.get(0);
                int to = out.get(0);
                if (!Double.isFinite(adjacency.get(from, to))) {
                    // skip edge does not already exist
                    EdgeChain from_chain = chains[from][i];
                    EdgeChain to_chain = chains[i][to];
                    EdgeChain new_chain = EdgeChain.join(from_chain, to_chain, from, i, to, adjacency.get(from, i), adjacency.get(i, to), false);
                    
                    chains[from][i] = null;
                    chains[i][to] = null;
                    chains[from][to] = new_chain;

                    adjacency.set(from, to, adjacency.get(from,i) + adjacency.get(i,to));
                    adjacency.set(from, i, Double.POSITIVE_INFINITY);
                    adjacency.set(i, to, Double.POSITIVE_INFINITY);
                } else {
                    kept_nodes.add(i);
                }
            } else if (in.size() == 2 && out.size() == 2 && (in.get(0).intValue() == out.get(0).intValue() && in.get(1).intValue() == out.get(1).intValue())) {
                // undirected case
                int from = in.get(0);
                int to = out.get(1);
                if (!Double.isFinite(adjacency.get(from, to)) && !Double.isFinite(adjacency.get(to, from))) {
                    // skip edge does not already exist
                    EdgeChain from_arc = chains[from][i];
                    EdgeChain to_arc = chains[i][to];
                    if (from_arc != null && to_arc != null && from_arc.undirected != to_arc.undirected) throw new RuntimeException("Directedness does not match");
                    EdgeChain new_arc = EdgeChain.join(from_arc, to_arc, from, i, to, adjacency.get(from, i), adjacency.get(i, to), true);
                    
                    chains[from][i] = null;
                    chains[i][from] = null;
                    chains[to][i] = null;
                    chains[i][to] = null;
                    chains[from][to] = new_arc;
                    chains[to][from] = new_arc;

                    adjacency.set(from, to, adjacency.get(from,i) + adjacency.get(i,to));
                    adjacency.set(to, from, adjacency.get(to,i) + adjacency.get(i,from));
                    adjacency.set(from, i, Double.POSITIVE_INFINITY);
                    adjacency.set(i, from, Double.POSITIVE_INFINITY);
                    adjacency.set(i, to, Double.POSITIVE_INFINITY);
                    adjacency.set(to, i, Double.POSITIVE_INFINITY);
                } else {
                    kept_nodes.add(i);
                }
            } else {
                kept_nodes.add(i);
            }
        }
        int new_n = kept_nodes.size();
        decompressed_name = new int[new_n]; 
        int[] compressed_name = new int[length];
        for (int i = 0; i < length; i++) compressed_name[i] = -1;

        for (int i = 0; i < new_n; i++) {
            int old_i = kept_nodes.get(i);
            decompressed_name[i] = old_i;
            compressed_name[old_i] = i;
        }
        SimpleMatrix new_adj = new SimpleMatrix(new_n, new_n);
        for (int i = 0; i < new_n; i++) {
            for (int j = 0; j < new_n; j++) {
                new_adj.set(i,j,adjacency.get(decompressed_name[i], decompressed_name[j]));
            }
        }

        this.adjacency = new_adj;
        this.old_length = length;
        this.length = new_n;
    }

    public SimpleMatrix[] decompress(SimpleMatrix dist, SimpleMatrix pred) {
        SimpleMatrix new_dist = SimpleMatrix.filled(old_length, old_length, Double.POSITIVE_INFINITY);
        SimpleMatrix new_pred = SimpleMatrix.filled(old_length, old_length, -1);
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                new_dist.set(decompressed_name[i], decompressed_name[j], dist.get(i,j));
                new_pred.set(decompressed_name[i], decompressed_name[j], pred.get(i,j));
            }
        }

        ArrayList<EdgeChain> chains_list = new ArrayList<>();
        for (int i = 0; i < old_length; i++) {
            for (int j = 0; j < old_length; j++) {
                if (chains[i][j] != null) {
                    chains_list.add(chains[i][j]);
                    if (chains[i][j].undirected) {
                        chains[j][i] = EdgeChain.reversed(chains[i][j]);
                    } 
                }
            }
        }

        for (EdgeChain edge : chains_list) {
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

            // Connect within a chain
            for (int i = 0; i < edge.nodes.size()-1; i++) {
                int i_id = edge.nodes.get(i);
                double between_weight = 0;
                for (int j = i+1; j < edge.nodes.size(); j++) {
                    int j_id = edge.nodes.get(j);
                    between_weight += edge.weights.get(j-1);
                    if (between_weight < new_dist.get(i_id, j_id)) {
                        new_dist.set(i_id, j_id, between_weight);
                        new_pred.set(i_id, j_id, edge.nodes.get(j-1));
                    }
                }
            }

            // Update all shortest paths from all compressed nodes to all nodes
            for (int i = edge.nodes.size()-2; i > 0; i--) {
                int id = edge.nodes.get(i);
                weight_to_end += edge.weights.get(i);
                for (int k = 0; k < old_length; k++) { 
                    double new_weight = weight_to_end + new_dist.get(end_id, k);
                    if (Double.isFinite(new_weight) && new_weight < new_dist.get(id, k)) {
                        new_dist.set(id, k, new_weight);
                        new_pred.set(id, k, end_id);
                    }
                }
            }

            
            
            // Connect all chains
            for (EdgeChain other_edge : chains_list) {
                if (edge.get_start() == other_edge.get_start() && edge.get_end() == other_edge.get_end()) {
                    // Same chain
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
        for (int i = 0; i < old_length; i++) {
            new_dist.set(i,i,0);
            new_pred.set(i,i,i);
        }
        return new SimpleMatrix[]{new_dist, new_pred};
    }
}
