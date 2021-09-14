package com.ringcentral.platform.metrics.var.stringVar.configs.builders;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.builders.AbstractCachingVarConfigBuilder;
import com.ringcentral.platform.metrics.var.stringVar.configs.*;

public class CachingStringVarConfigBuilder extends AbstractCachingVarConfigBuilder<CachingStringVarConfig, CachingStringVarConfigBuilder> {

    public static CachingStringVarConfigBuilder cachingStringVar() {
        return cachingStringVarConfigBuilder();
    }

    public static CachingStringVarConfigBuilder withCachingStringVar() {
        return cachingStringVarConfigBuilder();
    }

    public static CachingStringVarConfigBuilder cachingStringVarConfigBuilder() {
        return new CachingStringVarConfigBuilder();
    }

    @Override
    protected CachingStringVarConfig buildImpl(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricContext context,
        long ttl,
        TimeUnit ttlUnit) {

        return new DefaultCachingStringVarConfig(
            enabled,
            prefixDimensionValues,
            dimensions,
            context,
            ttl,
            ttlUnit);
    }
}
