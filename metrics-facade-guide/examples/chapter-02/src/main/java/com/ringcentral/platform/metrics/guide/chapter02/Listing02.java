package com.ringcentral.platform.metrics.guide.chapter02;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;

public class Listing02 {

    public static void main(String[] args) {
        System.out.println(run());
    }

    public static String run() {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Register metric
        MetricName name = MetricName.of("request", "duration", "seconds");
        Histogram histogram = registry.histogram(name, () ->
                withHistogram()
                        // 3) Specify custom set of measurables
                        .measurables(
                                Counter.COUNT,
                                Histogram.TOTAL_SUM,
                                Histogram.Bucket.of(5),
                                Histogram.Bucket.of(10),
                                Histogram.Bucket.of(15)
                        )
        );

        // 4) Let's pretend that we had 3 requests: 5, 10 and 15 seconds long
        histogram.update(5);
        histogram.update(10);
        histogram.update(15);

        // 5) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 6) Export metrics
        var result = new StringBuilder();
        result.append(exporter.exportMetrics());
        return result.toString();
    }
}
