package com.ringcentral.platform.metrics.stub;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.AbstractVar;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.longVar.CachingLongVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class StubCachingLongVar extends AbstractVar<Long> implements CachingLongVar {

    public StubCachingLongVar(
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