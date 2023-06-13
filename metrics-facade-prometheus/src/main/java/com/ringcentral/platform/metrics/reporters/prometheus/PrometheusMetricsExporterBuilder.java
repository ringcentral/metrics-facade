package com.ringcentral.platform.metrics.reporters.prometheus;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.samples.CompositeInstanceSamplesProvider;
import com.ringcentral.platform.metrics.samples.InstanceSamplesProvider;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSample;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSamplesProvider;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusSample;

import java.util.ArrayList;
import java.util.List;

import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSamplesProviderBuilder.prometheusInstanceSamplesProvider;
import static java.util.Objects.requireNonNull;

public class PrometheusMetricsExporterBuilder {

    private PrometheusMetricsExporter.Format defaultFormat = PrometheusMetricsExporter.DEFAULT_FORMAT;
    private boolean convertNameToLowercase = PrometheusMetricsExporter.DEFAULT_CONVERT_NAME_TO_LOWER_CASE;
    private PrometheusMetricSanitizer sanitizer = new DefaultPrometheusMetricSanitizer();
    private final List<InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample>> instanceSamplesProviders = new ArrayList<>();

    public static PrometheusMetricsExporterBuilder prometheusMetricsExporter() {
        return new PrometheusMetricsExporterBuilder();
    }

    public static PrometheusMetricsExporterBuilder prometheusMetricsExporterBuilder() {
        return new PrometheusMetricsExporterBuilder();
    }

    public static PrometheusMetricsExporterBuilder prometheusMetricsExporter(MetricRegistry metricRegistry) {
        PrometheusInstanceSamplesProvider instanceSamplesProvider = prometheusInstanceSamplesProvider(requireNonNull(metricRegistry)).build();
        return prometheusMetricsExporter().addInstanceSamplesProvider(instanceSamplesProvider);
    }

    public PrometheusMetricsExporterBuilder defaultFormat(PrometheusMetricsExporter.Format defaultFormat) {
        this.defaultFormat = requireNonNull(defaultFormat);
        return this;
    }

    public PrometheusMetricsExporterBuilder convertNameToLowercase() {
        return convertNameToLowercase(true);
    }

    public PrometheusMetricsExporterBuilder convertNameToLowercase(boolean convertNameToLowercase) {
        this.convertNameToLowercase = convertNameToLowercase;
        return this;
    }

    public PrometheusMetricsExporterBuilder sanitizer(PrometheusMetricSanitizer sanitizer) {
        this.sanitizer = requireNonNull(sanitizer);
        return this;
    }

    public PrometheusMetricsExporterBuilder addInstanceSamplesProvider(InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider) {
        this.instanceSamplesProviders.add(requireNonNull(instanceSamplesProvider));
        return this;
    }

    public PrometheusMetricsExporter build() {
        if (instanceSamplesProviders.isEmpty()) {
            throw new IllegalStateException("instanceSamplesProviders not configured");
        }

        return new PrometheusMetricsExporter(
            defaultFormat,
            convertNameToLowercase,
            sanitizer,
            instanceSamplesProviders.size() == 1 ? instanceSamplesProviders.get(0) : new CompositeInstanceSamplesProvider<>(instanceSamplesProviders));
    }
}
