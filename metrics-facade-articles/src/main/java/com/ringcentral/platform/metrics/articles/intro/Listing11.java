package com.ringcentral.platform.metrics.articles.intro;

import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import static com.ringcentral.platform.metrics.MetricModBuilder.modifying;
import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.allMetrics;

public class Listing11 {

    public static void main(String[] args) throws InterruptedException {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Set up the default set of Measurables for histograms
        registry.preConfigure(
            allMetrics(),
            modifying().histogram(withHistogram().measurables(COUNT, TOTAL_SUM, PERCENTILE_50, PERCENTILE_90, PERCENTILE_99)));

        // 3) Create Histogram with custom Measurables
        Histogram histogram = registry.histogram(
            withName("histogram", "override", "metricTypeDefaultAndPreConfigured", "measurables"),
            () -> withHistogram().measurables(
                COUNT,
                TOTAL_SUM,
                PERCENTILE_10, PERCENTILE_25, PERCENTILE_50, PERCENTILE_90, PERCENTILE_99, PERCENTILE_999));

        // 4) Update histogram
        for (int i = 0; i < 10; i++) {
            histogram.update(i);
        }

        // 5) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 6) Export metrics
        System.out.println(exporter.exportMetrics());
    }
}
