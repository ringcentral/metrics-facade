package com.ringcentral.platform.metrics.x.var.stringVar;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.x.var.XCachingValueSupplier;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import com.ringcentral.platform.metrics.var.stringVar.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class XCachingStringVar extends AbstractStringVar implements CachingStringVar {

    public XCachingStringVar(
        MetricName name,
        CachingVarConfig config,
        Supplier<String> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            STRING_VALUE,
            valueSupplier != null ? new XCachingValueSupplier<>(config, valueSupplier) : null,
            DefaultStringVarInstanceMaker.INSTANCE,
            executor);
    }

    @Override
    public void register(Supplier<String> valueSupplier, MetricDimensionValues dimensionValues) {
        super.register(
            new XCachingValueSupplier<>((CachingVarConfig)config(), valueSupplier),
            dimensionValues);
    }
}
