package com.ringcentral.platform.metrics.samples.rate;

import com.ringcentral.platform.metrics.defaultImpl.rate.AbstractRateImpl;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.utils.TimeNanosProvider;

import java.util.Set;

public class CountScalingRateImpl extends AbstractRateImpl {

    private final long factor;

    public CountScalingRateImpl(
        Set<? extends Measurable> measurables,
        TimeNanosProvider timeNanosProvider,
        long factor) {

        super(measurables, timeNanosProvider);
        this.factor = factor;
    }

    @Override
    protected void markForRates(long count) {}

    @Override
    public long count() {
        return super.count() * factor;
    }

    @Override
    public double oneMinuteRate() {
        return 0.0;
    }

    @Override
    public double fiveMinutesRate() {
        return 0.0;
    }

    @Override
    public double fifteenMinutesRate() {
        return 0.0;
    }
}
