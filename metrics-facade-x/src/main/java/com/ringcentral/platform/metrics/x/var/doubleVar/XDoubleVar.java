package com.ringcentral.platform.metrics.x.var.doubleVar;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.doubleVar.AbstractDoubleVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class XDoubleVar extends AbstractDoubleVar {

    public XDoubleVar(
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