package com.ringcentral.platform.metrics.var.objectVar.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.builders.AbstractCachingVarConfigBuilder;
import com.ringcentral.platform.metrics.var.objectVar.configs.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
        String description,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        boolean nonDecreasing,
        MetricContext context,
        long ttl,
        TimeUnit ttlUnit) {

        return new DefaultCachingObjectVarConfig(
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
