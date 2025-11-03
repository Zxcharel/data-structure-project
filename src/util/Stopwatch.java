package src.util;

/**
 * Simple stopwatch utility for measuring execution time.
 * Provides start/stop functionality and elapsed time reporting.
 */
public class Stopwatch {
    private long startTime;
    private long endTime;
    private boolean running;
    
    /**
     * Starts the stopwatch
     */
    public void start() {
        startTime = System.currentTimeMillis();
        running = true;
    }
    
    /**
     * Stops the stopwatch
     */
    public void stop() {
        if (running) {
            endTime = System.currentTimeMillis();
            running = false;
        }
    }
    
    /**
     * Gets the elapsed time in milliseconds
     */
    public long getElapsedMs() {
        if (running) {
            return System.currentTimeMillis() - startTime;
        } else {
            return endTime - startTime;
        }
    }
    
    /**
     * Gets the elapsed time in seconds
     */
    public double getElapsedSeconds() {
        return getElapsedMs() / 1000.0;
    }
    
    /**
     * Resets the stopwatch
     */
    public void reset() {
        startTime = 0;
        endTime = 0;
        running = false;
    }
    
    /**
     * Checks if the stopwatch is currently running
     */
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public String toString() {
        return String.format("Stopwatch{elapsed=%dms, running=%s}", getElapsedMs(), running);
    }
}
