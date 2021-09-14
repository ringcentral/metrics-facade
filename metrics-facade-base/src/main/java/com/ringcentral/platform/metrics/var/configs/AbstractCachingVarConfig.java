package com.ringcentral.platform.metrics.var.configs;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;

import static java.util.Objects.*;

public abstract class AbstractCachingVarConfig extends AbstractVarConfig implements CachingVarConfig {

    private final long ttl;
    private final TimeUnit ttlUnit;

    protected AbstractCachingVarConfig(
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
