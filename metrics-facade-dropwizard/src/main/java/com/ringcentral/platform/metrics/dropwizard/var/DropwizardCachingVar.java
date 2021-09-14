package com.ringcentral.platform.metrics.dropwizard.var;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import com.codahale.metrics.CachedGauge;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.AbstractVar;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;

public abstract class DropwizardCachingVar<V> extends AbstractVar<V> {

    private final CachingVarConfig config;

    protected DropwizardCachingVar(
        MetricName name,
        CachingVarConfig config,
        Measurable valueMeasurable,
        Supplier<V> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            valueMeasurable,
            valueSupplier != null ? new CachingValueSupplier<>(config, valueSupplier) : null,
            executor);

        this.config = config;
    }

    @Override
    public void register(Supplier<V> valueSupplier, MetricDimensionValues dimensionValues) {
        super.register(new CachingValueSupplier<>(config, valueSupplier), dimensionValues);
    }

    private static class CachingValueSupplier<V> extends CachedGauge<V> implements Supplier<V> {

        final Supplier<V> valueSupplier;

        CachingValueSupplier(CachingVarConfig config, Supplier<V> valueSupplier) {
            super(config.ttl(), config.ttlUnit());
            this.valueSupplier = valueSupplier;
        }

        @Override
        public V get() {
            return getValue();
        }

        @Override
        protected V loadValue() {
            return valueSupplier.get();
        }
    }
}
