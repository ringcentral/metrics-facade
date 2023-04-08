package com.ringcentral.platform.metrics.samples.guide;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.withCounter;

// Label concept
public class Lesson_2_1 {

    public static void main(String[] args) throws Exception {
        MetricRegistry registry = new DefaultMetricRegistry();

        MetricName name = MetricName.of("request", "total");

        // 1) Create label
        Label codelabel = new Label("code");

        // 2) Add label to metric during its registration
        Counter counter = registry.counter(
                name,
                () ->
                        withCounter()
                                .labels(codelabel)
        );

        // 3) Increase counter with use of label values
        LabelValues labelValuesWith404Status = LabelValues.labelValues(codelabel.value("404"));
        counter.inc(labelValuesWith404Status);

        LabelValues labelValuesWith200Status = LabelValues.labelValues(codelabel.value("200"));
        counter.inc(100, labelValuesWith200Status);

        PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);
        System.out.println(exporter.exportMetrics());

//        # HELP request_total Generated from metric instances with name request.total
//        # TYPE request_total gauge
//        request_total{code="200",} 100.0
//        request_total{code="404",} 1.0
    }

}