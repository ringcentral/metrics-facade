package com.ringcentral.platform.metrics.articles.intro;

import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSampleSpecModsProvider;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusSampleSpecModsProvider;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfigBuilder.scale;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.labels.LabelValues.forLabelValues;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.namePrefix;
import static com.ringcentral.platform.metrics.predicates.DefaultMetricInstancePredicate.forMetricInstancesMatching;
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSampleSpec.instanceSampleSpec;
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSamplesProviderBuilder.prometheusInstanceSamplesProvider;
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusSampleSpec.sampleSpec;
import static com.ringcentral.platform.metrics.scale.LinearScaleBuilder.linearScale;
import static java.lang.Thread.sleep;

public class Listing15 {

    public static void main(String[] args) throws InterruptedException {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Define labels
        var service = new Label("service");
        var server = new Label("server");

        // 3) Register metric
        Histogram histogram = registry.histogram(withName("failover", "count", "histogram"), () -> withHistogram()
            .description("Failover count histogram")
            .labels(service, server)
            .measurables(
                COUNT, TOTAL_SUM, MEAN,
                Buckets.of(linearScale().from(0).steps(1, 2).withInf()),
                PERCENTILE_50, PERCENTILE_95)
            // We've changed the Histogram implementation to Scale, which we'll describe in detail in the upcoming article
            .impl(scale().with(linearScale().from(0).steps(1, 2).withInf())));

        // 4) Update metric
        histogram.update(0, forLabelValues(service.value("service-1"), server.value("server-1-1")));
        histogram.update(1, forLabelValues(service.value("service-1"), server.value("server-1-1")));

        histogram.update(1, forLabelValues(service.value("service-1"), server.value("server-1-2")));
        histogram.update(2, forLabelValues(service.value("service-1"), server.value("server-1-2")));

        histogram.update(0, forLabelValues(service.value("service-2"), server.value("server-2-1")));
        histogram.update(2, forLabelValues(service.value("service-2"), server.value("server-2-1")));

        // Metric instances are added asynchronously
        sleep(100);

        // 4) Define exporter

        // 4.1) Modify MetricInstances export. To do this, we need to modify instance sample specs:
        PrometheusInstanceSampleSpecModsProvider instanceSampleSpecModsProvider = new PrometheusInstanceSampleSpecModsProvider();

        instanceSampleSpecModsProvider.addMod(
            forMetricInstancesMatching(namePrefix("failover"), instance -> "service-2".equals(instance.valueOf(service))),
            (metric, instance, currSpec) -> instanceSampleSpec().disable());

        // 4.2) Modify measurables export. To do this, we need to modify sample specs:
        PrometheusSampleSpecModsProvider sampleSpecModsProvider = new PrometheusSampleSpecModsProvider();

        sampleSpecModsProvider.addMod(
            forMetricInstancesMatching(namePrefix("failover"), instance -> instance instanceof HistogramInstance),
            (instanceSampleSpec, instance, measurableValues, measurable, currSpec) ->
                measurable instanceof Histogram.Mean ? sampleSpec().disable() : sampleSpec());

        // 4.3) Create exporter
        PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(prometheusInstanceSamplesProvider(registry)
            .instanceSampleSpecModsProvider(instanceSampleSpecModsProvider)
            .sampleSpecModsProvider(sampleSpecModsProvider)
            .build());

        // 5) Export metrics
        System.out.println(exporter.exportMetrics());
    }
}
