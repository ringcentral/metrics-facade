package com.ringcentral.platform.metrics.configs.builders;

import com.ringcentral.platform.metrics.configs.BaseMetricConfig;

public class BaseMetricConfigBuilder
    extends AbstractMetricConfigBuilder<BaseMetricConfig, BaseMetricConfigBuilder>
    implements MetricConfigBuilder<BaseMetricConfig> {

    public static BaseMetricConfigBuilder metric() {
        return metricConfigBuilder();
    }

    public static BaseMetricConfigBuilder withMetric() {
        return metricConfigBuilder();
    }

    public static BaseMetricConfigBuilder metricConfigBuilder() {
        return new BaseMetricConfigBuilder();
    }

    @Override
    public BaseMetricConfig build() {
        return new BaseMetricConfig(
            hasEnabled() ? getEnabled() : DEFAULT_ENABLED,
            prefixDimensionValues(),
            context().unmodifiable());
    }

    @Override
    public BaseMetricConfigBuilder builder() {
        return this;
    }
}
