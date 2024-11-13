package uk.ac.cam.cl.ac2499;


import org.ejml.simple.SimpleMatrix;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.*;


public class Main {
    
    public static void main(String[] args) throws IOException {
//        Graph g = new Graph("preprocessed_OL.cedge.txt");
//        SimpleMatrix sub = g.adjacency.extractMatrix(0,10,0,10);
//        System.out.println(sub.toString());
        
        BlockingQueue<Integer> bq = new LinkedBlockingQueue<Integer>();
        Thread t1 = new Test1(bq);
        Thread t2 = new Test2(bq, 2);
        Thread t3 = new Test2(bq, 3);

        t1.start();
        t2.start();
        t3.start();
    }
}