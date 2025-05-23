package uk.ac.cam.cl.student2435G.simulator;

import org.ejml.simple.SimpleMatrix;

import uk.ac.cam.cl.student2435G.algorithms.CodeBlock;
import uk.ac.cam.cl.student2435G.algorithms.utils.SubMatrix;

public interface CommunicationInterface {
    public void send_data(int source, int destination, String data);
    public void send_matrix(int source, int destination, String data, SimpleMatrix matrix, Memory sm);
    public void send_submatrix(int source, int destination, String data, SubMatrix matrix, Memory sm);
    public void send_instruction(int destination, CodeBlock instruction);
    public String receive_data(int source, int destination);
    public CodeBlock receive_instruction(int destination);
}
