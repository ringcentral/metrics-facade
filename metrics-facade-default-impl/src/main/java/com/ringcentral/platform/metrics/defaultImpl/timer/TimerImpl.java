package com.ringcentral.platform.metrics.defaultImpl.timer;

import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.rate.RateImpl;

public interface TimerImpl {
    void update(long value);
    RateImpl rate();
    HistogramImpl histogram();
}
