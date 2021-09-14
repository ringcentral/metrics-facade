package com.ringcentral.platform.metrics.var.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.builders.MetricConfigBuilder;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ringcentral.platform.metrics.utils.Preconditions.*;
import static java.util.Objects.*;
import static java.util.concurrent.TimeUnit.*;

public abstract class AbstractCachingVarConfigBuilder<C extends CachingVarConfig, CB extends CachingVarConfigBuilder<C, CB>>
    extends AbstractVarConfigBuilder<C, CB> implements CachingVarConfigBuilder<C, CB> {

    public static final long DEFAULT_TTL_SEC = 30L;

    private Long ttl;
    private TimeUnit ttlUnit;

    @Override
    public void rebase(MetricConfigBuilder<?> base) {
        super.rebase(base);

        if (base instanceof AbstractCachingVarConfigBuilder) {
            AbstractCachingVarConfigBuilder<?, ?> cachingVarBase = (AbstractCachingVarConfigBuilder<?, ?>)base;

            if (cachingVarBase.hasTtl() && !hasTtl()) {
                ttl(cachingVarBase.ttl, cachingVarBase.ttlUnit);
            }
        }
    }

    @Override
    public void modify(MetricConfigBuilder<?> mod) {
        super.modify(mod);

        if (mod instanceof AbstractCachingVarConfigBuilder) {
            AbstractCachingVarConfigBuilder<?, ?> cachingVarMod = (AbstractCachingVarConfigBuilder<?, ?>)mod;

            if (cachingVarMod.hasTtl()) {
                ttl(cachingVarMod.ttl, cachingVarMod.ttlUnit);
            }
        }
    }

    public boolean hasTtl() {
        return ttl != null;
    }

    public CB ttl(long ttl, TimeUnit ttlUnit) {
        checkArgument(ttl > 0L, "ttl <= 0");
        this.ttl = ttl;
        this.ttlUnit = requireNonNull(ttlUnit);
        return builder();
    }

    @Override
    protected C buildImpl(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricContext context) {

        return buildImpl(
            enabled,
            prefixDimensionValues,
            dimensions,
            context,
            DEFAULT_TTL_SEC,
            SECONDS);
    }

    @Override
    public C build() {
        return buildImpl(
            hasEnabled() ? getEnabled() : DEFAULT_ENABLED,
            prefixDimensionValues(),
            dimensions(),
            context().unmodifiable(),
            hasTtl() ? ttl : DEFAULT_TTL_SEC,
            hasTtl() ? ttlUnit : SECONDS);
    }

    protected abstract C buildImpl(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricContext context,
        long ttl,
        TimeUnit ttlUnit);
}
