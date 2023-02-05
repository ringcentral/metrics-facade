package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.counter.CounterInstance;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.rate.RateInstance;
import com.ringcentral.platform.metrics.timer.TimerInstance;
import com.ringcentral.platform.metrics.var.VarInstance;
import com.ringcentral.platform.metrics.var.doubleVar.DoubleVarInstance;
import com.ringcentral.platform.metrics.var.longVar.LongVarInstance;
import io.prometheus.client.Collector;
import org.junit.Test;

import java.util.Set;

import static com.ringcentral.platform.metrics.histogram.Histogram.SEC_1_BUCKET;
import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSampleMaker.DEFAULT_LABELED_METRIC_TOTAL_INSTANCE_NAME_SUFFIX;
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSampleMaker.DEFAULT_TOTAL_INSTANCE_NAME_SUFFIX;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PrometheusInstanceSampleMakerTest {

    PrometheusInstanceSampleMaker maker = new PrometheusInstanceSampleMaker();

    @Test
    public void instanceSampleDisabled() {
        assertNull(maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
            false,
            mock(MetricInstance.class),
            name("a"),
            "Description for " + name("a"),
            emptyList())));
    }

    @Test
    public void noInstance() {
        assertNull(maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
            true,
            null,
            name("a"),
            "Description for " + name("a"),
            emptyList())));
    }

    @Test
    public void noName() {
        assertNull(maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
            true,
            mock(MetricInstance.class),
            null,
            null,
            emptyList())));
    }

    @Test
    public void var() {
        MetricInstance instance = mock(VarInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isLabeledMetricTotalInstance()).thenReturn(false);

        PrometheusInstanceSample expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b"),
            "Description for " + name("a", "b"),
            Collector.Type.GAUGE);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(VarInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isLabeledMetricTotalInstance()).thenReturn(true);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b", "all"),
            "Description for " + name("a", "b"),
            Collector.Type.GAUGE);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(VarInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(false);
        when(instance.isLabeledMetricTotalInstance()).thenReturn(false);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b"),
            "Description for " + name("a", "b"),
            Collector.Type.GAUGE);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);
    }

    @Test
    public void counter() {
        MetricInstance instance = mock(CounterInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isLabeledMetricTotalInstance()).thenReturn(false);

        PrometheusInstanceSample expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b"),
            "Description for " + name("a", "b"),
            Collector.Type.GAUGE);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(VarInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isLabeledMetricTotalInstance()).thenReturn(true);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b", "all"),
            "Description for " + name("a", "b"),
            Collector.Type.GAUGE);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(VarInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(false);
        when(instance.isLabeledMetricTotalInstance()).thenReturn(false);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b"),
            "Description for " + name("a", "b"),
            Collector.Type.GAUGE);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);
    }

    @Test
    public void rate() {
        // rate
        MetricInstance instance = mock(RateInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isLabeledMetricTotalInstance()).thenReturn(false);

        PrometheusInstanceSample expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b", "total"),
            "Description for " + name("a", "b"),
            Collector.Type.COUNTER);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(RateInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isLabeledMetricTotalInstance()).thenReturn(true);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b", "all", "total"),
            "Description for " + name("a", "b"),
            Collector.Type.COUNTER);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(RateInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(false);
        when(instance.isLabeledMetricTotalInstance()).thenReturn(false);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b", "total"),
            "Description for " + name("a", "b"),
            Collector.Type.COUNTER);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);
    }

    @Test
    public void histogram() {
        MetricInstance instance = mock(HistogramInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isLabeledMetricTotalInstance()).thenReturn(false);

        PrometheusInstanceSample expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b"),
            "Description for " + name("a", "b"),
            Collector.Type.SUMMARY);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(HistogramInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isLabeledMetricTotalInstance()).thenReturn(true);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b", "all"),
            "Description for " + name("a", "b"),
            Collector.Type.SUMMARY);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(HistogramInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(false);
        when(instance.isLabeledMetricTotalInstance()).thenReturn(false);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b"),
            "Description for " + name("a", "b"),
            Collector.Type.SUMMARY);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);
    }

    @Test
    public void makingInstanceSample() {
        MetricInstance instance = mock(TimerInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isLabeledMetricTotalInstance()).thenReturn(false);
        when(instance.measurables()).thenReturn(Set.of(SEC_1_BUCKET));
        when(instance.isWithBuckets()).thenReturn(true);

        PrometheusInstanceSample expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b"),
            "Description for " + name("a", "b"),
            Collector.Type.HISTOGRAM);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(TimerInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isLabeledMetricTotalInstance()).thenReturn(true);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b", "all"),
            "Description for " + name("a", "b"),
            Collector.Type.SUMMARY);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(TimerInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(false);
        when(instance.isLabeledMetricTotalInstance()).thenReturn(false);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b"),
            "Description for " + name("a", "b"),
            Collector.Type.SUMMARY);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);
    }

    void check(PrometheusInstanceSample actual, PrometheusInstanceSample expected) {
        assertThat(actual.instanceName(), is(expected.instanceName()));
        assertThat(actual.name(), is(expected.name()));
        assertThat(actual.description(), is(expected.description()));
        assertThat(actual.type(), is(expected.type()));
        assertThat(actual.children(), is(expected.children()));
    }

    @Test
    public void exportNonDecreasingLongVarAsCounter() {
        LongVarInstance instance = mock(LongVarInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isLabeledMetricTotalInstance()).thenReturn(false);
        when(instance.isNonDecreasing()).thenReturn(true);

        PrometheusInstanceSample expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b"),
            "Description for " + name("a", "b"),
            Collector.Type.COUNTER);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        maker = new PrometheusInstanceSampleMaker(
            DEFAULT_TOTAL_INSTANCE_NAME_SUFFIX,
            DEFAULT_LABELED_METRIC_TOTAL_INSTANCE_NAME_SUFFIX,
            false);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b"),
            "Description for " + name("a", "b"),
            Collector.Type.GAUGE);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        maker = new PrometheusInstanceSampleMaker();
        DoubleVarInstance doubleVarInstance = mock(DoubleVarInstance.class);
        when(doubleVarInstance.name()).thenReturn(name("a", "b"));
        when(doubleVarInstance.isTotalInstance()).thenReturn(true);
        when(doubleVarInstance.isLabeledMetricTotalInstance()).thenReturn(false);
        when(doubleVarInstance.isNonDecreasing()).thenReturn(true);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b"),
            "Description for " + name("a", "b"),
            Collector.Type.GAUGE);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                doubleVarInstance,
                name("a", "b"),
                "Description for " + name("a", "b"),
                emptyList())),
            expectedInstanceSample);
    }
}