package com.ringcentral.platform.metrics.defaultImpl.var.objectVar;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.objectVar.AbstractObjectVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class DefaultObjectVar extends AbstractObjectVar {

    public DefaultObjectVar(
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