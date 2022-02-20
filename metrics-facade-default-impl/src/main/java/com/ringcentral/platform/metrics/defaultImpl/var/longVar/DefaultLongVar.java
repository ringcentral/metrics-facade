package com.ringcentral.platform.metrics.defaultImpl.var.longVar;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.longVar.AbstractLongVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class DefaultLongVar extends AbstractLongVar {

    public DefaultLongVar(
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