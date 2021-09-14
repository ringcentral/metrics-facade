package com.ringcentral.platform.metrics.infoProviders;

public class DefaultConcurrentMetricNamedInfoProviderTest extends AbstractMetricNamedInfoProviderTest {

    public DefaultConcurrentMetricNamedInfoProviderTest() {
        super(new DefaultConcurrentMetricNamedInfoProvider<>());
    }
}