package com.ringcentral.platform.metrics.stub;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.objectVar.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class StubObjectVar extends AbstractObjectVar implements ObjectVar {

    public StubObjectVar(
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