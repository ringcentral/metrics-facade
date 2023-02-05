package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.timer.Timer;

import static com.ringcentral.platform.metrics.labels.AnyLabelValuesPredicate.labelValuesMatchingAny;
import static com.ringcentral.platform.metrics.labels.LabelValues.forLabelValues;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;

@SuppressWarnings("ALL")
public class ExclusionsSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new DefaultMetricRegistry();

        Timer t = registry.timer(
            withName("ActiveHealthChecker", "healthCheck"),
            () -> withTimer()
                .labels(SERVICE, SERVER, PORT)
                .exclude(labelValuesMatchingAny(
                    SERVER.mask("server_1_*|*2_1*"),
                    PORT.predicate(p -> p.equals("9001"))))
                .allSlice().noLevels());

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
