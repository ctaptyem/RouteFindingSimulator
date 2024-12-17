package uk.ac.cam.cl.ac2499;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

abstract public class CodeBlock implements Runnable {
    public int id;
    public int peGridSize;
    public Memory privateMemory;
    public Memory sharedMemory;
    public CommunicationManager communications;
    public boolean shutdown = false;
    
    abstract public void run();
}
