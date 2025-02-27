package uk.ac.cam.cl.ac2499;

import uk.ac.cam.cl.ac2499.algorithms.CodeBlock;

public class ProcessingElement implements Runnable{
    int id;
    Memory privateMemory;
    Memory sharedMemory;
    Memory metricMemory;
    CodeBlock code;
    CommunicationManager communications;
    public ProcessingElement(int id, Memory sharedMemory, Memory metricMemory, CommunicationManager communications) {
        this.id = id;
        this.privateMemory = new Memory();
        this.sharedMemory = sharedMemory;
        this.metricMemory = metricMemory;
        this.communications = communications;
    }
    
    public void run() {
        while (true) {
            this.code = this.communications.receive_instruction(this.id);
            if (this.code.shutdown) {
                break;
            }
            this.code.id = id;
            this.code.pm = privateMemory;
            this.code.sm = sharedMemory;
            this.code.mm = metricMemory;
            this.code.communications = communications;
            this.code.run();
        }
    }
}
