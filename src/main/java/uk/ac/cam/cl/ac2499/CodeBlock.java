package uk.ac.cam.cl.ac2499;

abstract public class CodeBlock implements Runnable {
    public int id;
    public int peGridSize;
    public Memory privateMemory;
    public Memory sharedMemory;
    public CommunicationManager communications;
    public boolean shutdown = false;
    
    abstract public void run();

    public void print(String s) {
        System.out.printf("%d: %s%n", id, s);
    }
}
