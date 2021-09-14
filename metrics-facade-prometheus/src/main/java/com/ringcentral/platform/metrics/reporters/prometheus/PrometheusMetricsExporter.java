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
import static java.lang.String.*;
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
    public static final boolean DEFAULT_LOWER_CASE_NAME = false;
    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    private final Format defaultFormat;
    private final InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider;
    private final boolean convertNameToLowercase;
    private final Locale locale;

    private static final Logger logger = getLogger(PrometheusMetricsExporter.class);

    public PrometheusMetricsExporter(MetricRegistry registry) {
        this(new PrometheusInstanceSamplesProvider(registry));
    }

    public PrometheusMetricsExporter(
        MetricRegistry registry,
        boolean convertNameToLowercase,
        Locale locale) {

        this(
            convertNameToLowercase,
            locale,
            new PrometheusInstanceSamplesProvider(registry));
    }

    public PrometheusMetricsExporter(InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider) {
        this(DEFAULT_FORMAT, instanceSamplesProvider);
    }

    public PrometheusMetricsExporter(
        Format defaultFormat,
        InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider) {

        this(
            defaultFormat,
            DEFAULT_LOWER_CASE_NAME,
            DEFAULT_LOCALE,
            instanceSamplesProvider);
    }

    public PrometheusMetricsExporter(
        Format defaultFormat,
        boolean convertNameToLowercase,
        Locale locale,
        InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider) {

        this.defaultFormat = defaultFormat;
        this.convertNameToLowercase = convertNameToLowercase;
        this.locale = locale != null ? locale : DEFAULT_LOCALE;
        this.instanceSamplesProvider = instanceSamplesProvider;
    }

    @SafeVarargs
    public PrometheusMetricsExporter(InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample>... instanceSamplesProviders) {
        this(
            DEFAULT_FORMAT,
            DEFAULT_LOWER_CASE_NAME,
            DEFAULT_LOCALE,
            instanceSamplesProviders);
    }

    @SafeVarargs
    public PrometheusMetricsExporter(
        boolean convertNameToLowercase,
        Locale locale,
        InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample>... instanceSamplesProviders) {

        this(
            DEFAULT_FORMAT,
            convertNameToLowercase,
            locale,
            instanceSamplesProviders);
    }

    @SafeVarargs
    public PrometheusMetricsExporter(
        Format defaultFormat,
        InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample>... instanceSamplesProviders) {

        this(
            defaultFormat,
            DEFAULT_LOWER_CASE_NAME,
            DEFAULT_LOCALE,
            instanceSamplesProviders);
    }

    @SafeVarargs
    public PrometheusMetricsExporter(
        Format defaultFormat,
        boolean convertNameToLowercase,
        Locale locale,
        InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample>... instanceSamplesProviders) {

        this.defaultFormat = defaultFormat;
        this.convertNameToLowercase = convertNameToLowercase;
        this.locale = locale != null ? locale : DEFAULT_LOCALE;
        this.instanceSamplesProvider = new CompositeInstanceSamplesProvider<>(List.of(instanceSamplesProviders));
    }

    @Override
    public String exportMetrics() {
        return exportMetrics(defaultFormat);
    }

    public String exportMetrics(Format format) {
        StringBuilderWriter writer = new StringBuilderWriter();
        exportMetrics(writer, format);
        return writer.result();
    }

    public void exportMetrics(Writer writer) {
        exportMetrics(writer, defaultFormat);
    }

    public void exportMetrics(Writer writer, Format format) {
        try {
            exportInstanceSamples(writer, format);
        } catch (Exception e) {
            logger.error("Failed to export metrics", e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void exportInstanceSamples(Writer writer, Format format) throws IOException {
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
        String name =
            convertNameToLowercase ?
            sanitizeMetricName(join(NAME_PARTS_DELIMITER, is.name())).toLowerCase(locale) :
            sanitizeMetricName(join(NAME_PARTS_DELIMITER, is.name()));

        return new MetricFamilySamples(
            name,
            is.type(),
            helpFor(is),
            is.samples().stream()
                .map(s -> {
                    String sampleName = name;

                    if (s.hasNameSuffix()) {
                        sampleName +=
                            convertNameToLowercase ?
                            sanitizeMetricName(s.nameSuffix()).toLowerCase(locale) :
                            sanitizeMetricName(s.nameSuffix());
                    }

                    return new MetricFamilySamples.Sample(
                        sampleName,
                        s.labelNames() != null ? s.labelNames() : emptyList(),
                        s.labelValues() != null ? s.labelValues() : emptyList(),
                        s.value());
                })
                .collect(toList()));
    }

    private static String helpFor(PrometheusInstanceSample is) {
        return
            is.hasDescription() ?
            is.description() :
            "Generated from metric instances with name " + is.instanceName();
    }
}