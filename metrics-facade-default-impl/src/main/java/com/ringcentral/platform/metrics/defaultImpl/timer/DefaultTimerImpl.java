package com.ringcentral.platform.metrics.defaultImpl.timer;

import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.rate.RateImpl;

public class DefaultTimerImpl implements TimerImpl {

    private final RateImpl rate;
    private final HistogramImpl histogram;

    public DefaultTimerImpl(RateImpl rate, HistogramImpl histogram) {
        this.rate = rate;
        this.histogram = histogram;
    }

    @Override
    public void update(long value) {
        rate.mark();
        histogram.update(value);
    }

    @Override
    public RateImpl rate() {
        return rate;
    }

    @Override
    public HistogramImpl histogram() {
        return histogram;
    }
}
