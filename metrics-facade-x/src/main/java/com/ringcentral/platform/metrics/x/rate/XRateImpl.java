package com.ringcentral.platform.metrics.x.rate;

public interface XRateImpl {
    void mark(long count);
    long count();
    double meanRate();
    double oneMinuteRate();
    double fiveMinutesRate();
    double fifteenMinutesRate();
}
