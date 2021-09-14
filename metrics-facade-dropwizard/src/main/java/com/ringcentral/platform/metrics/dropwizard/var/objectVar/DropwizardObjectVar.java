package com.ringcentral.platform.metrics.dropwizard.var.objectVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.AbstractVar;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.objectVar.ObjectVar;

public class DropwizardObjectVar extends AbstractVar<Object> implements ObjectVar {

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