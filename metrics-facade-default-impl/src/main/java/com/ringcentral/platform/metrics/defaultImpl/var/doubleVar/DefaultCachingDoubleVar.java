package com.ringcentral.platform.metrics.defaultImpl.var.doubleVar;

import com.ringcentral.platform.metrics.defaultImpl.var.DefaultCachingValueSupplier;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import com.ringcentral.platform.metrics.var.doubleVar.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class DefaultCachingDoubleVar extends AbstractDoubleVar implements CachingDoubleVar {

    public DefaultCachingDoubleVar(
        MetricName name,
        CachingVarConfig config,
        Supplier<Double> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            DOUBLE_VALUE,
            valueSupplier != null ? new DefaultCachingValueSupplier<>(config, valueSupplier) : null,
            DefaultDoubleVarInstanceMaker.INSTANCE,
            executor);
    }

    @Override
    public void register(Supplier<Double> valueSupplier, MetricDimensionValues dimensionValues) {
        super.register(
            new DefaultCachingValueSupplier<>((CachingVarConfig)config(), valueSupplier),
            dimensionValues);
    }
}
