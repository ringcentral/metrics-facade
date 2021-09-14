package com.ringcentral.platform.metrics.dropwizard.var.objectVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import com.ringcentral.platform.metrics.dropwizard.var.DropwizardCachingVar;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import com.ringcentral.platform.metrics.var.objectVar.CachingObjectVar;

public class DropwizardCachingObjectVar extends DropwizardCachingVar<Object> implements CachingObjectVar {

    public DropwizardCachingObjectVar(
        MetricName name,
        CachingVarConfig config,
        Supplier<Object> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            OBJECT_VALUE,
            valueSupplier,
            executor);
    }
}
