package com.ringcentral.platform.metrics.var.longVar.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.builders.AbstractCachingVarConfigBuilder;
import com.ringcentral.platform.metrics.var.longVar.configs.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CachingLongVarConfigBuilder extends AbstractCachingVarConfigBuilder<CachingLongVarConfig, CachingLongVarConfigBuilder> {

    public static CachingLongVarConfigBuilder cachingLongVar() {
        return cachingLongVarConfigBuilder();
    }

    public static CachingLongVarConfigBuilder withCachingLongVar() {
        return cachingLongVarConfigBuilder();
    }

    public static CachingLongVarConfigBuilder cachingLongVarConfigBuilder() {
        return new CachingLongVarConfigBuilder();
    }

    @Override
    protected CachingLongVarConfig buildImpl(
        boolean enabled,
        String description,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        boolean nonDecreasing,
        MetricContext context,
        long ttl,
        TimeUnit ttlUnit) {

        return new DefaultCachingLongVarConfig(
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
