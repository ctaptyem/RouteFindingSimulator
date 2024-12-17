package uk.ac.cam.cl.ac2499;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CommunicationManager {
    LinkedBlockingQueue<String>[][] data_link;
    BlockingQueue<CodeBlock>[] instruction_link;
    int num_processors;
    
    public CommunicationManager(int processorCount) {
        this.num_processors = processorCount;
        this.data_link = new LinkedBlockingQueue[processorCount][processorCount];
        this.instruction_link = new LinkedBlockingQueue[processorCount];
        for (int i = 0; i < processorCount; i++) {
            for (int j = 0; j < processorCount; j++) 
                this.data_link[i][j] = new LinkedBlockingQueue<String>();
            this.instruction_link[i] = new LinkedBlockingQueue<CodeBlock>();
        }
    }
    
    public void send_data(int source, int destination, String data) {
        this.data_link[source][destination].offer(data);
    }
    public void send_instruction(int destination, CodeBlock instruction) {
        this.instruction_link[destination].offer(instruction);
    }
    public String receive_data(int source, int destination) {
        try {
            return this.data_link[source][destination].take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public CodeBlock receive_instruction(int destination) {
        try {
            return this.instruction_link[destination].take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
