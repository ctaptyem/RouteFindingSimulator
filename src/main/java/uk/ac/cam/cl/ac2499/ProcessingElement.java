package uk.ac.cam.cl.ac2499;

import org.ejml.simple.SimpleMatrix;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class ProcessingElement implements Runnable{
    int id;
    Memory privateMemory;
    Memory sharedMemory;
    CodeBlock code;
    CommunicationManager communications;
    public ProcessingElement(int id, Memory sharedMemory, CommunicationManager communications) {
        this.id = id;
        this.privateMemory = new Memory();
        this.sharedMemory = sharedMemory;
        this.communications = communications;
    }
    
    public void run() {
        while (true) {
            this.code = this.communications.receive_instruction(this.id);
            if (this.code.shutdown) {
                break;
            }
            this.code.id = id;
            this.code.privateMemory = privateMemory;
            this.code.sharedMemory = sharedMemory;
            this.code.communications = communications;
            this.code.run();
        }
    }
}
