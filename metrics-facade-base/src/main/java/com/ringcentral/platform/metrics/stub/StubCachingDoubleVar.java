package com.ringcentral.platform.metrics.stub;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.AbstractVar;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.doubleVar.CachingDoubleVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class StubCachingDoubleVar extends AbstractVar<Double> implements CachingDoubleVar {

    public StubCachingDoubleVar(
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