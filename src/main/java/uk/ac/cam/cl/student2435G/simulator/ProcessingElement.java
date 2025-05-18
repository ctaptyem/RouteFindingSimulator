package uk.ac.cam.cl.student2435G.simulator;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import uk.ac.cam.cl.student2435G.algorithms.CodeBlock;

public class ProcessingElement implements Runnable{
    int id;
    Memory privateMemory;
    Memory sharedMemory;
    CodeBlock code;
    MetricTracker metric_tracker;
    long time;

    public ProcessingElement(int id, Memory sharedMemory, CommunicationManager cm) {
        this.id = id;
        this.privateMemory = new Memory();
        this.sharedMemory = sharedMemory;
        this.metric_tracker = new MetricTracker(cm);
    }
    
    public void run() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        while (true) {
            metric_tracker.resume_runtime();
            this.code = this.metric_tracker.receive_instruction(this.id);
            metric_tracker.pause_runtime();
            if (this.code.shutdown) {
                time = bean.getCurrentThreadUserTime();
                break;
            }
            this.code.id = id;
            this.code.pm = privateMemory;
            this.code.sm = sharedMemory;
            this.code.communications = metric_tracker;
            metric_tracker.resume_runtime();
            this.code.run();
            metric_tracker.pause_runtime();
            metric_tracker.log_metrics();
        }
    }
}
