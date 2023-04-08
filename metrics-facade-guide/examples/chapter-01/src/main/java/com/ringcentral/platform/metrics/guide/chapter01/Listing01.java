package com.ringcentral.platform.metrics.guide.chapter01;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

public class Listing01 {

    public static void main(String[] args) {
        System.out.println(run());
    }

    public static String run() {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Register metric
        MetricName name = MetricName.of("request", "total");
        Counter counter = registry.counter(name);

        // 3) Increase counter
        counter.inc();

        // 4) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 5) Export metrics
        var result = new StringBuilder();
        result.append(exporter.exportMetrics());
        return result.toString();
    }
}
