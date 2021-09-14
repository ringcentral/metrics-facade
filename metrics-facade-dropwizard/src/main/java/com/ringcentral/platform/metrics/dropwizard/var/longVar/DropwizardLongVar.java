package com.ringcentral.platform.metrics.dropwizard.var.longVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import com.ringcentral.platform.metrics.var.AbstractVar;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.longVar.LongVar;

public class DropwizardLongVar extends AbstractVar<Long> implements LongVar {

    public DropwizardLongVar(
        MetricName name,
        VarConfig config,
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