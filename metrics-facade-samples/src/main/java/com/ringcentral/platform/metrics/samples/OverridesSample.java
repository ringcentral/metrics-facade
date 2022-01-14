package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.x.XMetricRegistry;

import static com.ringcentral.platform.metrics.MetricModBuilder.modifying;
import static com.ringcentral.platform.metrics.configs.builders.BaseMeterConfigBuilder.withMeter;
import static com.ringcentral.platform.metrics.configs.builders.BaseMetricConfigBuilder.withMetric;
import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.dimensionValues;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.forDimensionValues;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.*;
import static com.ringcentral.platform.metrics.predicates.CompositeMetricNamedPredicateBuilder.metrics;
import static com.ringcentral.platform.metrics.rate.Rate.ONE_MINUTE_RATE;
import static com.ringcentral.platform.metrics.rate.configs.builders.RateConfigBuilder.withRate;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;
import static java.time.temporal.ChronoUnit.MINUTES;

@SuppressWarnings("ALL")
public class OverridesSample extends AbstractSample {

    static final MetricDimension SAMPLE = new MetricDimension("sample");
    static final MetricDimension SERVICE = new MetricDimension("service");
    static final MetricDimension SERVER = new MetricDimension("server");
    static final MetricDimension PORT = new MetricDimension("port");

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new XMetricRegistry();

        registry.postConfigure(allMetrics(), modifying()
            .metric(withMetric().prefix(dimensionValues(SAMPLE.value("overrides"))))
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
                // Will be overriden with { COUNT, MAX, MEAN, PERCENTILE_95 }
                .measurables(
                    COUNT, MIN, MAX, MEAN,
                    PERCENTILE_50, PERCENTILE_75, PERCENTILE_90, PERCENTILE_95, PERCENTILE_99)
                .maxDimensionalInstancesPerSlice(5)
                // Will be overriden with (30, MINUTES)
                .expireDimensionalInstanceAfter(1, MINUTES));

        h.update(25, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("7001")));
        h.update(25, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("7002")));
        h.update(75, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("7001")));
        h.update(25, forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("8001")));
        h.update(75, forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_2"), PORT.value("8001")));
        h.update(1000, forDimensionValues(SERVICE.value("service_3"), SERVER.value("server_3_1"), PORT.value("9001")));

        // You can configure any subset of metrics:
        registry.postConfigure(metricWithName("a.b.c"), modifying().metric(withMetric().disable()));
        registry.postConfigure(metricsWithNamePrefix("a.b"), modifying().histogram(withHistogram().enable()));
        registry.postConfigure(metricsMatchingNameMask("a.**.b"), modifying().histogram(withHistogram().enable()));

        registry.postConfigure(
            metrics()
                .including(metricsMatchingNameMask("a.b.**.d.**")).excluding(metricWithName("a.b.c.d"))
                .including(metricsWithNamePrefix("d.e.f")).excluding(metricsWithNamePrefix("d.e.f.g")),
            modifying().meter(withMeter().disable()));

        assert registry.timer(withName("a", "b", "c", "d")).isEnabled();
        assert !registry.timer(withName("a", "b", "c", "d", "e")).isEnabled(); // disabled
        assert !registry.timer(withName("a", "b", "x", "d")).isEnabled(); // disabled
        assert !registry.timer(withName("d", "e", "f")).isEnabled(); // disabled
        assert registry.timer(withName("d", "e", "f", "g")).isEnabled();
        assert registry.timer(withName("d", "e", "f", "g", "h")).isEnabled();

        export(registry);
        hang();
    }
}
