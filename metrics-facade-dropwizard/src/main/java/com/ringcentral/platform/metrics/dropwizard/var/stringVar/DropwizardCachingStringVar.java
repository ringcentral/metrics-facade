package com.ringcentral.platform.metrics.dropwizard.var.stringVar;

import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.dropwizard.var.DropwizardCachingValueSupplier;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import com.ringcentral.platform.metrics.var.stringVar.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class DropwizardCachingStringVar extends AbstractStringVar implements CachingStringVar {

    public DropwizardCachingStringVar(
        MetricName name,
        CachingVarConfig config,
        Supplier<String> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            STRING_VALUE,
            valueSupplier != null ? new DropwizardCachingValueSupplier<>(config, valueSupplier) : null,
            DefaultStringVarInstanceMaker.INSTANCE,
            executor);
    }

    @Override
    public void register(Supplier<String> valueSupplier, LabelValues labelValues) {
        super.register(
            new DropwizardCachingValueSupplier<>((CachingVarConfig)config(), valueSupplier),
            labelValues);
    }
}
