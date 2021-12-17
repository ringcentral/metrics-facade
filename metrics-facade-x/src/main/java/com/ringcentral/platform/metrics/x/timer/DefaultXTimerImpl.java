package com.ringcentral.platform.metrics.x.timer;

import com.ringcentral.platform.metrics.x.histogram.XHistogramImpl;
import com.ringcentral.platform.metrics.x.rate.XRateImpl;

public class DefaultXTimerImpl implements XTimerImpl {

    private final XRateImpl rate;
    private final XHistogramImpl histogram;

    public DefaultXTimerImpl(XRateImpl rate, XHistogramImpl histogram) {
        this.rate = rate;
        this.histogram = histogram;
    }

    @Override
    public void update(long value) {
        rate.mark();
        histogram.update(value);
    }

    @Override
    public XRateImpl rate() {
        return rate;
    }

    @Override
    public XHistogramImpl histogram() {
        return histogram;
    }
}
