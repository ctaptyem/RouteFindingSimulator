package uk.ac.cam.cl.ac2499;

public class Timer {
    long current_total;
    long start;

    public Timer() {
        current_total = 0;
        start = 0;
    }

    public void resume() {
        start = System.currentTimeMillis();
    }

    public void pause() {
        long end = System.currentTimeMillis();
        current_total += end - start;
    }

    public long get_time() {
        return current_total;
    }

    public void add_time(long time) {
        current_total += time;
    }
}
