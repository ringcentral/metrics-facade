package com.ringcentral.platform.metrics.samples.prometheus;

import io.prometheus.client.Collector;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.names.MetricName.name;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PrometheusInstanceSampleTest {

    @Test
    public void addingSamples() {
        PrometheusInstanceSample instanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b", "c"),
            "Description for " + name("a", "b"),
            Collector.Type.UNKNOWN);

        instanceSample.add(new PrometheusSample(
            COUNT,
            null,
            null,
            null,
            "suffix_1",
            emptyList(),
            emptyList(),
            1.0));

        instanceSample.add(new PrometheusSample(
            MIN,
            name("childSuffix_1"),
            Collector.Type.SUMMARY,
            null,
            "suffix_2",
            List.of("dimension_1", "dimension_2"),
            List.of("dimension_1_value", "dimension_2_value"),
            2.0));

        instanceSample.add(new PrometheusSample(
            MAX,
            name("childSuffix_2"),
            Collector.Type.HISTOGRAM,
            null,
            null,
            List.of("dimension_1"),
            List.of("dimension_1_value"),
            3.0));

        instanceSample.add(new PrometheusSample(
            MEAN,
            name("childSuffix_1"),
            Collector.Type.SUMMARY,
            null,
            "suffix_2",
            List.of("dimension_1", "dimension_2"),
            List.of("dimension_1_value", "dimension_2_value"),
            4.0));

        instanceSample.add(new PrometheusSample(
            PERCENTILE_50,
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
                COUNT,
                null,
                null,
                null,
                "suffix_1",
                emptyList(),
                emptyList(),
                1.0));

        check(
            instanceSample.samples().get(1),
            new PrometheusSample(
                PERCENTILE_50,
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
                MIN,
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
                MEAN,
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
                MAX,
                null,
                null,
                null,
                null,
                List.of("dimension_1"),
                List.of("dimension_1_value"),
                3.0));
    }

    @Test
    public void sortingSamples() {
        PrometheusInstanceSample instanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b", "c"),
            "Description for " + name("a", "b"),
            Collector.Type.HISTOGRAM);

        instanceSample.add(new PrometheusSample(
            COUNT,
            null,
            null,
            null,
            "count",
            emptyList(),
            emptyList(),
            1.0));

        instanceSample.add(new PrometheusSample(
            TOTAL_SUM,
            null,
            null,
            null,
            "total_sum",
            emptyList(),
            emptyList(),
            2.0));

        instanceSample.add(new PrometheusSample(
            PERCENTILE_50,
            null,
            null,
            null,
            "50_percentile",
            Collections.emptyList(),
            Collections.emptyList(),
            50.0));

        instanceSample.add(new PrometheusSample(
            PERCENTILE_1,
            null,
            null,
            null,
            "1_percentile",
            Collections.emptyList(),
            Collections.emptyList(),
            1.0));

        instanceSample.add(new PrometheusSample(
            PERCENTILE_5,
            null,
            null,
            null,
            "5_percentile",
            Collections.emptyList(),
            Collections.emptyList(),
            5.0));

        instanceSample.add(new PrometheusSample(
            INF_BUCKET,
            null,
            null,
            null,
            "inf_bucket",
            Collections.emptyList(),
            Collections.emptyList(),
            10.0));

        instanceSample.add(new PrometheusSample(
            MS_5_BUCKET,
            null,
            null,
            null,
            "ms_5_bucket",
            Collections.emptyList(),
            Collections.emptyList(),
            5.0));

        instanceSample.add(new PrometheusSample(
            MS_1_BUCKET,
            null,
            null,
            null,
            "ms_1_bucket",
            Collections.emptyList(),
            Collections.emptyList(),
            1.0));

        assertThat(instanceSample.samples().size(), is(8));

        check(
            instanceSample.samples().get(0),
            new PrometheusSample(
                MS_1_BUCKET,
                null,
                null,
                null,
                "ms_1_bucket",
                emptyList(),
                emptyList(),
                1.0));

        check(
            instanceSample.samples().get(1),
            new PrometheusSample(
                MS_5_BUCKET,
                null,
                null,
                null,
                "ms_5_bucket",
                emptyList(),
                emptyList(),
                5.0));

        check(
            instanceSample.samples().get(2),
            new PrometheusSample(
                INF_BUCKET,
                null,
                null,
                null,
                "inf_bucket",
                emptyList(),
                emptyList(),
                10.0));

        check(
            instanceSample.samples().get(3),
            new PrometheusSample(
                PERCENTILE_1,
                null,
                null,
                null,
                "1_percentile",
                emptyList(),
                emptyList(),
                1.0));

        check(
            instanceSample.samples().get(4),
            new PrometheusSample(
                PERCENTILE_5,
                null,
                null,
                null,
                "5_percentile",
                emptyList(),
                emptyList(),
                5.0));

        check(
            instanceSample.samples().get(5),
            new PrometheusSample(
                PERCENTILE_50,
                null,
                null,
                null,
                "50_percentile",
                emptyList(),
                emptyList(),
                50.0));

        check(
            instanceSample.samples().get(6),
            new PrometheusSample(
                COUNT,
                null,
                null,
                null,
                "count",
                emptyList(),
                emptyList(),
                1.0));

        check(
            instanceSample.samples().get(7),
            new PrometheusSample(
                TOTAL_SUM,
                null,
                null,
                null,
                "total_sum",
                emptyList(),
                emptyList(),
                2.0));
    }

    public void check(PrometheusSample actual, PrometheusSample expected) {
        assertThat(actual.measurable(), is(expected.measurable()));
        assertThat(actual.childInstanceSampleNameSuffix(), is(expected.childInstanceSampleNameSuffix()));
        assertThat(actual.childInstanceSampleType(), is(expected.childInstanceSampleType()));
        assertThat(actual.nameSuffix(), is(expected.nameSuffix()));
        assertThat(actual.labelNames(), is(expected.labelNames()));
        assertThat(actual.labelValues(), is(expected.labelValues()));
        assertThat(actual.value(), is(expected.value()));
    }
}