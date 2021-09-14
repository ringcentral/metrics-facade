package com.ringcentral.platform.metrics.configs.builders;

public interface MetricConfigBuilderProvider<CB extends MetricConfigBuilder<?>> {
    CB builder();
}
