package com.ringcentral.platform.metrics.reporters.prometheus;

import com.ringcentral.platform.metrics.samples.InstanceSamplesProvider;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSample;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusSample;
import com.ringcentral.platform.metrics.samples.prometheus.collectorRegistry.SimpleCollectorRegistryPrometheusInstanceSamplesProvider;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter.Format.OPENMETRICS_TEXT_1_0_0;
import static com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter.Format.PROMETHEUS_TEXT_O_O_4;
import static com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporterBuilder.prometheusMetricsExporter;
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusSamplesProducer.*;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class PrometheusMetricsExporterTest {

    static final String LABEL_1 = "label_1";
    static final String LABEL_2 = "label_2";
    static final String LABEL_3 = "label_3";

    @Test
    public void export_noInstanceSamples() {
        var instanceSamplesProvider = mock(InstanceSamplesProvider.class);
        when(instanceSamplesProvider.instanceSamples()).thenReturn(Set.of());

        var exporter = prometheusMetricsExporter().addInstanceSamplesProvider(instanceSamplesProvider).build();
        String result = exporter.exportMetrics();
        assertThat(result, is(""));
    }

    @Test
    public void export() {
        var instanceSamplesProvider = mock(InstanceSamplesProvider.class);
        LinkedHashSet<PrometheusInstanceSample> instanceSamples = new LinkedHashSet<>();
        when(instanceSamplesProvider.instanceSamples()).thenReturn(instanceSamples);

        // counter
        PrometheusInstanceSample instanceSample = new PrometheusInstanceSample(
            name("counter", "a"),
            name("a"),
            null,
            Collector.Type.GAUGE);

        instanceSample.add(new PrometheusSample(
            COUNT,
            null,
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
            COUNT,
            null,
            null,
            null,
            null,
            List.of(LABEL_1, LABEL_2),
            List.of("label_1_value", "label_2_value"),
            2.0));

        instanceSamples.add(instanceSample);

        // rate
        instanceSample = new PrometheusInstanceSample(
            name("rate", "a", "b"),
            name("a", "b", "total"),
            "Description for " + name("a", "b", "total"),
            Collector.Type.COUNTER);

        instanceSample.add(new PrometheusSample(
            COUNT,
            null,
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
            COUNT,
            null,
            null,
            null,
            null,
            List.of(LABEL_1, LABEL_2, LABEL_3),
            List.of("label_1_value", "label_2_value", "label_3_value"),
            4.0));

        instanceSamples.add(instanceSample);

        // histogram
        instanceSample = new PrometheusInstanceSample(
            name("histogram", "a", "b", "c", "d"),
            name("a", "b", "c", "d"),
            "Description for " + name("a", "b", "c", "d"),
            Collector.Type.SUMMARY);

        instanceSample.add(new PrometheusSample(
            COUNT,
            null,
            null,
            null,
            "_count",
            emptyList(),
            emptyList(),
            5.0));

        instanceSample.add(new PrometheusSample(
            MIN,
            DEFAULT_MIN_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX,
            Collector.Type.GAUGE,
            null,
            null,
            emptyList(),
            emptyList(),
            5.5));

        instanceSample.add(new PrometheusSample(
            MAX,
            DEFAULT_MAX_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX,
            Collector.Type.GAUGE,
            null,
            null,
            emptyList(),
            emptyList(),
            6.0));

        instanceSample.add(new PrometheusSample(
            MEAN,
            DEFAULT_MEAN_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX,
            Collector.Type.GAUGE,
            null,
            null,
            emptyList(),
            emptyList(),
            7.0));

        instanceSample.add(new PrometheusSample(
            PERCENTILE_50,
            null,
            null,
            null,
            null,
            List.of("quantile"),
            List.of("0.50"),
            8.0));

        instanceSample.add(new PrometheusSample(
            PERCENTILE_75,
            null,
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
            Collector.Type.HISTOGRAM);

        instanceSample.add(new PrometheusSample(
            COUNT,
            null,
            null,
            null,
            "_count",
            List.of(LABEL_1, LABEL_2, LABEL_3),
            List.of("label_1_value", "label_2_value", "label_3_value"),
            10.0));

        instanceSample.add(new PrometheusSample(
            MIN,
            DEFAULT_MIN_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX,
            Collector.Type.GAUGE,
            null,
            null,
            List.of(LABEL_1, LABEL_2, LABEL_3),
            List.of("label_1_value", "label_2_value", "label_3_value"),
            10.5));

        instanceSample.add(new PrometheusSample(
            MAX,
            DEFAULT_MAX_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX,
            Collector.Type.GAUGE,
            null,
            null,
            List.of(LABEL_1, LABEL_2, LABEL_3),
            List.of("label_1_value", "label_2_value", "label_3_value"),
            11.0));

        instanceSample.add(new PrometheusSample(
            MEAN,
            DEFAULT_MEAN_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX,
            Collector.Type.GAUGE,
            null,
            null,
            List.of(LABEL_1, LABEL_2, LABEL_3),
            List.of("label_1_value", "label_2_value", "label_3_value"),
            12.0));

        instanceSample.add(new PrometheusSample(
            PERCENTILE_50,
            null,
            null,
            null,
            null,
            List.of(LABEL_1, LABEL_2, LABEL_3, "quantile"),
            List.of("label_1_value", "label_2_value", "label_3_value", "0.50"),
            13.0));

        instanceSample.add(new PrometheusSample(
            PERCENTILE_75,
            null,
            null,
            null,
            null,
            List.of(LABEL_1, LABEL_2, LABEL_3, "quantile"),
            List.of("label_1_value", "label_2_value", "label_3_value", "0.75"),
            14.0));

        instanceSamples.add(instanceSample);
        CollectorRegistry collectorRegistry = new CollectorRegistry(true);

        Counter collectorRegistryCounter = Counter.build()
            .name("counter")
            .labelNames("label_1", "label_2")
            .help("Counter from defaultRegistry")
            .register(collectorRegistry);

        collectorRegistryCounter.labels("label_1_value", "label_2_value").inc(3);

        var exporter = prometheusMetricsExporter()
            .defaultFormat(PROMETHEUS_TEXT_O_O_4)
            .addInstanceSamplesProvider(instanceSamplesProvider)
            .addInstanceSamplesProvider(new SimpleCollectorRegistryPrometheusInstanceSamplesProvider(collectorRegistry))
            .build();

        assertThat(exporter.exportMetrics().replaceAll("(# HELP |# TYPE )?counter_created.*?\n", "").trim(), is(
            "# HELP a Generated from metric instances with name counter.a\n" +
            "# TYPE a gauge\n" +
            "a 1.0\n" +
            "# HELP a_b Description for a.b\n" +
            "# TYPE a_b gauge\n" +
            "a_b{label_1=\"label_1_value\",label_2=\"label_2_value\",} 2.0\n" +
            "# HELP a_b_total Description for a.b.total\n" +
            "# TYPE a_b_total counter\n" +
            "a_b_total 3.0\n" +
            "# HELP a_b_c_total Description for a.b.c.total\n" +
            "# TYPE a_b_c_total counter\n" +
            "a_b_c_total{label_1=\"label_1_value\",label_2=\"label_2_value\",label_3=\"label_3_value\",} 4.0\n" +
            "# HELP a_b_c_d Description for a.b.c.d\n" +
            "# TYPE a_b_c_d summary\n" +
            "a_b_c_d{quantile=\"0.50\",} 8.0\n" +
            "a_b_c_d{quantile=\"0.75\",} 9.0\n" +
            "a_b_c_d_count 5.0\n" +
            "# HELP a_b_c_d_min Description for a.b.c.d\n" +
            "# TYPE a_b_c_d_min gauge\n" +
            "a_b_c_d_min 5.5\n" +
            "# HELP a_b_c_d_max Description for a.b.c.d\n" +
            "# TYPE a_b_c_d_max gauge\n" +
            "a_b_c_d_max 6.0\n" +
            "# HELP a_b_c_d_mean Description for a.b.c.d\n" +
            "# TYPE a_b_c_d_mean gauge\n" +
            "a_b_c_d_mean 7.0\n" +
            "# HELP a_b_c_d_e Description for a.b.c.d.e\n" +
            "# TYPE a_b_c_d_e histogram\n" +
            "a_b_c_d_e{label_1=\"label_1_value\",label_2=\"label_2_value\",label_3=\"label_3_value\",quantile=\"0.50\",} 13.0\n" +
            "a_b_c_d_e{label_1=\"label_1_value\",label_2=\"label_2_value\",label_3=\"label_3_value\",quantile=\"0.75\",} 14.0\n" +
            "a_b_c_d_e_count{label_1=\"label_1_value\",label_2=\"label_2_value\",label_3=\"label_3_value\",} 10.0\n" +
            "# HELP a_b_c_d_e_min Description for a.b.c.d.e\n" +
            "# TYPE a_b_c_d_e_min gauge\n" +
            "a_b_c_d_e_min{label_1=\"label_1_value\",label_2=\"label_2_value\",label_3=\"label_3_value\",} 10.5\n" +
            "# HELP a_b_c_d_e_max Description for a.b.c.d.e\n" +
            "# TYPE a_b_c_d_e_max gauge\n" +
            "a_b_c_d_e_max{label_1=\"label_1_value\",label_2=\"label_2_value\",label_3=\"label_3_value\",} 11.0\n" +
            "# HELP a_b_c_d_e_mean Description for a.b.c.d.e\n" +
            "# TYPE a_b_c_d_e_mean gauge\n" +
            "a_b_c_d_e_mean{label_1=\"label_1_value\",label_2=\"label_2_value\",label_3=\"label_3_value\",} 12.0\n" +
            "# HELP counter_total Counter from defaultRegistry\n" +
            "# TYPE counter_total counter\n" +
            "counter_total{label_1=\"label_1_value\",label_2=\"label_2_value\",} 3.0"));

        exporter = prometheusMetricsExporter()
            .defaultFormat(OPENMETRICS_TEXT_1_0_0)
            .addInstanceSamplesProvider(instanceSamplesProvider)
            .addInstanceSamplesProvider(new SimpleCollectorRegistryPrometheusInstanceSamplesProvider(collectorRegistry))
            .build();

        assertThat(exporter.exportMetrics().replaceAll("(# HELP |# TYPE )?counter_created.*?\n", ""), is(
            "# TYPE a gauge\n" +
            "# HELP a Generated from metric instances with name counter.a\n" +
            "a 1.0\n" +
            "# TYPE a_b gauge\n" +
            "# HELP a_b Description for a.b\n" +
            "a_b{label_1=\"label_1_value\",label_2=\"label_2_value\"} 2.0\n" +
            "# TYPE a_b counter\n" +
            "# HELP a_b Description for a.b.total\n" +
            "a_b_total 3.0\n" +
            "# TYPE a_b_c counter\n" +
            "# HELP a_b_c Description for a.b.c.total\n" +
            "a_b_c_total{label_1=\"label_1_value\",label_2=\"label_2_value\",label_3=\"label_3_value\"} 4.0\n" +
            "# TYPE a_b_c_d summary\n" +
            "# HELP a_b_c_d Description for a.b.c.d\n" +
            "a_b_c_d{quantile=\"0.50\"} 8.0\n" +
            "a_b_c_d{quantile=\"0.75\"} 9.0\n" +
            "a_b_c_d_count 5.0\n" +
            "# TYPE a_b_c_d_min gauge\n" +
            "# HELP a_b_c_d_min Description for a.b.c.d\n" +
            "a_b_c_d_min 5.5\n" +
            "# TYPE a_b_c_d_max gauge\n" +
            "# HELP a_b_c_d_max Description for a.b.c.d\n" +
            "a_b_c_d_max 6.0\n" +
            "# TYPE a_b_c_d_mean gauge\n" +
            "# HELP a_b_c_d_mean Description for a.b.c.d\n" +
            "a_b_c_d_mean 7.0\n" +
            "# TYPE a_b_c_d_e histogram\n" +
            "# HELP a_b_c_d_e Description for a.b.c.d.e\n" +
            "a_b_c_d_e{label_1=\"label_1_value\",label_2=\"label_2_value\",label_3=\"label_3_value\",quantile=\"0.50\"} 13.0\n" +
            "a_b_c_d_e{label_1=\"label_1_value\",label_2=\"label_2_value\",label_3=\"label_3_value\",quantile=\"0.75\"} 14.0\n" +
            "a_b_c_d_e_count{label_1=\"label_1_value\",label_2=\"label_2_value\",label_3=\"label_3_value\"} 10.0\n" +
            "# TYPE a_b_c_d_e_min gauge\n" +
            "# HELP a_b_c_d_e_min Description for a.b.c.d.e\n" +
            "a_b_c_d_e_min{label_1=\"label_1_value\",label_2=\"label_2_value\",label_3=\"label_3_value\"} 10.5\n" +
            "# TYPE a_b_c_d_e_max gauge\n" +
            "# HELP a_b_c_d_e_max Description for a.b.c.d.e\n" +
            "a_b_c_d_e_max{label_1=\"label_1_value\",label_2=\"label_2_value\",label_3=\"label_3_value\"} 11.0\n" +
            "# TYPE a_b_c_d_e_mean gauge\n" +
            "# HELP a_b_c_d_e_mean Description for a.b.c.d.e\n" +
            "a_b_c_d_e_mean{label_1=\"label_1_value\",label_2=\"label_2_value\",label_3=\"label_3_value\"} 12.0\n" +
            "# TYPE counter counter\n" +
            "# HELP counter Counter from defaultRegistry\n" +
            "counter_total{label_1=\"label_1_value\",label_2=\"label_2_value\"} 3.0\n" +
            "# EOF\n"));
    }

    @Test
    public void sanitizing() {
        var instanceSamplesProvider = mock(InstanceSamplesProvider.class);
        LinkedHashSet<PrometheusInstanceSample> instanceSamples = new LinkedHashSet<>();
        when(instanceSamplesProvider.instanceSamples()).thenReturn(instanceSamples);

        PrometheusInstanceSample instanceSample = new PrometheusInstanceSample(
            name("rate", "name"),
            name("name"),
            "Description for " + name("name"),
            Collector.Type.COUNTER);

        instanceSample.add(new PrometheusSample(
            COUNT,
            null,
            null,
            null,
            null,
            List.of(LABEL_1, LABEL_2),
            List.of("label.1.value", "label.2.value"),
            1.0));

        instanceSamples.add(instanceSample);

        CollectorRegistry collectorRegistry = new CollectorRegistry(true);

        Counter collectorRegistryCounter = Counter.build()
            .name("counter")
            .labelNames("label_1", "label_2")
            .help("Counter from defaultRegistry")
            .register(collectorRegistry);

        collectorRegistryCounter.labels("label.1.value", "label.2.value").inc(3);

        var sanitizer = new PrometheusMetricSanitizer() {

            @Override
            public String sanitizeMetricName(String metricName) {
                return metricName.replaceAll("name", "sanitized_name").replaceAll("counter", "sanitized_counter");
            }

            @Override
            public List<String> sanitizeLabelNames(List<String> labelNames) {
                return labelNames.stream().map(labelName -> labelName.replaceAll("label", "sanitized_label")).collect(toList());
            }

            @Override
            public String sanitizeLabelName(String labelName) {
                return labelName.replaceAll("label", "sanitized_label");
            }
        };

        var exporter = prometheusMetricsExporter()
            .defaultFormat(PROMETHEUS_TEXT_O_O_4)
            .sanitizer(sanitizer)
            .addInstanceSamplesProvider(instanceSamplesProvider)
            .addInstanceSamplesProvider(new SimpleCollectorRegistryPrometheusInstanceSamplesProvider(collectorRegistry))
            .build();

        assertThat(exporter.exportMetrics().replaceAll("(# HELP |# TYPE )?sanitized_counter_created.*?\n", "").trim(), is(
        "# HELP sanitized_name_total Description for name\n" +
            "# TYPE sanitized_name_total counter\n" +
            "sanitized_name_total{sanitized_label_1=\"label.1.value\",sanitized_label_2=\"label.2.value\",} 1.0\n" +
            "# HELP sanitized_counter_total Counter from defaultRegistry\n" +
            "# TYPE sanitized_counter_total counter\n" +
            "sanitized_counter_total{sanitized_label_1=\"label.1.value\",sanitized_label_2=\"label.2.value\",} 3.0"));

        assertThat(exporter.exportMetrics(OPENMETRICS_TEXT_1_0_0).replaceAll("(# HELP |# TYPE )?sanitized_counter_created.*?\n", ""), is(
        "# TYPE sanitized_name counter\n" +
            "# HELP sanitized_name Description for name\n" +
            "sanitized_name_total{sanitized_label_1=\"label.1.value\",sanitized_label_2=\"label.2.value\"} 1.0\n" +
            "# TYPE sanitized_counter counter\n" +
            "# HELP sanitized_counter Counter from defaultRegistry\n" +
            "sanitized_counter_total{sanitized_label_1=\"label.1.value\",sanitized_label_2=\"label.2.value\"} 3.0\n" +
            "# EOF\n"));
    }
}