package com.ringcentral.platform.metrics.defaultImpl;

import com.ringcentral.platform.metrics.AbstractMetricRegistryTest;

public class DefaultMetricRegistryTest extends AbstractMetricRegistryTest<DefaultMetricRegistry> {

    public DefaultMetricRegistryTest() {
        super(new DefaultMetricRegistry());
    }
}