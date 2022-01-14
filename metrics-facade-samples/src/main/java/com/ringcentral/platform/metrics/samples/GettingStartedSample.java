package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.reporters.jmx.JmxMetricsReporter;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusHttpServer;
import com.ringcentral.platform.metrics.timer.*;
import com.ringcentral.platform.metrics.x.XMetricRegistry;

import static com.ringcentral.platform.metrics.dimensions.AllMetricDimensionValuesPredicate.dimensionValuesMatchingAll;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.forDimensionValues;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
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
        MetricRegistry registry = new XMetricRegistry();

        // Add reporters
        PrometheusMetricsExporter prometheusExporter = new PrometheusMetricsExporter(registry);
        new PrometheusHttpServer(PROMETHEUS_PORT, prometheusExporter); // This server is for tests only

        registry.addListener(new JmxMetricsReporter());

        // Add metrics
        Timer httpClientRequestTimer = registry.timer(
            withName("http", "client", "request", "duration"),
            () -> withTimer()
                .dimensions(SERVICE, SERVER, PORT)
                .exclude(dimensionValuesMatchingAll(SERVICE.mask("discoveryService")))
                .maxDimensionalInstancesPerSlice(100)
                .expireDimensionalInstanceAfter(30, SECONDS)
                .allSlice()
                    .enableLevels() // levels for AllSlice are enabled by default
                .slice("by", "server")
                    .predicate(dimensionValuesMatchingAll(
                        SERVICE.mask("auth*|*throttling*"),
                        PORT.predicate(p -> !p.equals("7004"))))
                    .dimensions(SERVER)
                    .measurables(MAX, MEAN, PERCENTILE_99));

        Counter activeClientConnectionCounter = registry.counter(withName("active", "client", "connections"));

        // Update metrics
        httpClientRequestTimer.update(
            100L, MILLISECONDS,
            forDimensionValues(SERVICE.value("authorizationService"), SERVER.value("127.0.0.1"), PORT.value("7001")));

        httpClientRequestTimer.update(
            200L, MILLISECONDS,
            forDimensionValues(SERVICE.value("authorizationService"), SERVER.value("127.0.0.2"), PORT.value("7002")));

        // start a stopwatch before executing the request
        Stopwatch stopwatch = httpClientRequestTimer.stopwatch(forDimensionValues(
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
            forDimensionValues(SERVICE.value("discoveryService"), SERVER.value("127.0.0.1"), PORT.value("7001")));

        hang();
    }
}
