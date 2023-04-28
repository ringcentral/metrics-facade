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

public class Listing04 {

    public static void main(String[] args) {
        System.out.println(run());
    }

    public static String run() {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Create prefix label
        var prefixLabel = new Label("prefix");
        LabelValues defaultLabels = labelValues(prefixLabel.value("prefix-value"));

        // 3) Configure label prefix for all metrics
        registry.preConfigure(allMetrics(),
                modifying()
                        .metric(withMetric().prefix(defaultLabels))
                        .histogram(
                                withHistogram()
                                        .measurables(COUNT, TOTAL_SUM, PERCENTILE_50, PERCENTILE_90, PERCENTILE_99))
        );

        // 4) Create Histogram with custom set of Measurables
        Histogram histogram = registry.histogram(MetricName.of("histogram", "metric", "example"));

        // 5) Some action happens
        for (int i = 0; i < 10; i++) {
            histogram.update(i);
        }

        // 6) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 7) Export metrics
        return exporter.exportMetrics();
    }
}
