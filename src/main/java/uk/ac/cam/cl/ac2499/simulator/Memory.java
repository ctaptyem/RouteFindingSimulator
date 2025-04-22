package uk.ac.cam.cl.ac2499.simulator;

import java.util.concurrent.ConcurrentHashMap;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.Graph;

public class Memory {
    ConcurrentHashMap<String, SimpleMatrix> matrix_store;
    long total_read;
    long total_write;
    ConcurrentHashMap<String, Long> long_store;
    ConcurrentHashMap<String, Boolean> written_and_not_read;
    ConcurrentHashMap<String, Graph> graph_store;

    
    public Memory() {
        matrix_store = new ConcurrentHashMap<>();
        long_store = new ConcurrentHashMap<>();
        graph_store = new ConcurrentHashMap<>();
        written_and_not_read = new ConcurrentHashMap<>();
        total_read = 0;
        total_write = 0;
    }

    public boolean contains(String key) {
        return matrix_store.containsKey(key);
    }
    
    public SimpleMatrix get(String key) {
        // metric tracking
        SimpleMatrix ret = matrix_store.get(key);
        total_read += ret.getNumElements();
        written_and_not_read.put(key, false);
        return ret;
    }

    public long get_long(String key) {
        return long_store.get(key);
    }
    
    public void set(String key, SimpleMatrix o) {
        // metric tracking
        total_write += o.getNumElements();
        matrix_store.put(key, o);
        if (written_and_not_read.containsKey(key) && written_and_not_read.get(key)) {
            // System.out.println(String.format("%n%n%s WAS WRITTEN TO TWICE%n%n", key));
            throw new RuntimeException(String.format("%n%n%s WAS WRITTEN TO TWICE%n%n", key));
        }
        written_and_not_read.put(key, true);
        
    }

    public void set(String key, long o) {
        long_store.put(key,o);
    }
    
    public void add_metrics(long read, long write) {
        total_read+=read;
        total_write+=write;
    }

}
