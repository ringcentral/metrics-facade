package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.timer.Timer;

import static com.ringcentral.platform.metrics.MetricModBuilder.modifying;
import static com.ringcentral.platform.metrics.PrefixLabelValuesMetricKey.withKey;
import static com.ringcentral.platform.metrics.configs.builders.BaseMetricConfigBuilder.withMetric;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.labels.LabelValues.forLabelValues;
import static com.ringcentral.platform.metrics.labels.LabelValues.labelValues;
import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.metricsMatchingNameMask;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;

@SuppressWarnings("ALL")
public class PrefixLabelValuesSample extends AbstractSample {

    public static final Label PREFIX_1 = new Label("prefix_1");
    public static final Label PREFIX_2 = new Label("prefix_2");

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new DefaultMetricRegistry();

        registry.postConfigure(
            metricsMatchingNameMask("ActiveHealthChecker.**"),
            modifying().metric(withMetric().prefix(labelValues(SAMPLE.value("prefixLabelValues")))));

        Histogram h = registry.histogram(
            withName("ActiveHealthChecker", "healthCheck", "attemptCount", "histogram"),
            () -> withHistogram().labels(SERVICE, SERVER, PORT).allSlice().noLevels());

        h.update(25, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("7001")));
        h.update(25, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("7002")));
        h.update(75, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("7001")));
        h.update(25, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("8001")));
        h.update(75, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_2"), PORT.value("8001")));
        h.update(1000, forLabelValues(SERVICE.value("service_3"), SERVER.value("server_3_1"), PORT.value("9001")));

        Timer t = registry.timer(
            withKey(
                name("ActiveHealthChecker", "healthCheck"),
                labelValues(PREFIX_1.value("prefix_1"), PREFIX_2.value("prefix_2"))),
            () -> withTimer().labels(SERVICE, SERVER, PORT).allSlice().noLevels());

        t.update(25, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("7001")));
        t.update(25, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("7002")));
        t.update(75, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("7001")));
        t.update(25, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("8001")));
        t.update(75, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_2"), PORT.value("8001")));
        t.update(1000, forLabelValues(SERVICE.value("service_3"), SERVER.value("server_3_1"), PORT.value("9001")));

        export(registry);
        hang();
    }
}
