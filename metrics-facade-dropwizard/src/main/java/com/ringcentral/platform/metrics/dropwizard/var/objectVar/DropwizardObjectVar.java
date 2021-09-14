package com.ringcentral.platform.metrics.dropwizard.var.objectVar;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.objectVar.AbstractObjectVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class DropwizardObjectVar extends AbstractObjectVar {

    public DropwizardObjectVar(
        MetricName name,
        VarConfig config,
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