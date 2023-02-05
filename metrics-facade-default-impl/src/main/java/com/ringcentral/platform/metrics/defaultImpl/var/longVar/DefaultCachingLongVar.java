package com.ringcentral.platform.metrics.defaultImpl.var.longVar;

import com.ringcentral.platform.metrics.defaultImpl.var.DefaultCachingValueSupplier;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import com.ringcentral.platform.metrics.var.longVar.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static com.ringcentral.platform.metrics.var.longVar.AbstractLongVar.DefaultLongVarInstanceMaker.INSTANCE;

public class DefaultCachingLongVar extends AbstractLongVar implements CachingLongVar {

    public DefaultCachingLongVar(
        MetricName name,
        CachingVarConfig config,
        Supplier<Long> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            LONG_VALUE,
            valueSupplier != null ? new DefaultCachingValueSupplier<>(config, valueSupplier) : null,
            INSTANCE,
            executor);
    }

    @Override
    public void register(Supplier<Long> valueSupplier, LabelValues labelValues) {
        super.register(
            new DefaultCachingValueSupplier<>((CachingVarConfig)config(), valueSupplier),
            labelValues);
    }
}
