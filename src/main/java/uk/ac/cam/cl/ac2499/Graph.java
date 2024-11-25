package uk.ac.cam.cl.ac2499;


import org.ejml.simple.SimpleMatrix;

import java.io.*;

import java.io.BufferedReader;
import java.io.FileReader;

public class Graph { 
    public final SimpleMatrix adjacency;
    public int length;
    
    public Graph(String filepath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        try {
            String line = br.readLine();
            length = Integer.parseInt(line)+1;
            this.adjacency = new SimpleMatrix(length, length);
            for (int i = 0; i<length; i++)
                for (int j = 0; j <length; j++)
                    if (i != j)
                        adjacency.set(i,j,Double.POSITIVE_INFINITY);

            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" ");
                // Assuming the format is (Edge ID, Start Node ID, End Node ID, Distance)
                if (tokens.length == 4) {
                    int edgeId = Integer.parseInt(tokens[0].trim());
                    int startNodeId = Integer.parseInt(tokens[1].trim());
                    int endNodeId = Integer.parseInt(tokens[2].trim());
                    float distance = Float.parseFloat(tokens[3].trim());
                    // Add twice to the adjacency matrix for undirected graph
                    adjacency.set(startNodeId, endNodeId, distance);
                    adjacency.set(endNodeId, startNodeId, distance);
                }
            }
        } finally {
            br.close();
        }
        
    }
}
