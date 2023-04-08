package com.ringcentral.platform.metrics.guide.chapter03;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import java.time.Duration;

import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.withCounter;

public class Listing04 {

    public static void main(String[] args) throws InterruptedException {
        System.out.println(run());
    }

    public static String run() throws InterruptedException {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Create labels
        var SERVICE = new Label("service");
        var SERVER = new Label("server");
        var PORT = new Label("port");

        // 3) Register metric
        var expirationTime = Duration.ofMillis(500);
        MetricName name = MetricName.of("request", "total");
        Counter counter = registry.counter(name,
                () -> withCounter()
                        .labels(SERVICE, SERVER, PORT)
                        .expireLabeledInstanceAfter(expirationTime)
        );

        // 4) Some action happens
        for (int i = 1; i < 6; i++) {
            counter.inc(
                    LabelValues.forLabelValues(
                            SERVICE.value("auth"),
                            SERVER.value("auth-server-" + i),
                            PORT.value("8080")
                    ));
        }

        // 5) Labeled metric instances are added asynchronously
        Thread.sleep(expirationTime.dividedBy(2).toMillis());

        // 6) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 7) Export metrics
        var result = new StringBuilder();
        result.append("Before expiration time\n")
                .append(exporter.exportMetrics());

        // 8) Export metrics after expiration time passed
        Thread.sleep(expirationTime.multipliedBy(2).toMillis());
        result.append("After expiration time\n")
                .append(exporter.exportMetrics());

        return result.toString();
    }
}
