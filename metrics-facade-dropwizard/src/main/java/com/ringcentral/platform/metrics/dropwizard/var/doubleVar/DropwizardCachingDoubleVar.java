package com.ringcentral.platform.metrics.dropwizard.var.doubleVar;

import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.dropwizard.var.DropwizardCachingValueSupplier;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import com.ringcentral.platform.metrics.var.doubleVar.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class DropwizardCachingDoubleVar extends AbstractDoubleVar implements CachingDoubleVar {

    public DropwizardCachingDoubleVar(
        MetricName name,
        CachingVarConfig config,
        Supplier<Double> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            DOUBLE_VALUE,
            valueSupplier != null ? new DropwizardCachingValueSupplier<>(config, valueSupplier) : null,
            DefaultDoubleVarInstanceMaker.INSTANCE,
            executor);
    }

    @Override
    public void register(Supplier<Double> valueSupplier, LabelValues labelValues) {
        super.register(
            new DropwizardCachingValueSupplier<>((CachingVarConfig)config(), valueSupplier),
            labelValues);
    }
}
