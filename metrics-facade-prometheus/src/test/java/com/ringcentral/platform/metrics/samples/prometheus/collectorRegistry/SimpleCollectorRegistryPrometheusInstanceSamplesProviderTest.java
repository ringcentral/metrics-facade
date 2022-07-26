package com.ringcentral.platform.metrics.samples.prometheus.collectorRegistry;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSample;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusSample;
import io.prometheus.client.*;
import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.names.MetricName.name;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class SimpleCollectorRegistryPrometheusInstanceSamplesProviderTest {

    @Test
    public void providingInstanceSamples() {
        CollectorRegistry collectorRegistry = new CollectorRegistry(true);

        SimpleCollectorRegistryPrometheusInstanceSamplesProvider instanceSamplesProvider = new SimpleCollectorRegistryPrometheusInstanceSamplesProvider(
            name("defaultRegistry"), // optional prefix
            new SampleNameFilter.Builder().nameMustNotStartWith("counter_1").build(), // optional filter
            sampleName -> !sampleName.contains("summary") || !sampleName.endsWith("created"), // optional filter
            collectorRegistry);

        Counter counter_1 = Counter.build()
            .name("counter_1")
            .help("Counter 1 from defaultRegistry")
            .register(collectorRegistry);

        counter_1.inc(1);

        Counter counter_2 = Counter.build()
            .name("counter_2")
            .help("Counter 2 from defaultRegistry")
            .register(collectorRegistry);

        counter_2.inc(2);

        Summary summary = Summary.build()
            .name("summary")
            .labelNames("label_1", "label_2")
            .help("Summary from defaultRegistry")
            .register(collectorRegistry);

        summary.labels("label_1_value", "label_2_value").observe(10);
        summary.labels("label_1_value", "label_2_value").observe(20);
        summary.labels("label_1_value", "label_2_value").observe(30);

        List<PrometheusInstanceSample> instanceSamples = instanceSamplesProvider.instanceSamples().stream()
            .sorted(comparing(prometheusInstanceSample -> prometheusInstanceSample.name().toString()))
            .collect(toList());

        assertThat(instanceSamples.size(), is(2));

        PrometheusInstanceSample instanceSample = instanceSamples.stream()
            .filter(is -> is.name().lastPart().equals("counter_2"))
            .findFirst().orElseThrow();

        assertThat(instanceSample.instanceName(), is(name("defaultRegistry", "counter_2")));
        assertThat(instanceSample.name(), is(name("defaultRegistry", "counter_2")));
        assertThat(instanceSample.description(), is("Counter 2 from defaultRegistry"));
        assertThat(instanceSample.type(), is(Collector.Type.COUNTER));
        assertFalse(instanceSample.hasChildren());

        List<PrometheusSample> samples = instanceSample.samples();
        assertThat(samples.size(), is(2));

        PrometheusSample sample = samples.stream()
            .filter(is -> is.name().lastPart().contains("total"))
            .findFirst().orElseThrow();

        assertNull(sample.childInstanceSampleNameSuffix());
        assertNull(sample.childInstanceSampleType());
        assertThat(sample.name(), is(MetricName.of("defaultRegistry", "counter_2_total")));
        assertNull(sample.nameSuffix());
        assertTrue(sample.labelNames().isEmpty());
        assertTrue(sample.labelValues().isEmpty());
        assertThat(sample.value(), is(2.0));

        instanceSample = instanceSamples.stream()
            .filter(is -> is.name().lastPart().equals("summary"))
            .findFirst().orElseThrow();

        assertThat(instanceSample.instanceName(), is(name("defaultRegistry", "summary")));
        assertThat(instanceSample.name(), is(name("defaultRegistry", "summary")));
        assertThat(instanceSample.description(), is("Summary from defaultRegistry"));
        assertThat(instanceSample.type(), is(Collector.Type.SUMMARY));
        assertFalse(instanceSample.hasChildren());

        samples = instanceSample.samples();
        assertThat(samples.size(), is(2));
    }
}