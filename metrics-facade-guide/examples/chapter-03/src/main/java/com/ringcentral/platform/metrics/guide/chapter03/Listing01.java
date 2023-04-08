package com.ringcentral.platform.metrics.guide.chapter03;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.withCounter;

public class Listing01 {

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
        MetricName name = MetricName.of("request", "total");
        Counter counter = registry.counter(name,
                () -> withCounter()
                        .labels(SERVICE, SERVER, PORT));

        // 4) Some action happens
        // auth-server-1:8080 is called once
        counter.inc(
                LabelValues.forLabelValues(
                        SERVICE.value("auth"),
                        SERVER.value("auth-server-1"),
                        PORT.value("8080")
                ));

        // auth-server-2:8080 is called 10 times
        for (int i = 0; i < 10; i++) {
            counter.inc(
                    LabelValues.forLabelValues(
                            SERVICE.value("auth"),
                            SERVER.value("auth-server-2"),
                            PORT.value("8080")
                    ));
        }

        // contact-server-1:8081 is called 3 times
        for (int i = 0; i < 3; i++) {
            counter.inc(
                    LabelValues.forLabelValues(
                            SERVICE.value("contacts"),
                            SERVER.value("contact-server-1"),
                            PORT.value("8081")
                    ));
        }

        // 5) Labeled metric instances are added asynchronously
        Thread.sleep(100);

        // 6) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 7) Export metrics
        var result = new StringBuilder();
        result.append(exporter.exportMetrics());
        return result.toString();
    }
}
