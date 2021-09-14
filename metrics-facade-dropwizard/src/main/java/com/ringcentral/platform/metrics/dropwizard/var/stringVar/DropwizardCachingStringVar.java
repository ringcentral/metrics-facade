package com.ringcentral.platform.metrics.dropwizard.var.stringVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import com.ringcentral.platform.metrics.dropwizard.var.DropwizardCachingVar;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import com.ringcentral.platform.metrics.var.stringVar.CachingStringVar;

public class DropwizardCachingStringVar extends DropwizardCachingVar<String> implements CachingStringVar {

    public DropwizardCachingStringVar(
        MetricName name,
        CachingVarConfig config,
        Supplier<String> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            STRING_VALUE,
            valueSupplier,
            executor);
    }
}
