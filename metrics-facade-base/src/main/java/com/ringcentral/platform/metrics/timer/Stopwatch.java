package com.ringcentral.platform.metrics.timer;

public interface Stopwatch {
    /**
     * @return elapsed time in nanos.
     */
    long stop();
}
