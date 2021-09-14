package com.ringcentral.platform.metrics.micrometer;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.var.doubleVar.DoubleVar;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.util.MeterEquivalence;
import org.slf4j.Logger;

import java.lang.ref.WeakReference;
import java.util.function.*;

import static com.ringcentral.platform.metrics.var.Var.noTotal;
import static com.ringcentral.platform.metrics.var.doubleVar.configs.builders.DoubleVarConfigBuilder.withDoubleVar;
import static org.slf4j.LoggerFactory.getLogger;

public class MfDoubleGauge<A> extends AbstractMeter implements MfMeter, Gauge {

    private final MfMeterBase base;
    private final Supplier<Double> valueSupplier;
    private final DoubleVar mfDoubleVar;

    private static final Logger logger = getLogger(MfDoubleGauge.class);

    public MfDoubleGauge(
        MetricRegistry mfRegistry,
        Id id,
        ToDoubleFunction<A> fun,
        A funArg,
        boolean nonDecreasing) {

        super(id);

        this.base = new MfMeterBase(mfRegistry, id);
        WeakReference<A> funArgWeakRef = new WeakReference<>(funArg);

        this.valueSupplier = () -> {
            A funArgRef = funArgWeakRef.get();

            if (funArgRef != null) {
                try {
                    return fun.applyAsDouble(funArgRef);
                } catch (Exception e) {
                    logger.error(
                        "Failed to apply the function for the gauge '{}'",
                        id.getName(), e);
                }
            }

            return Double.NaN;
        };

        this.mfDoubleVar = mfRegistry.doubleVar(
            this.base.name(),
            this.base.hasDimensions() ? noTotal() : this.valueSupplier,
            () ->
                this.base.hasDimensions() ?
                withDoubleVar().dimensions(this.base.dimensions()).nonDecreasing(nonDecreasing) :
                withDoubleVar().nonDecreasing(nonDecreasing));

        if (this.base.hasDimensions()) {
            this.mfDoubleVar.register(this.valueSupplier, this.base.dimensionValues());
        }
    }

    @Override
    public double value() {
        Double value = valueSupplier.get();
        return value == null ? Double.NaN : value;
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
            mfDoubleVar.deregister(base.dimensionValues());
        } else {
            base.mfRegistry().remove(base.name());
        }
    }
}
