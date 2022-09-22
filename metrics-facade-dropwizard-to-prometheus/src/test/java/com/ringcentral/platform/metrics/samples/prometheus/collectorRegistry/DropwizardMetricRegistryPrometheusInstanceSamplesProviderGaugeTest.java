package com.ringcentral.platform.metrics.samples.prometheus.collectorRegistry;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static com.ringcentral.platform.metrics.samples.prometheus.collectorRegistry.DropwizardMetricRegistryPrometheusInstanceSamplesProviderTest.checkPrometheusSample;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@SuppressWarnings("ALL")
@RunWith(Parameterized.class)
public class DropwizardMetricRegistryPrometheusInstanceSamplesProviderGaugeTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {(MetricRegistry.MetricSupplier<Gauge>) () -> (Gauge<Number>) () -> 42, 42D},
            {(MetricRegistry.MetricSupplier<Gauge>) () -> (Gauge<Integer>) () -> 42, 42D},
            {(MetricRegistry.MetricSupplier<Gauge>) () -> (Gauge<Long>) () -> 42L, 42D},
            {(MetricRegistry.MetricSupplier<Gauge>) () -> (Gauge<Double>) () -> 42.5, 42.5D},
            {(MetricRegistry.MetricSupplier<Gauge>) () -> (Gauge<Float>) () -> 42.5f, 42.5D},
            {(MetricRegistry.MetricSupplier<Gauge>) () -> (Gauge<Boolean>) () -> true, 1.0D},
            {(MetricRegistry.MetricSupplier<Gauge>) () -> (Gauge<Boolean>) () -> false, 0.0D}
        });
    }

    @Parameter
    public MetricRegistry.MetricSupplier<Gauge> gaugeSupplier;

    @Parameter(1)
    public double expectedValue;

    private final MetricRegistry registry = new MetricRegistry();

    private final DropwizardMetricRegistryPrometheusInstanceSamplesProvider underTest = new DropwizardMetricRegistryPrometheusInstanceSamplesProvider(registry);

    @Test
    public void when_number_gauge_is_registered_then_return_instance_for_it() {
        // given
        registry.gauge("g1", gaugeSupplier);

        // when
        final var instanceSamples = underTest.instanceSamples();

        // then
        final var samplesList = new ArrayList<>(instanceSamples);

        assertThat(samplesList.size(), equalTo(1));

        final var instanceSample = samplesList.get(0);
        final var samples = instanceSample.samples();
        assertThat(samples.size(), equalTo(1));
        final var sample = samples.get(0);
        checkPrometheusSample(sample, "g1", expectedValue);
    }
}