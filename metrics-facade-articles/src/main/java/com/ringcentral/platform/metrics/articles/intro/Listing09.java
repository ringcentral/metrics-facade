package com.ringcentral.platform.metrics.articles.intro;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.reporters.telegraf.TelegrafMetricsJsonExporter;
import com.ringcentral.platform.metrics.timer.Stopwatch;
import com.ringcentral.platform.metrics.timer.Timer;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.labels.AllLabelValuesPredicate.labelValuesMatchingAll;
import static com.ringcentral.platform.metrics.labels.LabelValues.forLabelValues;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Listing09 {

    public static void main(String[] args) throws InterruptedException, JsonProcessingException {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Define labels
        var service = new Label("service");
        var server = new Label("server");

        // 3) Register metric
        Timer httpClientRequestTimer = registry.timer(withName("http", "client", "request"), () -> withTimer()
            .labels(service, server)
            .measurables(COUNT, TOTAL_SUM)
            .allSlice().enableLevels() // implements requirement 1. Enabled by default for AllSlice
            .slice("by", "server") // implements requirement 2
                .predicate(labelValuesMatchingAll(service.mask("auth*|*contacts*")))
                .labels(server)
                .measurables(MAX, MEAN, PERCENTILE_99)
                .noTotal()); // disable total instance for this slice

        // 4) Update metric

        // 4.1) (auth, 127.0.0.1). Let's assume that there were 2 requests: 100 and 300 milliseconds long
        httpClientRequestTimer.update(
            100, MILLISECONDS,
            forLabelValues(service.value("auth"), server.value("127.0.0.1")));

        httpClientRequestTimer.update(
            300, MILLISECONDS,
            forLabelValues(service.value("auth"), server.value("127.0.0.1")));

        // 4.2) (auth, 127.0.0.2). Let's assume that there were 2 requests: 200 and 400 milliseconds long
        httpClientRequestTimer.update(
            200, MILLISECONDS,
            forLabelValues(service.value("auth"), server.value("127.0.0.2")));

        httpClientRequestTimer.update(
            400, MILLISECONDS,
            forLabelValues(service.value("auth"), server.value("127.0.0.2")));

        // 4.3) (user-contacts, 127.0.0.3): start a stopwatch before executing the request
        Stopwatch stopwatch = httpClientRequestTimer.stopwatch(forLabelValues(
            service.value("user-contacts"),
            server.value("127.0.0.3")));

        // The (user-contacts, 127.0.0.3) request execution took 600 milliseconds
        sleep(600);

        // Stop the stopwatch after executing the request to (user-contacts, 127.0.0.3)
        stopwatch.stop();

        // Labeled metric instances are added asynchronously
        sleep(100);

        // 5) Create exporter
        var exporter = new TelegrafMetricsJsonExporter(false, registry);

        // 6) Export metrics
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(exporter.exportMetrics()));
    }
}
