package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.reporters.jmx.JmxMetricsReporter;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusHttpServer;
import com.ringcentral.platform.metrics.timer.Stopwatch;
import com.ringcentral.platform.metrics.timer.Timer;

import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.labels.AllLabelValuesPredicate.labelValuesMatchingAll;
import static com.ringcentral.platform.metrics.labels.LabelValues.forLabelValues;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;
import static java.lang.Thread.sleep;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SuppressWarnings("ALL")
public class GettingStartedSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // Create a registry

        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new DefaultMetricRegistry();

        // Add reporters
        PrometheusMetricsExporter prometheusExporter = new PrometheusMetricsExporter(registry);
        new PrometheusHttpServer(PROMETHEUS_PORT, prometheusExporter); // This server is for tests only

        registry.addListener(new JmxMetricsReporter());

        // Add metrics
        Timer httpClientRequestTimer = registry.timer(
            withName("http", "client", "request", "duration"),
            () -> withTimer()
                .labels(SERVICE, SERVER, PORT)
                .exclude(labelValuesMatchingAll(SERVICE.mask("discoveryService")))
                .maxLabeledInstancesPerSlice(100)
                .expireLabeledInstanceAfter(30, SECONDS)
                .allSlice()
                    .enableLevels() // levels for AllSlice are enabled by default
                .slice("by", "server")
                    .predicate(labelValuesMatchingAll(
                        SERVICE.mask("auth*|*throttling*"),
                        PORT.predicate(p -> !p.equals("7004"))))
                    .labels(SERVER)
                    .measurables(MAX, MEAN, PERCENTILE_99));

        Counter activeClientConnectionCounter = registry.counter(withName("active", "client", "connections"));

        // Update metrics
        httpClientRequestTimer.update(
            100L, MILLISECONDS,
            forLabelValues(SERVICE.value("authorizationService"), SERVER.value("127.0.0.1"), PORT.value("7001")));

        httpClientRequestTimer.update(
            200L, MILLISECONDS,
            forLabelValues(SERVICE.value("authorizationService"), SERVER.value("127.0.0.2"), PORT.value("7002")));

        // start a stopwatch before executing the request
        Stopwatch stopwatch = httpClientRequestTimer.stopwatch(forLabelValues(
            SERVICE.value("throttlingService"),
            SERVER.value("127.0.0.3"),
            PORT.value("7003")));

        sleep(300L);

        // stop the stopwatch after executing the request
        stopwatch.stop();

        activeClientConnectionCounter.inc();
        activeClientConnectionCounter.inc();
        activeClientConnectionCounter.inc();
        activeClientConnectionCounter.dec();

        httpClientRequestTimer.update(
            100L, MILLISECONDS,
            forLabelValues(SERVICE.value("discoveryService"), SERVER.value("127.0.0.1"), PORT.value("7001")));

        hang();
    }
}
