package uk.ac.cam.cl.ac2499;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class ProcessingElement implements Runnable{
    int id;
    Memory privateMemory;
    Memory sharedMemory;
    CodeBlock code;
    public ProcessingElement(int id, Memory sharedMemory) {
        this.id = id;
        this.privateMemory = new Memory();
        this.sharedMemory = sharedMemory;
    }
    
    public void run() {
        this.code.id = id;
        this.code.privateMemory = privateMemory;
        this.code.sharedMemory = sharedMemory;
        code.run();
    }
}
