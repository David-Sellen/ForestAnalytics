package edu.miun.firespark.utilities;

import java.util.concurrent.TimeUnit;

/**
 * Created by Dwe on 2016-05-02.
 */
public class Timer {
    long startTime;
    long stopTime;

    public void start(){
        startTime = System.nanoTime();
    }

    public void stop(){
        stopTime = System.nanoTime();
    }

    public long elapsedTimeMS(){
        long elapsedTime = startTime-stopTime;
        return TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
    }

    public long elapsedTimeNano(){
        long elapsed = stopTime - startTime;
        return elapsed;
    }

    @Override
    public String toString() {
        return "NS: "+elapsedTimeNano();
    }
}
