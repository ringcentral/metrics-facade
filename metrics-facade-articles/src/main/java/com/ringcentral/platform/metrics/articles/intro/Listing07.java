package com.ringcentral.platform.metrics.articles.intro;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.withCounter;
import static com.ringcentral.platform.metrics.labels.LabelValues.forLabelValues;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static java.lang.Thread.sleep;

public class Listing07 {

    public static void main(String[] args) throws InterruptedException {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Define labels
        var service = new Label("service");

        // 3) Register metric
        Counter counter = registry.counter(withName("requests", "total"), () -> withCounter()
            .labels(service)
            .maxLabeledInstancesPerSlice(3)
            .allSlice().noLevels());

        // 4) Process requests for 6 different services
        for (int i = 1; i <= 6; ++i) {
            counter.inc(forLabelValues(service.value("service-" + i)));
            sleep(2);
        }

        // Labeled metric instances are added asynchronously
        sleep(100);

        // 5) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 6) Export metrics
        System.out.println(exporter.exportMetrics());
    }
}
