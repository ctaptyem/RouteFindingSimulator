package uk.ac.cam.cl.ac2499;

import java.util.concurrent.ConcurrentHashMap;

import org.ejml.simple.SimpleMatrix;

public class Memory {
    ConcurrentHashMap<String, SimpleMatrix> matrix_store;
    long total_read;
    long total_write;
    ConcurrentHashMap<String, Long> long_store;

    
    public Memory() {
        matrix_store = new ConcurrentHashMap<>();
        long_store = new ConcurrentHashMap<>();
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
        return ret;
    }

    public long get_long(String key) {
        return long_store.get(key);
    }
    
    public void set(String key, SimpleMatrix o) {
        // metric tracking
        total_write += o.getNumElements();
        matrix_store.put(key, o);
    }

    public void set(String key, long o) {
        long_store.put(key,o);
    }
    
    public void add_metrics(long read, long write) {
        total_read+=read;
        total_write+=write;
    }

}
