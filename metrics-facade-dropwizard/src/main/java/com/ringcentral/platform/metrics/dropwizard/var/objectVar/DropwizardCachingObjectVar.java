package com.ringcentral.platform.metrics.dropwizard.var.objectVar;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.dropwizard.var.DropwizardCachingValueSupplier;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import com.ringcentral.platform.metrics.var.objectVar.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class DropwizardCachingObjectVar extends AbstractObjectVar implements CachingObjectVar {

    public DropwizardCachingObjectVar(
        MetricName name,
        CachingVarConfig config,
        Supplier<Object> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            OBJECT_VALUE,
            valueSupplier != null ? new DropwizardCachingValueSupplier<>(config, valueSupplier) : null,
            DefaultObjectVarInstanceMaker.INSTANCE,
            executor);
    }

    @Override
    public void register(Supplier<Object> valueSupplier, MetricDimensionValues dimensionValues) {
        super.register(
            new DropwizardCachingValueSupplier<>((CachingVarConfig)config(), valueSupplier),
            dimensionValues);
    }
}
