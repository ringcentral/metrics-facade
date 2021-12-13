package com.ringcentral.platform.metrics.x.var.longVar;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.longVar.AbstractLongVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class XLongVar extends AbstractLongVar {

    public XLongVar(
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