package uk.ac.cam.cl.ac2499;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import uk.ac.cam.cl.ac2499.algorithms.CodeBlock;

public class ControlUnit extends Thread{
    int id;
    int processorCount;
    Memory privateMemory;
    CodeBlock code;
    BlockingQueue<Integer> data_queue;
    
    public ControlUnit(int id, int processorCount) {
        this.id = id;
        this.processorCount = processorCount;
        this.privateMemory = new Memory();
        this.data_queue = new LinkedBlockingQueue<>();
    }

    public void run() {
        this.code.id = id;
        this.code.pm = privateMemory;
        code.run();
    }
}
