package uk.ac.cam.cl.student2435G.algorithms;

import uk.ac.cam.cl.student2435G.simulator.CommunicationInterface;
import uk.ac.cam.cl.student2435G.simulator.Memory;

abstract public class CodeBlock implements Runnable {
    public int id;
    public int pe_grid_size;
    public Memory pm; //privateMemory
    public Memory sm; // sharedMemory
    public CommunicationInterface communications;
    public boolean shutdown = false;
    
    abstract public void run();

    public void print(String s) {
        System.out.printf("%d: %s%n", id, s);
    }
}
