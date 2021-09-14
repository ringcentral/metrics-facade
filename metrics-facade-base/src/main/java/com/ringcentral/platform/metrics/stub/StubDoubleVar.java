package com.ringcentral.platform.metrics.stub;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.doubleVar.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class StubDoubleVar extends AbstractDoubleVar implements DoubleVar {

    public StubDoubleVar(
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