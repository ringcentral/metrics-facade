package com.ringcentral.platform.metrics.dropwizard.var.doubleVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import com.ringcentral.platform.metrics.dropwizard.var.DropwizardCachingVar;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import com.ringcentral.platform.metrics.var.doubleVar.CachingDoubleVar;

public class DropwizardCachingDoubleVar extends DropwizardCachingVar<Double> implements CachingDoubleVar {

    public DropwizardCachingDoubleVar(
        MetricName name,
        CachingVarConfig config,
        Supplier<Double> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            DOUBLE_VALUE,
            valueSupplier,
            executor);
    }
}
