package uk.ac.cam.cl.ac2499.algorithms.utils;

import uk.ac.cam.cl.ac2499.simulator.Memory;

public class Heap {
    public static void heap_insert(int new_value, String pq, int pq_size, Memory pm, Memory sm) {
        pm.get(pq).set(pq_size, new_value);
        // Move the newest node to its correct position
        int current = pq_size;
        while (current > 0) {
            int parent = (current - 1) / 2;
            
            // If parent has a shorter distance than current, heap property is satisfied
            if (pm.get("dist").get((int) pm.get(pq).get(parent)) <= pm.get("dist").get((int) pm.get(pq).get(current))) {
                break;
            }
            
            // Swap with parent node if the heap property does not hold
            double temp = pm.get(pq).get(current);
            pm.get(pq).set(current, pm.get(pq).get(parent));
            pm.get(pq).set(parent, temp);
            
            current = parent;
        }
    }

    public static boolean heap_update_or_insert(int value, String pq, int pq_size, Memory pm, Memory sm) {
        int current = -1;
        for (int i = 0; i < pq_size; i++) 
            if ((int) pm.get(pq).get(i) == value) {
                current = i;
                break;
            }
        if (current == -1) {
            heap_insert(value, pq, pq_size, pm, sm);
            return true;
        }
        while (current > 0) {
            int parent = (current - 1) / 2;
            
            // If parent has a shorter distance than current, heap property is satisfied
            if (pm.get("dist").get((int) pm.get(pq).get(parent)) <= pm.get("dist").get((int) pm.get(pq).get(current))) {
                break;
            }
            
            // Swap with parent node if the heap property does not hold
            double temp = pm.get(pq).get(current);
            pm.get(pq).set(current, pm.get(pq).get(parent));
            pm.get(pq).set(parent, temp);
            
            current = parent;
        }
        return false;
    }

    public static int heap_pop(String pq, int pq_size, Memory pm, Memory sm) {
        int min = (int) pm.get(pq).get(0);
        pm.get(pq).set(0, pm.get(pq).get(pq_size-1));
        int current = 0;
        while (true) {
            int left = 2 * current + 1;
            int right = 2 * current + 2;
            int best = current;
            
            // Identify which child node has smaller distance, if any
            if (left < pq_size && pm.get("dist").get((int) pm.get(pq).get(left)) < pm.get("dist").get((int) pm.get(pq).get(best))) {
                best = left;
            }
            if (right < pq_size && pm.get("dist").get((int) pm.get(pq).get(right)) < pm.get("dist").get((int) pm.get(pq).get(best))) {
                best = right;
            }
            
            // Break if all nodes below have larger distance
            if (best == current) {
                break;
            }

            // Swap with the smallest child
            double temp = pm.get(pq).get(current);
            pm.get(pq).set(current, pm.get(pq).get(best));
            pm.get(pq).set(best, temp);
            
            current = best;
        }
        return min;
    }
}
