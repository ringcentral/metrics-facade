package com.ringcentral.platform.metrics.articles.intro;

import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import com.ringcentral.platform.metrics.timer.Timer;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.labels.LabelValues.forLabelValues;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Listing05 {

    public static void main(String[] args) throws InterruptedException {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Define labels. Typically, we define labels as constants, but in this example, we deviate from that convention for the sake of brevity
        var service = new Label("service");
        var server = new Label("server");

        // 3) Register metric
        Timer httpClientRequestTimer = registry.timer(withName("http", "client", "request", "duration"), () -> withTimer()
            .labels(service, server)
            // We leave only one measurable for the sake of brevity.
            .measurables(COUNT));

        // 4) Auth service is called once
        httpClientRequestTimer.update(
            1, SECONDS,
            forLabelValues(service.value("auth"), server.value("127.0.0.1")));

        // 5) Contacts service is called 3 times on the server 127.0.0.1
        for (int i = 1; i <= 3; ++i) {
            httpClientRequestTimer.update(
                i, SECONDS,
                forLabelValues(service.value("contacts"), server.value("127.0.0.1")));
        }

        // 6) Contacts service is called 6 times on the server 127.0.0.2
        for (int i = 1; i <= 6; ++i) {
            httpClientRequestTimer.update(
                i, SECONDS,
                forLabelValues(service.value("contacts"), server.value("127.0.0.2")));
        }

        // Labeled metric instances are added asynchronously
        sleep(100);

        // 7) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 8) Export metrics
        System.out.println(exporter.exportMetrics());
    }
}
