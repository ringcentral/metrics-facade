package com.ringcentral.platform.metrics.var.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public abstract class AbstractCachingVarConfig extends AbstractVarConfig implements CachingVarConfig {

    private final long ttl;
    private final TimeUnit ttlUnit;

    protected AbstractCachingVarConfig(
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
            context);

        this.ttl = ttl;
        this.ttlUnit = requireNonNull(ttlUnit);
    }

    @Override
    public long ttl() {
        return ttl;
    }

    @Override
    public TimeUnit ttlUnit() {
        return ttlUnit;
    }
}
