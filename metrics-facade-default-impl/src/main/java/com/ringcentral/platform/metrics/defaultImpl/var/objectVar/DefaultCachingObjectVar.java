package com.ringcentral.platform.metrics.defaultImpl.var.objectVar;

import com.ringcentral.platform.metrics.defaultImpl.var.DefaultCachingValueSupplier;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import com.ringcentral.platform.metrics.var.objectVar.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class DefaultCachingObjectVar extends AbstractObjectVar implements CachingObjectVar {

    public DefaultCachingObjectVar(
        MetricName name,
        CachingVarConfig config,
        Supplier<Object> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            OBJECT_VALUE,
            valueSupplier != null ? new DefaultCachingValueSupplier<>(config, valueSupplier) : null,
            DefaultObjectVarInstanceMaker.INSTANCE,
            executor);
    }

    @Override
    public void register(Supplier<Object> valueSupplier, MetricDimensionValues dimensionValues) {
        super.register(
            new DefaultCachingValueSupplier<>((CachingVarConfig)config(), valueSupplier),
            dimensionValues);
    }
}
