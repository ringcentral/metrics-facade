package com.ringcentral.platform.metrics.articles.intro;

import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import com.ringcentral.platform.metrics.timer.Timer;

import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Listing03 {

    public static void main(String[] args) {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Register metric
        Timer timer = registry.timer(withName("request", "duration", "seconds"));

        // 3) Update metric. Let's assume that there were 3 requests: 5, 10 and 15 seconds long
        timer.update(5, SECONDS);
        timer.update(10, SECONDS);
        timer.update(15, SECONDS);

        // 4) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 5) Export metrics
        System.out.println(exporter.exportMetrics());
    }
}
