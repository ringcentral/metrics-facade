package com.ringcentral.platform.metrics.reporters.prometheus;

import com.ringcentral.platform.metrics.samples.InstanceSamplesProvider;
import com.ringcentral.platform.metrics.samples.prometheus.*;
import io.prometheus.client.Collector;
import org.junit.Test;

import java.util.*;

import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter.Format.*;
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusSampleMaker.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class PrometheusMetricsExporterTest {

    static final String DIMENSION_1 = "dimension_1";
    static final String DIMENSION_2 = "dimension_2";
    static final String DIMENSION_3 = "dimension_3";

    @Test
    public void export_noInstanceSamples() {
        InstanceSamplesProvider<PrometheusSample, PrometheusInstanceSample> instanceSamplesProvider =
            mock(InstanceSamplesProvider.class);

        when(instanceSamplesProvider.instanceSamples()).thenReturn(Set.of());

        PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(instanceSamplesProvider);
        String result = exporter.exportMetrics();
        assertThat(result, is(""));
    }

    @Test
    public void export() {
        InstanceSamplesProvider<PrometheusSample, PrometheusInstanceSample> instanceSamplesProvider =
            mock(InstanceSamplesProvider.class);

        LinkedHashSet<PrometheusInstanceSample> instanceSamples = new LinkedHashSet<>();
        when(instanceSamplesProvider.instanceSamples()).thenReturn(instanceSamples);

        // counter
        PrometheusInstanceSample instanceSample = new PrometheusInstanceSample(
            name("counter", "a"),
            name("a"),
            null,
            Collector.Type.GAUGE);

        instanceSample.add(new PrometheusSample(
            null,
            null,
            null,
            emptyList(),
            emptyList(),
            1.0));

        instanceSamples.add(instanceSample);

        instanceSample = new PrometheusInstanceSample(
            name("counter", "a", "b"),
            name("a", "b"),
            "Description for " + name("a", "b"),
            Collector.Type.GAUGE);

        instanceSample.add(new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1, DIMENSION_2),
            List.of("dimension_1_value", "dimension_2_value"),
            2.0));

        instanceSamples.add(instanceSample);

        // rate
        instanceSample = new PrometheusInstanceSample(
            name("rate", "a", "b"),
            name("a", "b", "total"),
            "Description for " + name("a", "b", "total"),
            Collector.Type.COUNTER);

        instanceSample.add(new PrometheusSample(
            null,
            null,
            null,
            emptyList(),
            emptyList(),
            3.0));

        instanceSamples.add(instanceSample);

        instanceSample = new PrometheusInstanceSample(
            name("rate", "a", "b", "c"),
            name("a", "b", "c", "total"),
            "Description for " + name("a", "b", "c", "total"),
            Collector.Type.COUNTER);

        instanceSample.add(new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1, DIMENSION_2, DIMENSION_3),
            List.of("dimension_1_value", "dimension_2_value", "dimension_3_value"),
            4.0));

        instanceSamples.add(instanceSample);

        // histogram
        instanceSample = new PrometheusInstanceSample(
            name("histogram", "a", "b", "c", "d"),
            name("a", "b", "c", "d"),
            "Description for " + name("a", "b", "c", "d"),
            Collector.Type.SUMMARY);

        instanceSample.add(new PrometheusSample(
            null,
            null,
            "_count",
            emptyList(),
            emptyList(),
            5.0));

        instanceSample.add(new PrometheusSample(
            DEFAULT_MAX_CHILD_NAME_SUFFIX,
            Collector.Type.GAUGE,
            null,
            emptyList(),
            emptyList(),
            6.0));

        instanceSample.add(new PrometheusSample(
            DEFAULT_MEAN_CHILD_NAME_SUFFIX,
            Collector.Type.GAUGE,
            null,
            emptyList(),
            emptyList(),
            7.0));

        instanceSample.add(new PrometheusSample(
            null,
            null,
            null,
            List.of("quantile"),
            List.of("0.50"),
            8.0));

        instanceSample.add(new PrometheusSample(
            null,
            null,
            null,
            List.of("quantile"),
            List.of("0.75"),
            9.0));

        instanceSamples.add(instanceSample);

        instanceSample = new PrometheusInstanceSample(
            name("histogram", "a", "b", "c", "d", "e"),
            name("a", "b", "c", "d", "e"),
            "Description for " + name("a", "b", "c", "d", "e"),
            Collector.Type.SUMMARY);

        instanceSample.add(new PrometheusSample(
            null,
            null,
            "_count",
            List.of(DIMENSION_1, DIMENSION_2, DIMENSION_3),
            List.of("dimension_1_value", "dimension_2_value", "dimension_3_value"),
            10.0));

        instanceSample.add(new PrometheusSample(
            DEFAULT_MAX_CHILD_NAME_SUFFIX,
            Collector.Type.GAUGE,
            null,
            List.of(DIMENSION_1, DIMENSION_2, DIMENSION_3),
            List.of("dimension_1_value", "dimension_2_value", "dimension_3_value"),
            11.0));

        instanceSample.add(new PrometheusSample(
            DEFAULT_MEAN_CHILD_NAME_SUFFIX,
            Collector.Type.GAUGE,
            null,
            List.of(DIMENSION_1, DIMENSION_2, DIMENSION_3),
            List.of("dimension_1_value", "dimension_2_value", "dimension_3_value"),
            12.0));

        instanceSample.add(new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1, DIMENSION_2, DIMENSION_3, "quantile"),
            List.of("dimension_1_value", "dimension_2_value", "dimension_3_value", "0.50"),
            13.0));

        instanceSample.add(new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1, DIMENSION_2, DIMENSION_3, "quantile"),
            List.of("dimension_1_value", "dimension_2_value", "dimension_3_value", "0.75"),
            14.0));

        instanceSamples.add(instanceSample);

        PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(PROMETHEUS_TEXT_O_O_4, instanceSamplesProvider);

        assertThat(exporter.exportMetrics(), is(
            "# HELP a Generated from metric instances with name counter.a\n" +
            "# TYPE a gauge\n" +
            "a 1.0\n" +
            "# HELP a_b Description for a.b\n" +
            "# TYPE a_b gauge\n" +
            "a_b{dimension_1=\"dimension_1_value\",dimension_2=\"dimension_2_value\",} 2.0\n" +
            "# HELP a_b_total Description for a.b.total\n" +
            "# TYPE a_b_total counter\n" +
            "a_b_total 3.0\n" +
            "# HELP a_b_c_total Description for a.b.c.total\n" +
            "# TYPE a_b_c_total counter\n" +
            "a_b_c_total{dimension_1=\"dimension_1_value\",dimension_2=\"dimension_2_value\",dimension_3=\"dimension_3_value\",} 4.0\n" +
            "# HELP a_b_c_d Description for a.b.c.d\n" +
            "# TYPE a_b_c_d summary\n" +
            "a_b_c_d_count 5.0\n" +
            "a_b_c_d{quantile=\"0.50\",} 8.0\n" +
            "a_b_c_d{quantile=\"0.75\",} 9.0\n" +
            "# HELP a_b_c_d_max Description for a.b.c.d\n" +
            "# TYPE a_b_c_d_max gauge\n" +
            "a_b_c_d_max 6.0\n" +
            "# HELP a_b_c_d_mean Description for a.b.c.d\n" +
            "# TYPE a_b_c_d_mean gauge\n" +
            "a_b_c_d_mean 7.0\n" +
            "# HELP a_b_c_d_e Description for a.b.c.d.e\n" +
            "# TYPE a_b_c_d_e summary\n" +
            "a_b_c_d_e_count{dimension_1=\"dimension_1_value\",dimension_2=\"dimension_2_value\",dimension_3=\"dimension_3_value\",} 10.0\n" +
            "a_b_c_d_e{dimension_1=\"dimension_1_value\",dimension_2=\"dimension_2_value\",dimension_3=\"dimension_3_value\",quantile=\"0.50\",} 13.0\n" +
            "a_b_c_d_e{dimension_1=\"dimension_1_value\",dimension_2=\"dimension_2_value\",dimension_3=\"dimension_3_value\",quantile=\"0.75\",} 14.0\n" +
            "# HELP a_b_c_d_e_max Description for a.b.c.d.e\n" +
            "# TYPE a_b_c_d_e_max gauge\n" +
            "a_b_c_d_e_max{dimension_1=\"dimension_1_value\",dimension_2=\"dimension_2_value\",dimension_3=\"dimension_3_value\",} 11.0\n" +
            "# HELP a_b_c_d_e_mean Description for a.b.c.d.e\n" +
            "# TYPE a_b_c_d_e_mean gauge\n" +
            "a_b_c_d_e_mean{dimension_1=\"dimension_1_value\",dimension_2=\"dimension_2_value\",dimension_3=\"dimension_3_value\",} 12.0\n"));

        exporter = new PrometheusMetricsExporter(OPENMETRICS_TEXT_1_0_0, instanceSamplesProvider);

        assertThat(exporter.exportMetrics(), is(
            "# TYPE a gauge\n" +
            "# HELP a Generated from metric instances with name counter.a\n" +
            "a 1.0\n" +
            "# TYPE a_b gauge\n" +
            "# HELP a_b Description for a.b\n" +
            "a_b{dimension_1=\"dimension_1_value\",dimension_2=\"dimension_2_value\"} 2.0\n" +
            "# TYPE a_b counter\n" +
            "# HELP a_b Description for a.b.total\n" +
            "a_b_total 3.0\n" +
            "# TYPE a_b_c counter\n" +
            "# HELP a_b_c Description for a.b.c.total\n" +
            "a_b_c_total{dimension_1=\"dimension_1_value\",dimension_2=\"dimension_2_value\",dimension_3=\"dimension_3_value\"} 4.0\n" +
            "# TYPE a_b_c_d summary\n" +
            "# HELP a_b_c_d Description for a.b.c.d\n" +
            "a_b_c_d_count 5.0\n" +
            "a_b_c_d{quantile=\"0.50\"} 8.0\n" +
            "a_b_c_d{quantile=\"0.75\"} 9.0\n" +
            "# TYPE a_b_c_d_max gauge\n" +
            "# HELP a_b_c_d_max Description for a.b.c.d\n" +
            "a_b_c_d_max 6.0\n" +
            "# TYPE a_b_c_d_mean gauge\n" +
            "# HELP a_b_c_d_mean Description for a.b.c.d\n" +
            "a_b_c_d_mean 7.0\n" +
            "# TYPE a_b_c_d_e summary\n" +
            "# HELP a_b_c_d_e Description for a.b.c.d.e\n" +
            "a_b_c_d_e_count{dimension_1=\"dimension_1_value\",dimension_2=\"dimension_2_value\",dimension_3=\"dimension_3_value\"} 10.0\n" +
            "a_b_c_d_e{dimension_1=\"dimension_1_value\",dimension_2=\"dimension_2_value\",dimension_3=\"dimension_3_value\",quantile=\"0.50\"} 13.0\n" +
            "a_b_c_d_e{dimension_1=\"dimension_1_value\",dimension_2=\"dimension_2_value\",dimension_3=\"dimension_3_value\",quantile=\"0.75\"} 14.0\n" +
            "# TYPE a_b_c_d_e_max gauge\n" +
            "# HELP a_b_c_d_e_max Description for a.b.c.d.e\n" +
            "a_b_c_d_e_max{dimension_1=\"dimension_1_value\",dimension_2=\"dimension_2_value\",dimension_3=\"dimension_3_value\"} 11.0\n" +
            "# TYPE a_b_c_d_e_mean gauge\n" +
            "# HELP a_b_c_d_e_mean Description for a.b.c.d.e\n" +
            "a_b_c_d_e_mean{dimension_1=\"dimension_1_value\",dimension_2=\"dimension_2_value\",dimension_3=\"dimension_3_value\"} 12.0\n" +
            "# EOF\n"));
    }
}