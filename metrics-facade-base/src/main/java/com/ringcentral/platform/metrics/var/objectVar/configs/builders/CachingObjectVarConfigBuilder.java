package com.ringcentral.platform.metrics.var.objectVar.configs.builders;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.builders.AbstractCachingVarConfigBuilder;
import com.ringcentral.platform.metrics.var.objectVar.configs.*;

public class CachingObjectVarConfigBuilder extends AbstractCachingVarConfigBuilder<CachingObjectVarConfig, CachingObjectVarConfigBuilder> {

    public static CachingObjectVarConfigBuilder cachingObjectVar() {
        return cachingObjectVarConfigBuilder();
    }

    public static CachingObjectVarConfigBuilder withCachingObjectVar() {
        return cachingObjectVarConfigBuilder();
    }

    public static CachingObjectVarConfigBuilder cachingObjectVarConfigBuilder() {
        return new CachingObjectVarConfigBuilder();
    }

    @Override
    protected CachingObjectVarConfig buildImpl(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricContext context,
        long ttl,
        TimeUnit ttlUnit) {

        return new DefaultCachingObjectVarConfig(
            enabled,
            prefixDimensionValues,
            dimensions,
            context,
            ttl,
            ttlUnit);
    }
}
