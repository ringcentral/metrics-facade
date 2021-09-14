package com.ringcentral.platform.metrics.samples.reporters;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.histogram.*;
import com.ringcentral.platform.metrics.reporters.jmx.JmxMetricsReporter;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import com.ringcentral.platform.metrics.samples.AbstractSample;
import com.ringcentral.platform.metrics.samples.prometheus.*;

import java.util.Locale;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.forDimensionValues;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.*;
import static com.ringcentral.platform.metrics.predicates.DefaultMetricInstancePredicate.forMetricInstancesMatching;
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSampleSpec.instanceSampleSpec;
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusSampleSpec.sampleSpec;

@SuppressWarnings("ALL")
public class PrometheusMetricsExporterSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        MetricRegistry registry = new DropwizardMetricRegistry();

        // Default config
        // PrometheusMetricsExporter prometheusMetricsExporter = new PrometheusMetricsExporter(registry);

        PrometheusInstanceSampleSpecProvider miSampleSpecProvider = new PrometheusInstanceSampleSpecProvider(
            true, // exportTotalInstances. defaults to true
            false, // exportDimensionalTotalInstances. defaults to false
            false); // exportLevelInstances. defaults to true

        PrometheusInstanceSampleSpecModsProvider miSampleSpecModsProvider = new PrometheusInstanceSampleSpecModsProvider();

        miSampleSpecModsProvider.addMod(
            forMetricInstancesMatching(
                nameMask("Histogram.**"),
                instance -> "service_2".equals(instance.valueOf(SERVICE))),
            (metric, instance) -> instanceSampleSpec().disable());

        miSampleSpecModsProvider.addMod(
            forMetricWithName("Histogram"),
            (metric, instance) -> instanceSampleSpec()
                .name(instance.name().withNewPart(instance.valueOf(SERVICE)))
                .dimensionValues(instance.dimensionValuesWithout(SERVICE)));

        PrometheusInstanceSampleMaker miSampleMaker = new PrometheusInstanceSampleMaker(
            null, // totalInstanceNameSuffix. defaults to null that means no suffix
            "all"); // dimensionalTotalInstanceNameSuffix. defaults to "all"

        PrometheusSampleSpecProvider sampleSpecProvider = new PrometheusSampleSpecProvider();
        PrometheusSampleSpecModsProvider sampleSpecModsProvider = new PrometheusSampleSpecModsProvider();

        sampleSpecModsProvider.addMod(
            forMetricInstancesMatching(
                nameMask("Histogram.**"),
                instance -> instance instanceof HistogramInstance),
            (instanceSampleSpec, instance, measurableValues, measurable) ->
                measurable instanceof Max ? sampleSpec().disable() : sampleSpec());

        PrometheusSampleMaker sampleMaker = new PrometheusSampleMaker();

        PrometheusInstanceSamplesProvider miSamplesProvider = new PrometheusInstanceSamplesProvider(
            miSampleSpecProvider,
            miSampleSpecModsProvider,
            miSampleMaker,
            sampleSpecProvider,
            sampleSpecModsProvider,
            sampleMaker,
            registry);

        PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(
            true,
            Locale.ENGLISH,
            miSamplesProvider);

        Histogram h = registry.histogram(
            withName("Histogram"),
            () -> withHistogram()
                .description("Histogram for " + PrometheusMetricsExporterSample.class.getSimpleName())
                .dimensions(SERVICE, SERVER, PORT)
                .measurables(MAX, MEAN));

        h.update(1, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
        h.update(2, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));
        h.update(3, forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));

        new PrometheusHttpServer(PROMETHEUS_PORT, exporter);
        registry.addListener(new JmxMetricsReporter());
        hang();
    }
}
