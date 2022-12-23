package com.ringcentral.platform.metrics.configs.builders;

import com.ringcentral.platform.metrics.ModifiableMetricContext;
import com.ringcentral.platform.metrics.configs.MetricConfig;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.impl.MetricImplConfigBuilder;

import java.util.List;

public abstract class AbstractMetricConfigBuilder<C extends MetricConfig, CB extends MetricConfigBuilder<C>>
    implements MetricConfigBuilder<C>, MetricConfigBuilderProvider<CB> {

    public static final boolean DEFAULT_ENABLED = true;

    private Boolean enabled;
    private String description;
    private MetricDimensionValues prefixDimensionValues;
    private final ModifiableMetricContext context = new ModifiableMetricContext();

    @Override
    public void rebase(MetricConfigBuilder<?> base) {
        if (base instanceof AbstractMetricConfigBuilder) {
            AbstractMetricConfigBuilder<?, ?> metricBase = (AbstractMetricConfigBuilder<?, ?>)base;

            if (metricBase.hasEnabled() && !hasEnabled()) {
                enabled(metricBase.getEnabled());
            }

            if (metricBase.hasDescription() && !hasDescription()) {
                description = metricBase.description;
            }

            if (metricBase.prefixDimensionValues != null && prefixDimensionValues == null) {
                prefixDimensionValues = metricBase.prefixDimensionValues;
            }

            if (!metricBase.context.isEmpty()) {
                context.putIfAbsent(metricBase.context);
            }
        }
    }

    @Override
    public void modify(MetricConfigBuilder<?> mod) {
        if (mod instanceof AbstractMetricConfigBuilder) {
            AbstractMetricConfigBuilder<?, ?> metricMod = (AbstractMetricConfigBuilder<?, ?>)mod;

            if (metricMod.hasEnabled()) {
                enabled(metricMod.getEnabled());
            }

            if (metricMod.hasDescription()) {
                description = metricMod.description;
            }

            if (metricMod.prefixDimensionValues != null) {
                prefixDimensionValues = metricMod.prefixDimensionValues;
            }

            if (!metricMod.context.isEmpty()) {
                context.put(metricMod.context);
            }
        }
    }

    public boolean hasEnabled() {
        return enabled != null;
    }

    public CB enable() {
        return enabled(true);
    }

    public CB disable() {
        return enabled(false);
    }

    public CB enabled(boolean enabled) {
        this.enabled = enabled;
        return builder();
    }

    protected Boolean getEnabled() {
        return enabled;
    }

    protected boolean hasDescription() {
        return description != null;
    }

    public CB description(String description) {
        this.description = description;
        return builder();
    }

    protected String description() {
        return description;
    }

    public CB prefix(MetricDimensionValues dimensionValues) {
        this.prefixDimensionValues = dimensionValues;
        return builder();
    }

    protected MetricDimensionValues prefixDimensionValues() {
        return prefixDimensionValues;
    }

    public CB impl(MetricImplConfigBuilder configBuilder) {
        return with(configBuilder);
    }

    public CB put(Object key, Object value) {
        context.put(key, value);
        return builder();
    }

    public CB with(Object value) {
        context.with(value);
        return builder();
    }

    protected ModifiableMetricContext context() {
        return context;
    }

    protected void checkDimensionsUnique(MetricDimensionValues dimensionValues, List<MetricDimension> dimensions) {
        if (dimensionValues != null && dimensions != null) {
            dimensionValues.list().forEach(dv -> {
                if (dimensions.contains(dv.dimension())) {
                    throw new IllegalArgumentException("Dimensions are not unique");
                }
            });
        }
    }
}
