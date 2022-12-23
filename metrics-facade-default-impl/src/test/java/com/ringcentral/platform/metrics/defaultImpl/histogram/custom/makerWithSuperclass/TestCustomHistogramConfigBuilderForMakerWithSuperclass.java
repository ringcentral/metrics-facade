package com.ringcentral.platform.metrics.defaultImpl.histogram.custom.makerWithSuperclass;

import com.ringcentral.platform.metrics.defaultImpl.histogram.custom.AbstractTestCustomHistogramConfigBuilder;

public class TestCustomHistogramConfigBuilderForMakerWithSuperclass extends AbstractTestCustomHistogramConfigBuilder<
    TestCustomHistogramImplConfigForMakerWithSuperclass,
    TestCustomHistogramConfigBuilderForMakerWithSuperclass> {

    public static TestCustomHistogramConfigBuilderForMakerWithSuperclass customHistogram_MakerWithSuperclass() {
        return new TestCustomHistogramConfigBuilderForMakerWithSuperclass();
    }

    @Override
    public TestCustomHistogramImplConfigForMakerWithSuperclass build() {
        return new TestCustomHistogramImplConfigForMakerWithSuperclass(measurableValue);
    }
}
