package uk.ac.cam.cl.ac2499;

import java.util.concurrent.ConcurrentHashMap;

public class Memory {
    ConcurrentHashMap<String, Object> store;
    
    public Memory() {
        store = new ConcurrentHashMap<>();
    }
    
    public Object get(String key) {
        // metric tracking
        return store.get(key);
    }
    
    public void set(String key, Object o) {
        // metric tracking
        store.put(key, o);
    }
}
