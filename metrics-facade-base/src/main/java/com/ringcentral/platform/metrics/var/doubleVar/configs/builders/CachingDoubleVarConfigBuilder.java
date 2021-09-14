package com.ringcentral.platform.metrics.var.doubleVar.configs.builders;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.builders.AbstractCachingVarConfigBuilder;
import com.ringcentral.platform.metrics.var.doubleVar.configs.*;

public class CachingDoubleVarConfigBuilder extends AbstractCachingVarConfigBuilder<CachingDoubleVarConfig, CachingDoubleVarConfigBuilder> {

    public static CachingDoubleVarConfigBuilder cachingDoubleVar() {
        return cachingDoubleVarConfigBuilder();
    }

    public static CachingDoubleVarConfigBuilder withCachingDoubleVar() {
        return cachingDoubleVarConfigBuilder();
    }

    public static CachingDoubleVarConfigBuilder cachingDoubleVarConfigBuilder() {
        return new CachingDoubleVarConfigBuilder();
    }

    @Override
    protected CachingDoubleVarConfig buildImpl(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricContext context,
        long ttl,
        TimeUnit ttlUnit) {

        return new DefaultCachingDoubleVarConfig(
            enabled,
            prefixDimensionValues,
            dimensions,
            context,
            ttl,
            ttlUnit);
    }
}
