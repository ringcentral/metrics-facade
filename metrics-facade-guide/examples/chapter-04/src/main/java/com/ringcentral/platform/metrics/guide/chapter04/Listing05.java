package com.ringcentral.platform.metrics.guide.chapter04;

import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import static com.ringcentral.platform.metrics.MetricModBuilder.modifying;
import static com.ringcentral.platform.metrics.configs.builders.BaseMetricConfigBuilder.withMetric;
import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.labels.LabelValues.labelValues;
import static com.ringcentral.platform.metrics.names.MetricNameMask.allMetrics;
import static com.ringcentral.platform.metrics.names.MetricNameMask.metricsWithNamePrefix;

public class Listing05 {

    public static void main(String[] args) {
        System.out.println(run());
    }

    public static String run() {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Reduce amount of Measurables for third party's Metrics
        registry.postConfigure(
                metricsWithNamePrefix("third.party"),
                modifying()
                        .histogram(
                                withHistogram()
                                        .measurables(COUNT, TOTAL_SUM, PERCENTILE_99)
                        )
        );

        // 3) Third party's Metric
        Histogram thirdPartyHistogram = registry.histogram(
                MetricName.of("third", "party", "histogram"),
                () -> withHistogram()
                        .measurables(COUNT, TOTAL_SUM, PERCENTILE_50, PERCENTILE_90, PERCENTILE_99, PERCENTILE_999)
        );

        // 3) App's Metric
        Histogram appHistogram = registry.histogram(MetricName.of("app", "histogram"),
                () -> withHistogram()
                        .measurables(COUNT, TOTAL_SUM, PERCENTILE_50, PERCENTILE_90, PERCENTILE_99));

        // 5) Some action happens
        for (int i = 0; i < 10; i++) {
            appHistogram.update(i);
            thirdPartyHistogram.update(i);
        }

        // 6) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 7) Export metrics
        return exporter.exportMetrics();
    }
}
