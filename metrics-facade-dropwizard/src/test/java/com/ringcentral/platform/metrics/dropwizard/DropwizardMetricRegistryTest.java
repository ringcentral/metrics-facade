package com.ringcentral.platform.metrics.dropwizard;

import com.codahale.metrics.Meter;
import com.codahale.metrics.*;
import com.ringcentral.platform.metrics.Metric;
import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.dropwizard.counter.DropwizardCounter;
import com.ringcentral.platform.metrics.dropwizard.histogram.DropwizardHistogram;
import com.ringcentral.platform.metrics.dropwizard.rate.DropwizardRate;
import com.ringcentral.platform.metrics.dropwizard.timer.DropwizardTimer;
import com.ringcentral.platform.metrics.dropwizard.var.objectVar.*;
import org.junit.Test;

import java.util.Map;

import static com.ringcentral.platform.metrics.names.MetricName.*;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class DropwizardMetricRegistryTest extends AbstractMetricRegistryTest<DropwizardMetricRegistry> {

    public DropwizardMetricRegistryTest() {
        super(new DropwizardMetricRegistry());
    }

    @Test
    public void addingDropwizardMetricSet() {
        Counter dwCounter_1 = new Counter();
        Meter dwMeter_1 = new Meter();
        Histogram dwHistogram_1 = new Histogram(new ExponentiallyDecayingReservoir());
        Timer dwTimer_1 = new Timer();
        Gauge<Object> objectGauge_1 = () -> 1;
        Gauge<Long> longGauge_1 = () -> 1L;

        CachedGauge<Object> cachedObjectGauge_1 = new CachedGauge<>(1, HOURS) {
            @Override protected Object loadValue() { return 1; }
        };

        CachedGauge<Long> cachedLongGauge_1 = new CachedGauge<>(1, HOURS) {
            @Override protected Long loadValue() { return 1L; }
        };

        Counter dwCounter_1_2 = new Counter();
        Meter dwMeter_1_2 = new Meter();
        Histogram dwHistogram_1_2 = new Histogram(new ExponentiallyDecayingReservoir());
        Timer dwTimer_1_2 = new Timer();
        Gauge<Object> objectGauge_1_2 = () -> 1;
        Gauge<Long> longGauge_1_2 = () -> 1L;

        CachedGauge<Object> cachedObjectGauge_1_2 = new CachedGauge<>(1, HOURS) {
            @Override protected Object loadValue() { return 1; }
        };

        CachedGauge<Long> cachedLongGauge_1_2 = new CachedGauge<>(1, HOURS) {
            @Override protected Long loadValue() { return 1L; }
        };

        MetricSet dwMetricSet = () -> Map.of(
            "counter.1", dwCounter_1,
            "meter.1", dwMeter_1,
            "histogram.1", dwHistogram_1,
            "timer.1", dwTimer_1,
            "objectGauge.1", objectGauge_1,
            "longGauge.1", longGauge_1,
            "cachedObjectGauge.1", cachedObjectGauge_1,
            "cachedLongGauge.1", cachedLongGauge_1,
            "metricSet.1", (MetricSet)() -> Map.of(
                "counter.1.2", dwCounter_1_2,
                "meter.1.2", dwMeter_1_2,
                "histogram.1.2", dwHistogram_1_2,
                "timer.1.2", dwTimer_1_2,
                "objectGauge.1.2", objectGauge_1_2,
                "longGauge.1.2", longGauge_1_2,
                "cachedObjectGauge.1.2", cachedObjectGauge_1_2,
                "cachedLongGauge.1.2", cachedLongGauge_1_2));

        registry.addMetricSet(name("prefix"), dwMetricSet);

        Map<MetricKey, Metric> metrics = registry.metrics();
        assertThat(metrics.size(), is(16));

        assertThat(metrics.get(name("prefix", "counter", "1")), is(instanceOf(DropwizardCounter.class)));
        assertThat(metrics.get(name("prefix", "meter", "1")), is(instanceOf(DropwizardRate.class)));
        assertThat(metrics.get(name("prefix", "histogram", "1")), is(instanceOf(DropwizardHistogram.class)));
        assertThat(metrics.get(name("prefix", "timer", "1")), is(instanceOf(DropwizardTimer.class)));
        assertThat(metrics.get(name("prefix", "objectGauge", "1")), is(instanceOf(DropwizardObjectVar.class)));
        assertThat(metrics.get(name("prefix", "longGauge", "1")), is(instanceOf(DropwizardObjectVar.class)));
        assertThat(metrics.get(name("prefix", "cachedObjectGauge", "1")), is(instanceOf(DropwizardCachingObjectVar.class)));
        assertThat(metrics.get(name("prefix", "cachedLongGauge", "1")), is(instanceOf(DropwizardCachingObjectVar.class)));

        assertThat(metrics.get(name("prefix", "metricSet", "1", "counter", "1", "2")), is(instanceOf(DropwizardCounter.class)));
        assertThat(metrics.get(name("prefix", "metricSet", "1", "meter", "1", "2")), is(instanceOf(DropwizardRate.class)));
        assertThat(metrics.get(name("prefix", "metricSet", "1", "histogram", "1", "2")), is(instanceOf(DropwizardHistogram.class)));
        assertThat(metrics.get(name("prefix", "metricSet", "1", "timer", "1", "2")), is(instanceOf(DropwizardTimer.class)));
        assertThat(metrics.get(name("prefix", "metricSet", "1", "objectGauge", "1", "2")), is(instanceOf(DropwizardObjectVar.class)));
        assertThat(metrics.get(name("prefix", "metricSet", "1", "longGauge", "1", "2")), is(instanceOf(DropwizardObjectVar.class)));
        assertThat(metrics.get(name("prefix", "metricSet", "1", "cachedObjectGauge", "1", "2")), is(instanceOf(DropwizardCachingObjectVar.class)));
        assertThat(metrics.get(name("prefix", "metricSet", "1", "cachedLongGauge", "1", "2")), is(instanceOf(DropwizardCachingObjectVar.class)));
    }
}