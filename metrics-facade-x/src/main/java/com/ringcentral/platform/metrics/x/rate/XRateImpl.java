package com.ringcentral.platform.metrics.x.rate;

public interface XRateImpl {
    default void mark() {
        mark(1L);
    }

    void mark(long count);
    long count();
    double meanRate();
    double oneMinuteRate();
    double fiveMinutesRate();
    double fifteenMinutesRate();
}
