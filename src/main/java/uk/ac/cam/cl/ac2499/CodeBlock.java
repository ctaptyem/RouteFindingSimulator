package uk.ac.cam.cl.ac2499;

abstract public class CodeBlock implements Runnable {
    public int id;
    public int processorCount;
    public Memory privateMemory;
    public Memory sharedMemory;
    
    abstract public void run();
}
