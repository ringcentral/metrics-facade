package com.ringcentral.platform.metrics.var.stringVar.configs;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.AbstractCachingVarConfig;

public class DefaultCachingStringVarConfig extends AbstractCachingVarConfig implements CachingStringVarConfig {

    public DefaultCachingStringVarConfig(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricContext context,
        long ttl,
        TimeUnit ttlUnit) {

        super(
            enabled,
            prefixDimensionValues,
            dimensions,
            context,
            ttl,
            ttlUnit);
    }
}