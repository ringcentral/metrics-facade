package com.ringcentral.platform.metrics.articles.intro;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.withCounter;
import static com.ringcentral.platform.metrics.names.MetricName.withName;

public class Listing02 {

    public static void main(String[] args) {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Register metric
        Counter counter = registry.counter(
            withName("requests", "total"),
            () -> withCounter().description("Client request count"));

        // 3) Increase counter
        counter.inc();

        // 4) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 5) Export metrics
        System.out.println(exporter.exportMetrics());
    }
}
