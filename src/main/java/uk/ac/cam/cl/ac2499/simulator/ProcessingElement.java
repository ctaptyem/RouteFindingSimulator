package uk.ac.cam.cl.ac2499.simulator;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import uk.ac.cam.cl.ac2499.algorithms.CodeBlock;

public class ProcessingElement implements Runnable{
    int id;
    Memory privateMemory;
    Memory sharedMemory;
    // Memory metricMemory;
    CodeBlock code;
    MetricTracker metric_tracker;
    long time;

    public ProcessingElement(int id, Memory sharedMemory, CommunicationManager cm) {
        this.id = id;
        this.privateMemory = new Memory();
        this.sharedMemory = sharedMemory;
        // this.metricMemory = metricMemory;
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
            // this.code.mm = metricMemory;
            this.code.communications = metric_tracker;
            metric_tracker.resume_runtime();
            this.code.run();
            metric_tracker.pause_runtime();
            metric_tracker.log_metrics();
        }
    }
}
