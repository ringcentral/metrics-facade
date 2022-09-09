package com.ringcentral.platform.metrics.dropwizard.samples;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.samples.DefaultSample;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class DropwizardMetricRegistryDefaultInstanceSamplesProviderTest {

    private final MetricRegistry registry = new MetricRegistry();
    private final DropwizardMetricRegistryDefaultInstanceSamplesProvider underTest =
            new DropwizardMetricRegistryDefaultInstanceSamplesProvider(registry);

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
        final var samples = instanceSample.samples();
        assertThat(samples.size(), equalTo(1));
        final var sample = samples.get(0);
        assertThat(sample.name(), equalTo("c1"));
        assertThat(sample.value(), equalTo(5L));
        assertThat(sample.type(), equalTo("delta"));
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
        h1.update(42);

        // when
        final var instanceSamples = underTest.instanceSamples();

        // then
        assertThat(instanceSamples.size(), equalTo(1));

        final var samplesList = new ArrayList<>(instanceSamples);
        final var instanceSample = samplesList.get(0);
        final var samples = instanceSample.samples();
        assertThat(samples, hasItems(
            new DefaultSample("h1.count", 1L, "delta"),
            new DefaultSample("h1.mean", 42.0D, "instant"),
            new DefaultSample("h1.max", 42L, "instant"),
            new DefaultSample("h1.min", 42L, "instant"),
            new DefaultSample("h1.median", 42.0D, "instant"),
            new DefaultSample("h1.std_dev", 0.0D, "instant"),
            new DefaultSample("h1.75_percentile", 42.0D, "instant"),
            new DefaultSample("h1.95_percentile", 42.0D, "instant"),
            new DefaultSample("h1.98_percentile", 42.0D, "instant"),
            new DefaultSample("h1.99_percentile", 42.0D, "instant"),
            new DefaultSample("h1.999_percentile", 42.0D, "instant")
        ));
        assertThat(samples.size(), equalTo(11));
    }

    @Test
    public void when_timer_then_return_instance_for_it() {
        // given
        final var t1 = registry.timer("t1");
        t1.update(Duration.ofMillis(1500));

        // when
        final var instanceSamples = underTest.instanceSamples();

        // then
        assertThat(instanceSamples.size(), equalTo(1));

        final var samplesList = new ArrayList<>(instanceSamples);
        final var instanceSample = samplesList.get(0);
        final var samples = instanceSample.samples();
        assertThat(samples, hasItems(
            new DefaultSample("t1.count", 1L, "delta"),
            new DefaultSample("t1.mean", 1.5D, "instant"),
            new DefaultSample("t1.max", 1.5D, "instant"),
            new DefaultSample("t1.min", 1.5D, "instant"),
            new DefaultSample("t1.median", 1.5D, "instant"),
            new DefaultSample("t1.std_dev", 0.0D, "instant"),
            new DefaultSample("t1.75_percentile", 1.5D, "instant"),
            new DefaultSample("t1.95_percentile", 1.5D, "instant"),
            new DefaultSample("t1.98_percentile", 1.5D, "instant"),
            new DefaultSample("t1.99_percentile", 1.5D, "instant"),
            new DefaultSample("t1.999_percentile", 1.5D, "instant")
        ));
        assertThat(samples.size(), equalTo(11));
    }

    @Test
    public void when_meter_then_return_instance_for_it() {
        // given
        final var m1 = registry.meter("m1");
        m1.mark(10);

        // when
        final var instanceSamples = underTest.instanceSamples();

        // then
        assertThat(instanceSamples.size(), equalTo(1));

        final var samplesList = new ArrayList<>(instanceSamples);
        final var instanceSample = samplesList.get(0);
        final var samples = instanceSample.samples();
        assertThat(samples, hasItems(
            new DefaultSample("m1.total", 10L, "delta"),
            new DefaultSample("m1.1_minute_rate", 0.0D, "instant"),
            new DefaultSample("m1.5_minute_rate", 0.0D, "instant"),
            new DefaultSample("m1.15_minute_rate", 0.0D, "instant")
        ));

        // it's difficult to calculate value for mean rate
        final var meanRateSample = samples.get(4);
        assertThat(meanRateSample.name(), equalTo("m1.mean_rate"));
        assertThat((double) meanRateSample.value() > 1, equalTo(true));
        assertThat(meanRateSample.type(), equalTo("instant"));

        assertThat(samples.size(), equalTo(5));
    }
}