package uk.ac.cam.cl.ac2499;

public class Simulator {
    Graph graph;
    CodeBlock algorithm;
    int processorCount;
    ProcessingElement[][] processors;
    ProcessingElement MCU;
    Memory sharedMemory;
    
    public Simulator(Parameters parameters, Graph input, CodeBlock algorithm) {
        this.graph = input;
        this.processorCount = parameters.processingElementCount;
        this.processors = new ProcessingElement[processorCount][processorCount];
        this.MCU = new ProcessingElement(0, processorCount);
        this.sharedMemory = new Memory();
        this.algorithm = algorithm;
    }
    
    public void start() {
        
    }
}
