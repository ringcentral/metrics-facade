package com.ringcentral.platform.metrics.micrometer;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.distribution.*;
import io.micrometer.core.instrument.util.MeterEquivalence;

import java.util.concurrent.atomic.*;

import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;

public class MfDistributionSummary extends AbstractDistributionSummary implements MfMeter {

    private final MfMeterBase base;
    private final Histogram mfHistogram;
    private final LongAdder count = new LongAdder();
    private final TimeWindowMax max;
    private final DoubleAdder total = new DoubleAdder();

    public MfDistributionSummary(
        MetricRegistry mfRegistry,
        Id id,
        Clock clock,
        DistributionStatisticConfig distributionStatisticConfig,
        double scale) {

        super(
            id,
            clock,
            distributionStatisticConfig,
            scale,
            false);

        this.base = new MfMeterBase(mfRegistry, id);

        this.mfHistogram =
            this.base.hasDimensions() ?
            mfRegistry.histogram(this.base.name(), () -> withHistogram().dimensions(this.base.dimensions())) :
            mfRegistry.histogram(this.base.name());

        this.max = new TimeWindowMax(clock, distributionStatisticConfig);
    }

    @Override
    protected void recordNonNegative(double amount) {
        if (amount >= 0) {
            long amountLong = (long)amount;
            mfHistogram.update(amountLong, base.dimensionValues());
            count.add(amountLong);
            max.record(amount);
            total.add(amount);
        }
    }

    @Override
    public long count() {
        return count.sum();
    }

    @Override
    public double max() {
        return max.poll();
    }

    @Override
    public double totalAmount() {
        return total.doubleValue();
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
            mfHistogram.removeInstancesFor(base.dimensionValues());
        } else {
            base.mfRegistry().remove(base.name());
        }
    }
}
