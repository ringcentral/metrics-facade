package com.ringcentral.platform.metrics.defaultImpl.rate;

public interface RateImpl {
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
