package com.ringcentral.platform.metrics.defaultImpl.var.stringVar;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.stringVar.AbstractStringVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class DefaultStringVar extends AbstractStringVar {

    public DefaultStringVar(
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