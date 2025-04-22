package uk.ac.cam.cl.ac2499.algorithms;

import uk.ac.cam.cl.ac2499.simulator.CommunicationManager;
import uk.ac.cam.cl.ac2499.simulator.Memory;

abstract public class CodeBlock implements Runnable {
    public int id;
    public int peGridSize;
    public Memory pm; //privateMemory
    public Memory sm; // sharedMemory
    public Memory mm; // metricMemory
    public CommunicationManager communications;
    public boolean shutdown = false;
    
    abstract public void run();

    public void print(String s) {
        System.out.printf("%d: %s%n", id, s);
    }
}
