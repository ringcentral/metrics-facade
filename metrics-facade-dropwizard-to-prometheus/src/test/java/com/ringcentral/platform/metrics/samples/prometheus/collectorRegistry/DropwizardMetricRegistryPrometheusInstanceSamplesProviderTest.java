package com.ringcentral.platform.metrics.samples.prometheus.collectorRegistry;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusSample;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class DropwizardMetricRegistryPrometheusInstanceSamplesProviderTest {

    private final MetricRegistry registry = new MetricRegistry();
    private final DropwizardMetricRegistryPrometheusInstanceSamplesProvider underTest = new DropwizardMetricRegistryPrometheusInstanceSamplesProvider(registry);

    @Test
    public void when_registry_is_empty_then_return_empty_instance_samples() {
        // given, when
        final var instanceSamples = underTest.instanceSamples();

        // then
        assertThat(instanceSamples.isEmpty(), equalTo(true));
    }

    @Test
    public void when_counter_is_registered_then_return_instance_for_counter() {
        // given
        final var counter = registry.counter("c1");
        counter.inc(5);

        // when
        final var instanceSamples = underTest.instanceSamples();

        // then
        final var samplesList = new ArrayList<>(instanceSamples);
        assertThat(samplesList.size(), equalTo(1));

        final var instanceSample = samplesList.get(0);
        assertThat(instanceSample.description(), equalTo("Generated from Dropwizard metric import (metric=c1, type=com.codahale.metrics.Counter)"));

        final var samples = instanceSample.samples();
        assertThat(samples.size(), equalTo(1));
        final var sample = samples.get(0);
        assertThat(sample.name(), equalTo(MetricName.name("c1")));
        assertThat(sample.value(), equalTo(5.0D));

        assertThat(sample.labelNames().isEmpty(), equalTo(true));
        assertThat(sample.labelValues().isEmpty(), equalTo(true));
    }

    @Test
    public void when_non_number_gauge_is_registered_then_do_not_return_instance_for_it() {
        // given
        registry.gauge("g1", () -> (Gauge<Object>) Object::new);

        // when
        final var instanceSamples = underTest.instanceSamples();

        // then
        assertThat(instanceSamples.isEmpty(), equalTo(true));
    }

    @Test
    public void when_histogram_then_return_instance_for_it() {
        // given
        final var h1 = registry.histogram("h1");
        for (int i = 1; i <= 100; i++) {
            h1.update(i);
        }

        // when
        final var instanceSamples = underTest.instanceSamples();

        // then
        assertThat(instanceSamples.size(), equalTo(1));

        final var samplesList = new ArrayList<>(instanceSamples);
        final var instanceSample = samplesList.get(0);
        final var samples = instanceSample.samples();
        final var quantile50Sample = samples.get(0);
        checkPrometheusSample(quantile50Sample, "h1", 50.0D, List.of("quantile"), List.of("0.5"));
        final var quantile75Sample = samples.get(1);
        checkPrometheusSample(quantile75Sample, "h1", 75.0D, List.of("quantile"), List.of("0.75"));
        final var quantile95Sample = samples.get(2);
        checkPrometheusSample(quantile95Sample, "h1", 95.0D, List.of("quantile"), List.of("0.95"));
        final var quantile98Sample = samples.get(3);
        checkPrometheusSample(quantile98Sample, "h1", 98.0D, List.of("quantile"), List.of("0.98"));
        final var quantile99Sample = samples.get(4);
        checkPrometheusSample(quantile99Sample, "h1", 99.0D, List.of("quantile"), List.of("0.99"));
        final var quantile999Sample = samples.get(5);
        checkPrometheusSample(quantile999Sample, "h1", 100.0D, List.of("quantile"), List.of("0.999"));
        final var countSample = samples.get(6);
        checkPrometheusSample(countSample, "h1_count", 100.0D);

        assertThat(samples.size(), equalTo(7));
    }

    @Test
    public void when_timer_then_return_instance_for_it() {
        // given
        final var t1 = registry.timer("t1");
        for (int i = 1; i <= 100; i++) {
            t1.update(Duration.ofMillis(i * 1000));
        }

        // when
        final var instanceSamples = underTest.instanceSamples();

        // then
        assertThat(instanceSamples.size(), equalTo(1));

        final var samplesList = new ArrayList<>(instanceSamples);
        final var instanceSample = samplesList.get(0);
        final var samples = instanceSample.samples();

        final var quantile50Sample = samples.get(0);
        checkPrometheusSample(quantile50Sample, "t1", 50.0D, List.of("quantile"), List.of("0.5"));
        final var quantile75Sample = samples.get(1);
        checkPrometheusSample(quantile75Sample, "t1", 75.0D, List.of("quantile"), List.of("0.75"));
        final var quantile95Sample = samples.get(2);
        checkPrometheusSample(quantile95Sample, "t1", 95.0D, List.of("quantile"), List.of("0.95"));
        final var quantile98Sample = samples.get(3);
        checkPrometheusSample(quantile98Sample, "t1", 98.0D, List.of("quantile"), List.of("0.98"));
        final var quantile99Sample = samples.get(4);
        checkPrometheusSample(quantile99Sample, "t1", 99.0D, List.of("quantile"), List.of("0.99"));
        final var quantile999Sample = samples.get(5);
        checkPrometheusSample(quantile999Sample, "t1", 100.0D, List.of("quantile"), List.of("0.999"));
        final var countSample = samples.get(6);
        checkPrometheusSample(countSample, "t1_count", 100.0D);

        assertThat(samples.size(), equalTo(7));
    }

    @Test
    public void when_meter_then_return_instance_for_it() {
        // given
        final var m1 = registry.meter("m1");
        m1.mark(10);
        m1.mark(12);

        // when
        final var instanceSamples = underTest.instanceSamples();

        // then
        assertThat(instanceSamples.size(), equalTo(1));

        final var samplesList = new ArrayList<>(instanceSamples);
        final var instanceSample = samplesList.get(0);
        final var samples = instanceSample.samples();

        final var countSample = samples.get(0);
        checkPrometheusSample(countSample, "m1_total", 22.0D);

        assertThat(samples.size(), equalTo(1));
    }

    static void checkPrometheusSample(
            PrometheusSample sample,
            String name,
            double value
    ) {
        checkPrometheusSample(sample, name, value, Collections.emptyList(), Collections.emptyList());
    }

    static void checkPrometheusSample(
            PrometheusSample sample,
            String name,
            double value,
            List<String> labelNames,
            List<String> labelValues

    ) {
        assertThat(sample.name(), equalTo(MetricName.name(name)));
        assertThat(sample.value(), equalTo(value));
        assertThat(sample.labelNames(), equalTo(labelNames));
        assertThat(sample.labelValues(), equalTo(labelValues));
    }
}