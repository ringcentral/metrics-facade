package com.ringcentral.platform.metrics.articles.intro;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfigBuilder.scale;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.labels.LabelValues.forLabelValues;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.scale.LinearScaleBuilder.linearScale;
import static java.lang.Thread.sleep;

public class Listing14 {

    public static void main(String[] args) throws InterruptedException, JsonProcessingException {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Define labels
        var service = new Label("service");

        // 3) Register metric
        Histogram histogram = registry.histogram(withName("failover", "count", "histogram"), () -> withHistogram()
            .description("Failover count histogram")
            .labels(service)
            .measurables(
                COUNT, TOTAL_SUM,
                Buckets.of(linearScale().from(0).steps(1, 2).withInf()),
                PERCENTILE_50)
            // We've changed the Histogram implementation to Scale, which we'll describe in detail in the upcoming article
            .impl(scale().with(linearScale().from(0).steps(1, 2).withInf())));

        // 4) Update metric
        histogram.update(0, forLabelValues(service.value("auth")));
        histogram.update(1, forLabelValues(service.value("auth")));

        histogram.update(1, forLabelValues(service.value("contacts")));
        histogram.update(2, forLabelValues(service.value("contacts")));

        // Metric instances are added asynchronously
        sleep(100);

        // 5) Create exporter
        PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);

        // 5) Export metrics
        System.out.println(exporter.exportMetrics());
    }
}
