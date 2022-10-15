package com.ringcentral.platform.metrics.defaultImpl.rate.custom;

import com.ringcentral.platform.metrics.defaultImpl.rate.RateImpl;

public class DefaultTestCustomRateImpl implements RateImpl {

    private final long measurableValue;

    public DefaultTestCustomRateImpl(long measurableValue) {
        this.measurableValue = measurableValue;
    }

    @Override
    public void mark(long count) {}

    @Override
    public long count() {
        return measurableValue;
    }

    @Override
    public double meanRate() {
        return measurableValue;
    }

    @Override
    public double oneMinuteRate() {
        return measurableValue;
    }

    @Override
    public double fiveMinutesRate() {
        return measurableValue;
    }

    @Override
    public double fifteenMinutesRate() {
        return measurableValue;
    }
}
