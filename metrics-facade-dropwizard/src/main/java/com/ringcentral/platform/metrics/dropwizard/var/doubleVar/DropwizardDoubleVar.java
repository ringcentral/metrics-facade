package com.ringcentral.platform.metrics.dropwizard.var.doubleVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.AbstractVar;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.doubleVar.DoubleVar;

public class DropwizardDoubleVar extends AbstractVar<Double> implements DoubleVar {

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