package com.ringcentral.platform.metrics.samples.reporters;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.reporters.jmx.JmxMetricsReporter;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import com.ringcentral.platform.metrics.samples.AbstractSample;
import com.ringcentral.platform.metrics.samples.prometheus.*;
import com.ringcentral.platform.metrics.samples.prometheus.collectorRegistry.SimpleCollectorRegistryPrometheusInstanceSamplesProvider;
import com.ringcentral.platform.metrics.timer.Timer;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.SampleNameFilter;
import io.prometheus.client.Summary;

import java.util.Locale;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.forDimensionValues;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.*;
import static com.ringcentral.platform.metrics.predicates.DefaultMetricInstancePredicate.forMetricInstancesMatching;
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSampleSpec.instanceSampleSpec;
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSamplesProviderBuilder.prometheusInstanceSamplesProvider;
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusSampleSpec.sampleSpec;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;
import static java.util.concurrent.TimeUnit.SECONDS;

@SuppressWarnings("ALL")
public class PrometheusMetricsExporterSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new DefaultMetricRegistry();

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
            (metric, instance, currSpec) -> instanceSampleSpec().disable());

        miSampleSpecModsProvider.addMod(
            forMetricWithName("Histogram"),
            (metric, instance, currSpec) -> instanceSampleSpec()
                .name(instance.name().withNewPart(instance.valueOf(SERVICE)))
                .dimensionValues(currSpec.dimensionValuesWithout(SERVICE)));

        miSampleSpecModsProvider.addMod(
            forMetricsWithNamePrefix("Histogram"),
            (metric, instance, currSpec) ->
                instanceSampleSpec().name(currSpec.name().replaceLast(currSpec.name().lastPart() + "_svc")));

        PrometheusInstanceSampleMaker miSampleMaker = new PrometheusInstanceSampleMaker(
            null, // totalInstanceNameSuffix. defaults to null that means no suffix
            "all"); // dimensionalTotalInstanceNameSuffix. defaults to "all"

        PrometheusSampleSpecProvider sampleSpecProvider = new PrometheusSampleSpecProvider();
        PrometheusSampleSpecModsProvider sampleSpecModsProvider = new PrometheusSampleSpecModsProvider();

        sampleSpecModsProvider.addMod(
            forMetricInstancesMatching(
                nameMask("Histogram.**"),
                instance -> instance instanceof HistogramInstance),
            (instanceSampleSpec, instance, measurableValues, measurable, currSpec) ->
                measurable instanceof Max ? sampleSpec().disable() : sampleSpec());

        PrometheusSamplesProducer samplesProducer = new PrometheusSamplesProducer();

        PrometheusInstanceSamplesProvider miSamplesProvider = prometheusInstanceSamplesProvider(registry)
            .instanceSampleSpecProvider(miSampleSpecProvider)
            .instanceSampleSpecModsProvider(miSampleSpecModsProvider)
            .instanceSampleMaker(miSampleMaker)
            .sampleSpecProvider(sampleSpecProvider)
            .sampleSpecModsProvider(sampleSpecModsProvider)
            .samplesProducer(samplesProducer)
            .build();

        PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(
            true,
            Locale.ENGLISH,
            miSamplesProvider,
            new SimpleCollectorRegistryPrometheusInstanceSamplesProvider(
                name("defaultRegistry"), // optional prefix
                new SampleNameFilter.Builder().nameMustNotStartWith("counter").build(), // optional filter
                sampleName -> !sampleName.endsWith("created"), // optional filter
                CollectorRegistry.defaultRegistry));

        Counter defaultRegistry_Counter = Counter.build()
            .name("counter")
            .help("Counter from defaultRegistry")
            .register();

        defaultRegistry_Counter.inc(3);

        Summary defaultRegistry_Summary = Summary.build()
            .name("summary")
            .help("Summary from defaultRegistry")
            .register();

        defaultRegistry_Summary.observe(10);
        defaultRegistry_Summary.observe(20);
        defaultRegistry_Summary.observe(30);

        Histogram h = registry.histogram(
            withName("Histogram"),
            () -> withHistogram()
                .description("Histogram for " + PrometheusMetricsExporterSample.class.getSimpleName())
                .dimensions(SERVICE, SERVER, PORT)
                .measurables(MIN, MAX, MEAN));

        h.update(1, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
        h.update(2, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
        h.update(3, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));
        h.update(4, forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));

        Timer t = registry.timer(
            withName("Timer"),
            () -> withTimer()
                .description("Timer for " + PrometheusMetricsExporterSample.class.getSimpleName())
                .dimensions(SERVICE, SERVER, PORT)
                .measurables(MIN, MAX, MEAN));

        t.update(SECONDS.toNanos(1), forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
        t.update(SECONDS.toNanos(2), forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));
        t.update(SECONDS.toNanos(3), forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));

        new PrometheusHttpServer(PROMETHEUS_PORT, exporter);
        registry.addListener(new JmxMetricsReporter());
        hang();
    }
}
