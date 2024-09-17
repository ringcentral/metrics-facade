package com.ringcentral.platform.metrics.reporters.prometheus;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.reporters.MetricsExporter;
import com.ringcentral.platform.metrics.samples.InstanceSamplesProvider;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSample;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSamplesProvider;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusSample;
import com.ringcentral.platform.metrics.utils.StringBuilderWriter;
import io.prometheus.client.exporter.common.TextFormat;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter.Format.PROMETHEUS_TEXT_O_O_4;
import static io.prometheus.client.Collector.MetricFamilySamples;
import static java.lang.String.join;
import static java.util.Collections.emptyList;
import static java.util.Collections.enumeration;
import static java.util.Locale.ENGLISH;
import static java.util.Objects.requireNonNull;
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

        public String contentType() {
            return contentType;
        }
    }

    public static final Format DEFAULT_FORMAT = PROMETHEUS_TEXT_O_O_4;
    public static final String NAME_PARTS_DELIMITER = "_";
    public static final boolean DEFAULT_CONVERT_NAME_TO_LOWER_CASE = false;

    private final Format defaultFormat;
    private final boolean convertNameToLowercase;
    private final PrometheusMetricSanitizer sanitizer;
    private final InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider;

    private static final Logger logger = getLogger(PrometheusMetricsExporter.class);

    public PrometheusMetricsExporter(MetricRegistry metricRegistry) {
        this(
            DEFAULT_FORMAT,
            DEFAULT_CONVERT_NAME_TO_LOWER_CASE,
            new DefaultPrometheusMetricSanitizer(),
            new PrometheusInstanceSamplesProvider(metricRegistry));
    }

    public PrometheusMetricsExporter(
        Format defaultFormat,
        boolean convertNameToLowercase,
        PrometheusMetricSanitizer sanitizer,
        InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider) {

        this.defaultFormat = requireNonNull(defaultFormat);
        this.convertNameToLowercase = convertNameToLowercase;
        this.sanitizer = requireNonNull(sanitizer);
        this.instanceSamplesProvider = requireNonNull(instanceSamplesProvider);
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
        Map<MetricName, MetricFamilySamples> nameToFs = new LinkedHashMap<>();

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
        String name = buildName(is.name());

        return new MetricFamilySamples(
            name,
            is.type(),
            helpFor(is),
            is.samples().stream()
                .map(s -> {
                    String sampleName = name;

                    if (s.hasName()) {
                        sampleName = buildName(s.name());
                    } else if (s.hasNameSuffix()) {
                        sampleName += buildNameSuffix(s);
                    }

                    return new MetricFamilySamples.Sample(
                        sampleName,
                        s.labelNames() != null ? sanitizer.sanitizeLabelNames(s.labelNames()) : emptyList(),
                        s.labelValues() != null ? s.labelValues() : emptyList(),
                        s.value());
                })
                .collect(toList()));
    }

    private String buildName(MetricName name) {
        final var sanitizedName = sanitizer.sanitizeMetricName(join(NAME_PARTS_DELIMITER, name));
        return convertNameToLowercase ? sanitizedName.toLowerCase(ENGLISH) : sanitizedName;
    }

    private String buildNameSuffix(PrometheusSample ps) {
        final var suffix = ps.nameSuffix();
        final var sanitizedSuffix = sanitizer.sanitizeMetricName(suffix);
        return convertNameToLowercase ? sanitizedSuffix.toLowerCase(ENGLISH) : sanitizedSuffix;
    }

    private static String helpFor(PrometheusInstanceSample is) {
        return
            is.hasDescription() ?
            is.description() :
            "Generated from metric instances with name " + is.instanceName();
    }
}