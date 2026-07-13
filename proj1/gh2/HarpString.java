package gh2;


import deque.ArrayDeque;
import deque.Deque;


public class HarpString {
    private static final int SR = 44100;      // Sampling Rate
    private static final double DECAY = .996; // energy decay factor

    /* Buffer for storing sound data. */
    private Deque<Double> buffer;

    /* Create a guitar string of the given frequency.  */
    public HarpString(double frequency) {
        int capacity =  (int) (0.5 * Math.round(SR / frequency));

        buffer = new ArrayDeque<Double>();

        for (int i = 0; i < capacity; i++) {
            buffer.addLast(0.0);
        }
    }


    public void pluck() {
        int currentSize = buffer.size();
        for (int i = 0; i < currentSize; i++) {
            buffer.removeFirst();
            double r = Math.random() - 0.5;
            buffer.addLast(r);
        }
    }

    /* Advance the simulation one time step by performing one iteration of
     * the Karplus-Strong algorithm.
     */
    public void tic() {
        double second = buffer.get(1);
        double t = buffer.removeFirst();
        double last = - DECAY * (t + second) / 2;
        buffer.addLast(last);
    }

    /* Return the double at the front of the buffer. */
    public double sample() {
        double first = buffer.get(0);
        return first;
    }
}

