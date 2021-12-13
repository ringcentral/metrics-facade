package com.ringcentral.platform.metrics.x.var.stringVar;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.stringVar.AbstractStringVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class XStringVar extends AbstractStringVar {

    public XStringVar(
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