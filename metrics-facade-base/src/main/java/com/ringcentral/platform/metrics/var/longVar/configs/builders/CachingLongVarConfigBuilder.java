package com.ringcentral.platform.metrics.var.longVar.configs.builders;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.builders.AbstractCachingVarConfigBuilder;
import com.ringcentral.platform.metrics.var.longVar.configs.*;

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
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricContext context,
        long ttl,
        TimeUnit ttlUnit) {

        return new DefaultCachingLongVarConfig(
            enabled,
            prefixDimensionValues,
            dimensions,
            context,
            ttl,
            ttlUnit);
    }
}
