package com.ringcentral.platform.metrics.micrometer;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.rate.Rate;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.util.MeterEquivalence;

import java.util.concurrent.atomic.LongAdder;

import static com.ringcentral.platform.metrics.rate.configs.builders.RateConfigBuilder.withRate;

public class MfCounter extends AbstractMeter implements MfMeter, Counter {

    private final MfMeterBase base;
    private final Rate mfRate;
    private final LongAdder count = new LongAdder();

    public MfCounter(MetricRegistry mfRegistry, Id id) {
        super(id);
        this.base = new MfMeterBase(mfRegistry, id);

        this.mfRate =
            this.base.hasDimensions() ?
            mfRegistry.rate(this.base.name(), () -> withRate().dimensions(this.base.dimensions())) :
            mfRegistry.rate(this.base.name());
    }

    @Override
    public void increment(double amount) {
        long amountLong = (long)amount;

        if (amountLong > 0L) {
            mfRate.mark(amountLong, base.dimensionValues());
            count.add(amountLong);
        }
    }

    @Override
    public double count() {
        return count.sum();
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object other) {
        return MeterEquivalence.equals(this, other);
    }

    @Override
    public int hashCode() {
        return MeterEquivalence.hashCode(this);
    }

    @Override
    public void meterRemoved() {
        if (base.hasDimensions()) {
            mfRate.removeInstancesFor(base.dimensionValues());
        } else {
            base.mfRegistry().remove(base.name());
        }
    }
}
