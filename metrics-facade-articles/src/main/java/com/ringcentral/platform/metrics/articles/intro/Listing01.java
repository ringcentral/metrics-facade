package com.ringcentral.platform.metrics.articles.intro;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import static com.ringcentral.platform.metrics.names.MetricName.withName;

public class Listing01 {

    public static void main(String[] args) {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Register metric
        Counter counter = registry.counter(withName("requests", "total"));

        // 3) Update metric
        counter.inc();

        // 4) Create exporter
        PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);

        // 5) Export metrics
        System.out.println(exporter.exportMetrics());
    }
}
