package uk.ac.cam.cl.ac2499;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

abstract public class CodeBlock implements Runnable {
    public int id;
    public int processorCount;
    public ProcessingElement[][] PE_array;
    public Memory privateMemory;
    public Memory sharedMemory;
    
    abstract public void run();
}
