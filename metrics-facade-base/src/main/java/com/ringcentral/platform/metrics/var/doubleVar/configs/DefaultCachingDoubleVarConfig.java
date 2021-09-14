package com.ringcentral.platform.metrics.var.doubleVar.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.AbstractCachingVarConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DefaultCachingDoubleVarConfig extends AbstractCachingVarConfig implements CachingDoubleVarConfig {

    public DefaultCachingDoubleVarConfig(
        boolean enabled,
        String description,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        boolean nonDecreasing,
        MetricContext context,
        long ttl,
        TimeUnit ttlUnit) {

        super(
            enabled,
            description,
            prefixDimensionValues,
            dimensions,
            nonDecreasing,
            context,
            ttl,
            ttlUnit);
    }
}