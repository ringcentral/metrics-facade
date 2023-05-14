package com.ringcentral.platform.metrics.articles.intro;

import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.producers.SystemMetricsProducer;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

public class Listing16 {

    public static void main(String[] args) throws InterruptedException {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Produce system metrics:
        new SystemMetricsProducer().produceMetrics(registry);

        // 3) Create exporter
        PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);

        // 4) Export metrics
        System.out.println(exporter.exportMetrics());
    }
}
