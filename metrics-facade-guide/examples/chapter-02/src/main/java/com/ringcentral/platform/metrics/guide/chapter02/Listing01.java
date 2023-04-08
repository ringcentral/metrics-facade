package com.ringcentral.platform.metrics.guide.chapter02;

import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
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
        MetricName name = MetricName.of("request", "duration", "seconds");
        Histogram histogram = registry.histogram(name);

        // 3) Let's pretend that we had 3 requests: 5, 10 and 15 seconds long
        histogram.update(5);
        histogram.update(10);
        histogram.update(15);

        // 4) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 5) Export metrics
        var result = new StringBuilder();
        result.append(exporter.exportMetrics());
        return result.toString();
    }
}
