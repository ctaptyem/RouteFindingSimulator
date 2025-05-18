package uk.ac.cam.cl.ac2499.algorithms.utils;

import org.ejml.simple.SimpleMatrix;

public class SubMatrix extends SimpleMatrix {
    public int row;
    public int col;
    public SimpleMatrix matrix;
    public SubMatrix(SimpleMatrix matrix, int row, int col) {
        this.matrix = matrix;
        this.row = row;
        this.col = col;
    }
}
