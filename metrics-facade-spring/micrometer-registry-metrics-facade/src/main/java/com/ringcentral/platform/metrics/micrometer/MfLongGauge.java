package com.ringcentral.platform.metrics.micrometer;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.var.longVar.LongVar;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.util.MeterEquivalence;
import org.slf4j.Logger;

import java.lang.ref.WeakReference;
import java.util.function.*;

import static com.ringcentral.platform.metrics.var.Var.noTotal;
import static com.ringcentral.platform.metrics.var.longVar.configs.builders.LongVarConfigBuilder.withLongVar;
import static org.slf4j.LoggerFactory.getLogger;

public class MfLongGauge<A> extends AbstractMeter implements MfMeter, Gauge {

    private final MfMeterBase base;
    private final Supplier<Long> valueSupplier;
    private final LongVar mfLongVar;
    private volatile long lastValue;

    private static final Logger logger = getLogger(MfLongGauge.class);

    public MfLongGauge(
        MetricRegistry mfRegistry,
        Id id,
        ToLongFunction<A> fun,
        A funArg,
        boolean nonDecreasing) {

        super(id);

        this.base = new MfMeterBase(mfRegistry, id);
        WeakReference<A> funArgWeakRef = new WeakReference<>(funArg);

        this.valueSupplier = () -> {
            A funArgRef = funArgWeakRef.get();

            if (funArgRef != null) {
                try {
                    return lastValue = fun.applyAsLong(funArgRef);
                } catch (Exception e) {
                    logger.error(
                        "Failed to apply the function for the gauge '{}'",
                        id.getName(), e);
                }
            }

            return lastValue;
        };

        this.mfLongVar = mfRegistry.longVar(
            this.base.name(),
            this.base.hasDimensions() ? noTotal() : this.valueSupplier,
            () ->
                this.base.hasDimensions() ?
                withLongVar().dimensions(this.base.dimensions()).nonDecreasing(nonDecreasing) :
                withLongVar().nonDecreasing(nonDecreasing));

        if (this.base.hasDimensions()) {
            this.mfLongVar.register(this.valueSupplier, this.base.dimensionValues());
        }
    }

    @Override
    public double value() {
        return valueSupplier.get();
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
            mfLongVar.deregister(base.dimensionValues());
        } else {
            base.mfRegistry().remove(base.name());
        }
    }
}
