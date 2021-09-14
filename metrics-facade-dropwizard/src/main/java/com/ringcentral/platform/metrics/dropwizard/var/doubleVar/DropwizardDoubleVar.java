package com.ringcentral.platform.metrics.dropwizard.var.doubleVar;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.doubleVar.AbstractDoubleVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class DropwizardDoubleVar extends AbstractDoubleVar {

    public DropwizardDoubleVar(
        MetricName name,
        VarConfig config,
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