package com.ringcentral.platform.metrics.dropwizard.var.longVar;

import com.ringcentral.platform.metrics.dropwizard.var.DropwizardCachingVar;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import com.ringcentral.platform.metrics.var.longVar.CachingLongVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class DropwizardCachingLongVar extends DropwizardCachingVar<Long> implements CachingLongVar {

    public DropwizardCachingLongVar(
        MetricName name,
        CachingVarConfig config,
        Supplier<Long> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            LONG_VALUE,
            valueSupplier,
            executor);
    }
}
