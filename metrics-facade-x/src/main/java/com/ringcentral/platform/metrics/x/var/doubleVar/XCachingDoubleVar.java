package com.ringcentral.platform.metrics.x.var.doubleVar;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.x.var.XCachingValueSupplier;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import com.ringcentral.platform.metrics.var.doubleVar.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class XCachingDoubleVar extends AbstractDoubleVar implements CachingDoubleVar {

    public XCachingDoubleVar(
        MetricName name,
        CachingVarConfig config,
        Supplier<Double> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            DOUBLE_VALUE,
            valueSupplier != null ? new XCachingValueSupplier<>(config, valueSupplier) : null,
            DefaultDoubleVarInstanceMaker.INSTANCE,
            executor);
    }

    @Override
    public void register(Supplier<Double> valueSupplier, MetricDimensionValues dimensionValues) {
        super.register(
            new XCachingValueSupplier<>((CachingVarConfig)config(), valueSupplier),
            dimensionValues);
    }
}
