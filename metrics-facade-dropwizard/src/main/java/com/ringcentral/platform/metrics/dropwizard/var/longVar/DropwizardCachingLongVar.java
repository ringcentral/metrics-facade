package com.ringcentral.platform.metrics.dropwizard.var.longVar;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.dropwizard.var.DropwizardCachingValueSupplier;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import com.ringcentral.platform.metrics.var.longVar.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static com.ringcentral.platform.metrics.var.longVar.AbstractLongVar.DefaultLongVarInstanceMaker.INSTANCE;

public class DropwizardCachingLongVar extends AbstractLongVar implements CachingLongVar {

    public DropwizardCachingLongVar(
        MetricName name,
        CachingVarConfig config,
        Supplier<Long> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            LONG_VALUE,
            valueSupplier != null ? new DropwizardCachingValueSupplier<>(config, valueSupplier) : null,
            INSTANCE,
            executor);
    }

    @Override
    public void register(Supplier<Long> valueSupplier, MetricDimensionValues dimensionValues) {
        super.register(
            new DropwizardCachingValueSupplier<>((CachingVarConfig)config(), valueSupplier),
            dimensionValues);
    }
}
