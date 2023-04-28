package com.ringcentral.platform.metrics.guide.chapter04;

import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import static com.ringcentral.platform.metrics.MetricModBuilder.modifying;
import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.names.MetricNameMask.allMetrics;

public class Listing01 {

    public static void main(String[] args) {
        System.out.println(run());
    }

    public static String run() {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Override default Histogram's set of Measurables
        registry.preConfigure(allMetrics(),
                modifying()
                        .histogram(
                                withHistogram()
                                        .measurables(COUNT, TOTAL_SUM, PERCENTILE_50, PERCENTILE_90, PERCENTILE_99)
                        )
        );

        // 3) Register metric
        Histogram histogram = registry.histogram(MetricName.of("histogram", "use", "preConfigure", "measurables"));

        // 4) Some action happens
        for (int i = 0; i < 10; i++) {
            histogram.update(i);
        }

        // 6) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 7) Export metrics
        return exporter.exportMetrics();
    }
}
