package uk.ac.cam.cl.ac2499.simulator;

import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.ac2499.algorithms.CodeBlock;
import uk.ac.cam.cl.ac2499.algorithms.utils.Timer;

public class MetricTracker implements CommunicationInterface {
    public CommunicationManager cm;
    public Timer runtime;
    public long comm_count;
    public long comm_volume;
    public ArrayList<Long> runtime_log;
    public ArrayList<Long> comm_count_log;
    public ArrayList<Long> comm_volume_log;

    private static void set_maximum(ArrayList<Long> max_log, ArrayList<Long> new_log) {
        for (int k = 0; k < new_log.size(); k++)
            if (max_log.size() <= k) {
                max_log.add(new_log.get(k));
            } else if (max_log.get(k) < new_log.get(k))
                max_log.set(k, new_log.get(k));
    }

    public static long get_log_sum(ArrayList<Long> max_log) {
        long sum = 0;
        for (long value : max_log) {
            sum+=value;
        }
        return sum;
    }

    public static long[] process_metrics(ProcessingElement[][] processors) {
        ArrayList<Long> max_runtime_log = new ArrayList<>();
        ArrayList<Long> max_comm_count_log = new ArrayList<>();
        ArrayList<Long> max_comm_volume_log = new ArrayList<>();
        for (int i = 0; i < processors.length; i++) {
            for (int j = 0; j < processors[0].length; j++) {
                MetricTracker mt = processors[i][j].metric_tracker;
                set_maximum(max_runtime_log, mt.runtime_log);
                set_maximum(max_comm_count_log, mt.comm_count_log);
                set_maximum(max_comm_volume_log, mt.comm_volume_log);
            }
        }
        return new long[]{get_log_sum(max_runtime_log), get_log_sum(max_comm_count_log), get_log_sum(max_comm_volume_log)};
    }

    public MetricTracker(CommunicationManager cm) {
        this.cm = cm;
        runtime = new Timer();
        comm_count = 0;
        comm_volume = 0;
        runtime_log = new ArrayList<>();
        comm_count_log = new ArrayList<>();
        comm_volume_log = new ArrayList<>();

    }
    public void send_data(int source, int destination, String data) {
        runtime.pause();
        comm_count++;
        cm.send_data(source, destination, data);
        runtime.resume();
    }
    public void send_matrix(int source, int destination, String data, SimpleMatrix matrix, Memory sm) {
        runtime.pause();
        comm_count++;
        comm_volume += matrix.getNumElements();
        cm.send_matrix(source, destination, data, matrix, sm);
        runtime.resume();
    }
    public void send_instruction(int destination, CodeBlock instruction) {
        runtime.pause();
        cm.send_instruction(destination, instruction);
        runtime.resume();
    }
    public String receive_data(int source, int destination) {
        runtime.pause();
        String return_value = cm.receive_data(source, destination);
        runtime.resume();
        return return_value;
    }
    public CodeBlock receive_instruction(int destination) {
        runtime.pause();
        CodeBlock return_value = cm.receive_instruction(destination);
        runtime.resume();
        return return_value; 
    }
    public void resume_runtime() {
        runtime.resume();
    }
    public void pause_runtime() {
        runtime.pause();
    }
    public void log_metrics() {
        runtime_log.add(runtime.get_time());
        runtime = new Timer();
        comm_count_log.add(comm_count);
        comm_count = 0;
        comm_volume_log.add(comm_volume);
        comm_volume = 0;
    }

    // public ArrayList<Long> get_runtime_log() {
    //     return runtime_log;
    // }
    // public ArrayList<Long> get_communication_count_log() {
    //     return comm_count_log;
    // }
    // public ArrayList<Long> get_communication_volume_log() {
    //     return comm_volume_log;
    // }
}

