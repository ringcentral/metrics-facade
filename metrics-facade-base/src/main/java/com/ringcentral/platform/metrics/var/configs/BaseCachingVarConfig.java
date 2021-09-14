package com.ringcentral.platform.metrics.var.configs;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;

public class BaseCachingVarConfig extends AbstractCachingVarConfig {

    public BaseCachingVarConfig(
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
