package com.ringcentral.platform.metrics.articles.intro;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import java.util.concurrent.TimeUnit;

import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.withCounter;
import static com.ringcentral.platform.metrics.labels.LabelValues.forLabelValues;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static java.lang.Thread.sleep;
import static java.time.temporal.ChronoUnit.SECONDS;

public class Listing08 {

    public static void main(String[] args) throws InterruptedException {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Define labels
        var service = new Label("service");

        // 3) Register metric
        Counter counter = registry.counter(withName("requests", "total"), () -> withCounter()
            .labels(service)
            .expireLabeledInstanceAfter(1, SECONDS)
            .allSlice().noLevels());

        // 4) Process requests for 6 different services
        for (int i = 1; i <= 6; ++i) {
            counter.inc(forLabelValues(service.value("service-" + i)));
        }

        // Labeled metric instances are added asynchronously
        sleep(100);

        // 5) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 6) Export metrics before expiration time has passed.
        var output = new StringBuilder();
        output.append("Before expiration time:\n").append(exporter.exportMetrics());

        // 7) Export metrics after expiration time has passed.
        // We have to add 10 seconds because (expireLabeledInstanceAfter + 10) seconds is a period of checking MetricInstances for expiration.
        sleep(TimeUnit.SECONDS.toMillis(1) + TimeUnit.SECONDS.toMillis(10));
        output.append("After expiration time:\n").append(exporter.exportMetrics());

        System.out.println(output);
    }
}
