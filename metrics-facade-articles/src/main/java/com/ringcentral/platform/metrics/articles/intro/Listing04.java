package com.ringcentral.platform.metrics.articles.intro;

import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.histogram.Histogram.Bucket;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.TOTAL_SUM;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.names.MetricName.withName;

public class Listing04 {

    public static void main(String[] args) {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Register metric.
        Histogram histogram = registry.histogram(withName("request", "duration", "seconds"), () -> withHistogram()
            // 3) Specify custom set of measurables
            .measurables(
                COUNT,
                TOTAL_SUM,
                Bucket.of(5),
                Bucket.of(10),
                Bucket.of(15)));

        // 4) Let's assume that there were 3 requests: 5, 10 and 15 seconds long
        histogram.update(5);
        histogram.update(10);
        histogram.update(15);

        // 5) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 6) Export metrics
        System.out.println(exporter.exportMetrics());
    }
}
