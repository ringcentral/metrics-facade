package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;

import static com.ringcentral.platform.metrics.MetricModBuilder.modifying;
import static com.ringcentral.platform.metrics.configs.builders.BaseMeterConfigBuilder.withMeter;
import static com.ringcentral.platform.metrics.configs.builders.BaseMetricConfigBuilder.withMetric;
import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.*;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.allMetrics;
import static com.ringcentral.platform.metrics.rate.Rate.ONE_MINUTE_RATE;
import static com.ringcentral.platform.metrics.rate.configs.builders.RateConfigBuilder.withRate;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;
import static java.time.temporal.ChronoUnit.MINUTES;

@SuppressWarnings("ALL")
public class DefaultsSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new DefaultMetricRegistry();

        registry.preConfigure(allMetrics(), modifying()
            .metric(withMetric().prefix(dimensionValues(SAMPLE.value("defaults"))))
            .meter(withMeter()
                .expireDimensionalInstanceAfter(30, MINUTES)
                .allSlice().noLevels())
            .rate(withRate().measurables(COUNT, ONE_MINUTE_RATE))
            .histogram(withHistogram().measurables(COUNT, MAX, MEAN, PERCENTILE_95))
            .timer(withTimer().measurables(COUNT, ONE_MINUTE_RATE, MAX, MEAN, PERCENTILE_95)));

        Histogram h = registry.histogram(
            withName("ActiveHealthChecker", "healthCheck", "attemptCount", "histogram"),
            () -> withHistogram()
                .dimensions(SERVICE, SERVER, PORT)
                .maxDimensionalInstancesPerSlice(5)
                // overrides the default:
                .expireDimensionalInstanceAfter(1, MINUTES));

        h.update(25, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("7001")));
        h.update(25, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("7002")));
        h.update(75, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("7001")));
        h.update(25, forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("8001")));
        h.update(75, forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_2"), PORT.value("8001")));
        h.update(1000, forDimensionValues(SERVICE.value("service_3"), SERVER.value("server_3_1"), PORT.value("9001")));

        export(registry);
        hang();
    }
}
