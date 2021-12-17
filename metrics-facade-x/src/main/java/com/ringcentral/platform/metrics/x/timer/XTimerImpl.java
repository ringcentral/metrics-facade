package com.ringcentral.platform.metrics.x.timer;

import com.ringcentral.platform.metrics.x.histogram.XHistogramImpl;
import com.ringcentral.platform.metrics.x.rate.XRateImpl;

public interface XTimerImpl {
    void update(long value);
    XRateImpl rate();
    XHistogramImpl histogram();
}
