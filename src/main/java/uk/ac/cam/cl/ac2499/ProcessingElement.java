package uk.ac.cam.cl.ac2499;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ProcessingElement extends Thread{
    int id;
    Memory privateMemory;
    CodeBlock code;
    BlockingQueue<CodeBlock> data_queue;
    public ProcessingElement(int id) {
        this.id = id;
        this.privateMemory = new Memory();
        this.job_queue = new LinkedBlockingQueue<>();
    }
    
    public void run() {
        this.code.id = id;
        this.code.privateMemory = privateMemory;
        code.run();
    }
}
