package com.ringcentral.platform.metrics.var.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.var.configs.BaseCachingVarConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BaseCachingVarConfigBuilder extends AbstractCachingVarConfigBuilder<BaseCachingVarConfig, BaseCachingVarConfigBuilder> {

    public static BaseCachingVarConfigBuilder cachingVar() {
        return cachingVarConfigBuilder();
    }

    public static BaseCachingVarConfigBuilder withCachingVar() {
        return cachingVarConfigBuilder();
    }

    public static BaseCachingVarConfigBuilder cachingVarConfigBuilder() {
        return new BaseCachingVarConfigBuilder();
    }

    @Override
    protected BaseCachingVarConfig buildImpl(
        boolean enabled,
        String description,
        LabelValues prefixLabelValues,
        List<Label> labels,
        boolean nonDecreasing,
        MetricContext context,
        long ttl,
        TimeUnit ttlUnit) {

        return new BaseCachingVarConfig(
            enabled,
            description,
            prefixLabelValues,
            labels,
            nonDecreasing,
            context,
            ttl,
            ttlUnit);
    }
}
