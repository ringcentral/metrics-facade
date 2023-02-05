package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.timer.Timer;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.labels.AllLabelValuesPredicate.labelValuesMatchingAll;
import static com.ringcentral.platform.metrics.labels.LabelValues.forLabelValues;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerInstanceConfigBuilder.timerInstance;
import static java.time.temporal.ChronoUnit.SECONDS;

@SuppressWarnings("ALL")
public class SlicesAndLevelsSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new DefaultMetricRegistry();

        Timer t = registry.timer(
            withName("ActiveHealthChecker", "healthCheck"),
            () -> withTimer()
                .labels(SERVICE, SERVER, PORT)
                .maxLabeledInstancesPerSlice(5)
                .measurables(
                    COUNT, MIN, MAX, MEAN,
                    PERCENTILE_50, PERCENTILE_75, PERCENTILE_90, PERCENTILE_95, PERCENTILE_99)
                .allSlice()
                    .maxLabeledInstances(15)
                    .notExpireLabeledInstances()
                    .total(timerInstance().measurables(COUNT, MEAN, PERCENTILE_95, MAX))
                .slice("byServer")
                    .labels(SERVER)
                    .measurables(COUNT, MEAN, MAX, PERCENTILE_95)
                .slice("server_1_or_2_1", "port_not_7002")
                    .predicate(labelValuesMatchingAll(
                        SERVER.mask("server_1_*|*2_1*"),
                        PORT.predicate(p -> !p.equals("7002"))))
                    .labels(SERVICE, SERVER)
                    .maxLabeledInstances(25)
                    .expireLabeledInstanceAfter(75, SECONDS)
                    .measurables(COUNT, MEAN, MAX, PERCENTILE_95)
                    .enableLevels());

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
