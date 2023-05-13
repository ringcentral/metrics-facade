package com.ringcentral.platform.metrics.articles.intro;

import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import static com.ringcentral.platform.metrics.MetricModBuilder.modifying;
import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.metricsWithNamePrefix;

public class Listing13 {

    public static void main(String[] args) throws InterruptedException {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Reduce the set of Measurables for the third party metrics
        registry.postConfigure(
            metricsWithNamePrefix("third.party"),
            modifying().histogram(withHistogram().measurables(COUNT, TOTAL_SUM, PERCENTILE_99)));

        // 3) Register third party metric
        Histogram thirdPartyHistogram = registry.histogram(
            withName("third", "party", "histogram"),
            () -> withHistogram()
                .description("Third party histogram")
                .measurables(COUNT, TOTAL_SUM, PERCENTILE_50, PERCENTILE_90, PERCENTILE_99, PERCENTILE_999));

        // 4) Register app metric
        Histogram appHistogram = registry.histogram(
            withName("app", "histogram"),
            () -> withHistogram()
                .description("App histogram")
                .measurables(COUNT, TOTAL_SUM, PERCENTILE_50, PERCENTILE_90, PERCENTILE_99));

        // 5) Update histograms
        for (int i = 0; i < 10; ++i) {
            appHistogram.update(i);
            thirdPartyHistogram.update(i);
        }

        // 6) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 7) Export metrics
        System.out.println(exporter.exportMetrics());
    }
}
