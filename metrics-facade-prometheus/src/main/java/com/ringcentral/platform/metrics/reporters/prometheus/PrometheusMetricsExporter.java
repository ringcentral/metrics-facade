package com.ringcentral.platform.metrics.reporters.prometheus;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.reporters.MetricsExporter;
import com.ringcentral.platform.metrics.samples.*;
import com.ringcentral.platform.metrics.samples.prometheus.*;
import com.ringcentral.platform.metrics.utils.StringBuilderWriter;
import io.prometheus.client.exporter.common.TextFormat;
import org.slf4j.Logger;

import java.io.*;
import java.util.*;

import static com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter.Format.PROMETHEUS_TEXT_O_O_4;
import static io.prometheus.client.Collector.*;
import static java.lang.String.join;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class PrometheusMetricsExporter implements MetricsExporter<String> {

    public enum Format {
        PROMETHEUS_TEXT_O_O_4(TextFormat.CONTENT_TYPE_004),
        OPENMETRICS_TEXT_1_0_0(TextFormat.CONTENT_TYPE_OPENMETRICS_100);

        private final String contentType;

        Format(String contentType) {
            this.contentType = contentType;
        }
    }

    public static final Format DEFAULT_FORMAT = PROMETHEUS_TEXT_O_O_4;
    public static final String NAME_PARTS_DELIMITER = "_";

    private final Format format;
    private final InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider;

    private static final Logger logger = getLogger(PrometheusMetricsExporter.class);

    public PrometheusMetricsExporter(MetricRegistry registry) {
        this(DEFAULT_FORMAT, new PrometheusInstanceSamplesProvider(registry));
    }

    public PrometheusMetricsExporter(InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider) {
        this(DEFAULT_FORMAT, instanceSamplesProvider);
    }

    public PrometheusMetricsExporter(
        Format format,
        InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider) {

        this.format = format;
        this.instanceSamplesProvider = instanceSamplesProvider;
    }

    @SafeVarargs
    public PrometheusMetricsExporter(InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample>... instanceSamplesProviders) {
        this(DEFAULT_FORMAT, instanceSamplesProviders);
    }

    @SafeVarargs
    public PrometheusMetricsExporter(
        Format format,
        InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample>... instanceSamplesProviders) {

        this.format = format;
        this.instanceSamplesProvider = new CompositeInstanceSamplesProvider<>(List.of(instanceSamplesProviders));
    }

    @Override
    public String exportMetrics() {
        StringBuilderWriter writer = new StringBuilderWriter();
        exportMetrics(writer);
        return writer.result();
    }

    public void exportMetrics(Writer writer) {
        try {
            exportInstanceSamples(writer);
        } catch (Exception e) {
            logger.error("Failed to export metrics", e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void exportInstanceSamples(Writer writer) throws IOException {
        Map<MetricName, MetricFamilySamples> nameToFs = new HashMap<>();

        instanceSamplesProvider.instanceSamples().forEach(is -> {
            exportInstanceSample(is, nameToFs);

            if (is.hasChildren()) {
                is.children().forEach(child -> exportInstanceSample(child, nameToFs));
            }
        });

        TextFormat.writeFormat(format.contentType, writer, enumeration(nameToFs.values()));
    }

    private void exportInstanceSample(PrometheusInstanceSample is, Map<MetricName, MetricFamilySamples> nameToFs) {
        if (is.samples().isEmpty()) {
            return;
        }

        MetricName name = is.name();
        MetricFamilySamples currFs = nameToFs.get(name);

        // TODO: check the uniqueness of the sanitized name.
        if (currFs != null) {
            if (is.type() != currFs.type) {
                logger.warn(
                    "MetricFamilySamples collision: instance sample name = '{}', existing type = {}, new type = {} (skipped)",
                    name, currFs.type, is.type());

                return;
            }

            List<MetricFamilySamples.Sample> newFsSamples = new ArrayList<>(currFs.samples);
            MetricFamilySamples fs = toMetricFamilySamples(is);
            newFsSamples.addAll(fs.samples);
            nameToFs.put(name, new MetricFamilySamples(fs.name, fs.type, fs.help, newFsSamples));
        } else {
            nameToFs.put(name, toMetricFamilySamples(is));
        }
    }

    private MetricFamilySamples toMetricFamilySamples(PrometheusInstanceSample is) {
        String name = sanitizeMetricName(join(NAME_PARTS_DELIMITER, is.name()));

        return new MetricFamilySamples(
            name,
            is.type(),
            helpFor(is.instanceName()),
            is.samples().stream()
                .map(s -> new MetricFamilySamples.Sample(
                    s.hasNameSuffix() ? name + sanitizeMetricName(s.nameSuffix()) : name,
                    s.labelNames() != null ? s.labelNames() : emptyList(),
                    s.labelValues() != null ? s.labelValues() : emptyList(),
                    s.value()))
                .collect(toList()));
    }

    private static String helpFor(MetricName name) {
        return "Generated from metric instances with name " + name;
    }
}