package com.ringcentral.platform.metrics.stub;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.AbstractVar;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.stringVar.CachingStringVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class StubCachingStringVar extends AbstractVar<String> implements CachingStringVar {

    public StubCachingStringVar(
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