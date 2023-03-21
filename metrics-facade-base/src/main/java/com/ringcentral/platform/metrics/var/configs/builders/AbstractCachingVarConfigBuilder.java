package com.ringcentral.platform.metrics.var.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.builders.MetricConfigBuilder;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

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
        String description,
        LabelValues prefixLabelValues,
        List<Label> labels,
        boolean nonDecreasing,
        MetricContext context) {

        return buildImpl(
            enabled,
            description,
            prefixLabelValues,
            labels,
            nonDecreasing,
            context,
            DEFAULT_TTL_SEC,
            SECONDS);
    }

    @Override
    public C build() {
        return buildImpl(
            hasEnabled() ? getEnabled() : DEFAULT_ENABLED,
            description(),
            prefixLabelValues(),
            labels(),
            hasNonDecreasing() ? getNonDecreasing() : DEFAULT_NON_DECREASING,
            context().unmodifiable(),
            hasTtl() ? ttl : DEFAULT_TTL_SEC,
            hasTtl() ? ttlUnit : SECONDS);
    }

    protected abstract C buildImpl(
        boolean enabled,
        String description,
        LabelValues prefixLabelValues,
        List<Label> labels,
        boolean nonDecreasing,
        MetricContext context,
        long ttl,
        TimeUnit ttlUnit);
}
