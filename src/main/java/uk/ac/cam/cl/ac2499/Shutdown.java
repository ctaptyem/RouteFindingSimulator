package uk.ac.cam.cl.ac2499;

public class Shutdown extends CodeBlock {
    public Shutdown() {
        super();
        shutdown = true;
    }
    public void run() {
        // This should never be called
        System.out.println("Shutdown executed");
        throw new RuntimeException("run() method on Shutdown instruction called");
    }
}
