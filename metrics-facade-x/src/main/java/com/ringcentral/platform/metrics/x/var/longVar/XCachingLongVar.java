package com.ringcentral.platform.metrics.x.var.longVar;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.x.var.XCachingValueSupplier;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import com.ringcentral.platform.metrics.var.longVar.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static com.ringcentral.platform.metrics.var.longVar.AbstractLongVar.DefaultLongVarInstanceMaker.INSTANCE;

public class XCachingLongVar extends AbstractLongVar implements CachingLongVar {

    public XCachingLongVar(
        MetricName name,
        CachingVarConfig config,
        Supplier<Long> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            LONG_VALUE,
            valueSupplier != null ? new XCachingValueSupplier<>(config, valueSupplier) : null,
            INSTANCE,
            executor);
    }

    @Override
    public void register(Supplier<Long> valueSupplier, MetricDimensionValues dimensionValues) {
        super.register(
            new XCachingValueSupplier<>((CachingVarConfig)config(), valueSupplier),
            dimensionValues);
    }
}
