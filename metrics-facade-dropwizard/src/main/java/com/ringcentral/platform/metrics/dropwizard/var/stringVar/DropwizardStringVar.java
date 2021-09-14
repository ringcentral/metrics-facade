package com.ringcentral.platform.metrics.dropwizard.var.stringVar;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.stringVar.AbstractStringVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class DropwizardStringVar extends AbstractStringVar {

    public DropwizardStringVar(
        MetricName name,
        VarConfig config,
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