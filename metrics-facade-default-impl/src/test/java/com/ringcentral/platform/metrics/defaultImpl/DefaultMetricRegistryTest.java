package com.ringcentral.platform.metrics.defaultImpl;

import com.ringcentral.platform.metrics.AbstractMetricRegistryTest;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.rate.Rate;
import org.junit.Test;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistryBuilder.defaultMetricRegistry;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.custom.DefaultTestCustomHistogramConfigBuilder.customHistogram_Default;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.custom.makerWithSuperclass.TestCustomHistogramConfigBuilderForMakerWithSuperclass.customHistogram_MakerWithSuperclass;
import static com.ringcentral.platform.metrics.defaultImpl.rate.custom.DefaultTestCustomRateConfigBuilder.customRate_Default;
import static com.ringcentral.platform.metrics.defaultImpl.rate.custom.makerWithSuperclass.TestCustomRateConfigBuilderForMakerWithSuperclass.customRate_MakerWithSuperclass;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.rate.configs.builders.RateConfigBuilder.withRate;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DefaultMetricRegistryTest extends AbstractMetricRegistryTest<DefaultMetricRegistry> {

    public DefaultMetricRegistryTest() {
        super(new DefaultMetricRegistry());
    }

    @Test
    public void rateImplsDiscovery() {
        registry = defaultMetricRegistry()
            .withCustomMetricImplsFromPackages("com.ringcentral.platform.metrics.defaultImpl.rate.custom")
            .build();

        Rate rate = registry.rate(
            name("default"),
            () -> withRate().impl(customRate_Default().measurableValue(1L)));

        assertThat(rate.iterator().next().valueOf(COUNT), is(1L));

        rate = registry.rate(
            name("makerWithSuperclass"),
            () -> withRate().impl(customRate_MakerWithSuperclass().measurableValue(2L)));

        assertThat(rate.iterator().next().valueOf(COUNT), is(2L));
    }

    @Test
    public void histogramImplsDiscovery() {
        registry = defaultMetricRegistry()
            .withCustomMetricImplsFromPackages("com.ringcentral.platform.metrics.defaultImpl.histogram.custom")
            .build();

        Histogram histogram = registry.histogram(
            name("default"),
            () -> withHistogram().impl(customHistogram_Default().measurableValue(1L)));

        assertThat(histogram.iterator().next().valueOf(COUNT), is(1L));

        histogram = registry.histogram(
            name("makerWithSuperclass"),
            () -> withHistogram().impl(customHistogram_MakerWithSuperclass().measurableValue(2L)));

        assertThat(histogram.iterator().next().valueOf(COUNT), is(2L));
    }
}