package com.ringcentral.platform.metrics.articles.intro;

import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram.Bucket;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import com.ringcentral.platform.metrics.timer.Timer;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.TOTAL_SUM;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Listing04 {

    public static void main(String[] args) {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Register metric.
        Timer timer = registry.timer(withName("request", "duration", "seconds"), () -> withTimer()
            // 3) Specify custom set of measurables
            .measurables(
                COUNT,
                TOTAL_SUM,
                Bucket.of(5, SECONDS),
                Bucket.of(10, SECONDS),
                Bucket.of(15, SECONDS)));

        // 4) Let's assume that there were 3 requests: 5, 10 and 15 seconds long
        timer.update(5, SECONDS);
        timer.update(10, SECONDS);
        timer.update(15, SECONDS);

        // 5) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 6) Export metrics
        System.out.println(exporter.exportMetrics());
    }
}
