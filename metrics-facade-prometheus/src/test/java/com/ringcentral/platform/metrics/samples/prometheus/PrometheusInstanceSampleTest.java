package com.ringcentral.platform.metrics.samples.prometheus;

import io.prometheus.client.Collector;
import org.junit.Test;

import java.util.*;

import static com.ringcentral.platform.metrics.names.MetricName.name;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class PrometheusInstanceSampleTest {

    @Test
    public void addingSamples() {
        PrometheusInstanceSample instanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b", "c"),
            "Description for " + name("a", "b"),
            Collector.Type.UNKNOWN);

        instanceSample.add(new PrometheusSample(
            null,
            null,
            null,
            "suffix_1",
            Collections.emptyList(),
            Collections.emptyList(),
            1.0));

        instanceSample.add(new PrometheusSample(
            name("childSuffix_1"),
            Collector.Type.SUMMARY,
            null,
            "suffix_2",
            List.of("dimension_1", "dimension_2"),
            List.of("dimension_1_value", "dimension_2_value"),
            2.0));

        instanceSample.add(new PrometheusSample(
            name("childSuffix_2"),
            Collector.Type.HISTOGRAM,
            null,
            null,
            List.of("dimension_1"),
            List.of("dimension_1_value"),
            3.0));

        instanceSample.add(new PrometheusSample(
            name("childSuffix_1"),
            Collector.Type.SUMMARY,
            null,
            "suffix_2",
            List.of("dimension_1", "dimension_2"),
            List.of("dimension_1_value", "dimension_2_value"),
            4.0));

        instanceSample.add(new PrometheusSample(
            null,
            null,
            null,
            "suffix_3",
            List.of("dimension_1", "dimension_2", "dimension_3"),
            List.of("dimension_1_value", "dimension_2_value", "dimension_3_value"),
            5.0));

        assertThat(instanceSample.samples().size(), is(2));

        check(
            instanceSample.samples().get(0),
            new PrometheusSample(
                null,
                null,
                null,
                "suffix_1",
                Collections.emptyList(),
                Collections.emptyList(),
                1.0));

        check(
            instanceSample.samples().get(1),
            new PrometheusSample(
                null,
                null,
                null,
                "suffix_3",
                List.of("dimension_1", "dimension_2", "dimension_3"),
                List.of("dimension_1_value", "dimension_2_value", "dimension_3_value"),
                5.0));

        assertTrue(instanceSample.hasChildren());
        assertThat(instanceSample.children().size(), is(2));

        PrometheusInstanceSample child = instanceSample.children().get(0);
        assertFalse(child.hasChildren());
        assertThat(child.samples().size(), is(2));

        check(
            child.samples().get(0),
            new PrometheusSample(
                null,
                null,
                null,
                "suffix_2",
                List.of("dimension_1", "dimension_2"),
                List.of("dimension_1_value", "dimension_2_value"),
                2.0));

        check(
            child.samples().get(1),
            new PrometheusSample(
                null,
                null,
                null,
                "suffix_2",
                List.of("dimension_1", "dimension_2"),
                List.of("dimension_1_value", "dimension_2_value"),
                4.0));

        child = instanceSample.children().get(1);
        assertFalse(child.hasChildren());
        assertThat(child.samples().size(), is(1));

        check(
            child.samples().get(0),
            new PrometheusSample(
                null,
                null,
                null,
                null,
                List.of("dimension_1"),
                List.of("dimension_1_value"),
                3.0));
    }

    public void check(PrometheusSample actual, PrometheusSample expected) {
        assertThat(actual.childInstanceSampleNameSuffix(), is(expected.childInstanceSampleNameSuffix()));
        assertThat(actual.childInstanceSampleType(), is(expected.childInstanceSampleType()));
        assertThat(actual.nameSuffix(), is(expected.nameSuffix()));
        assertThat(actual.labelNames(), is(expected.labelNames()));
        assertThat(actual.labelValues(), is(expected.labelValues()));
        assertThat(actual.value(), is(expected.value()));
    }
}