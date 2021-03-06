package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.counter.CounterInstance;
import com.ringcentral.platform.metrics.histogram.*;
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
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSampleMaker.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class PrometheusInstanceSampleMakerTest {

    PrometheusInstanceSampleMaker maker = new PrometheusInstanceSampleMaker();

    @Test
    public void makingInstanceSample() {
        assertNull(maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
            false,
            mock(MetricInstance.class),
            name("a"),
            "Description for " + name("a"),
            emptyList())));

        assertNull(maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
            true,
            null,
            name("a"),
            "Description for " + name("a"),
            emptyList())));

        assertNull(maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
            true,
            mock(MetricInstance.class),
            null,
            null,
            emptyList())));

        // var
        MetricInstance instance = mock(VarInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isDimensionalTotalInstance()).thenReturn(false);

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
        when(instance.isDimensionalTotalInstance()).thenReturn(true);

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
        when(instance.isDimensionalTotalInstance()).thenReturn(false);

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

        // counter
        instance = mock(CounterInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isDimensionalTotalInstance()).thenReturn(false);

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

        instance = mock(VarInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isDimensionalTotalInstance()).thenReturn(true);

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
        when(instance.isDimensionalTotalInstance()).thenReturn(false);

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

        // rate
        instance = mock(RateInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isDimensionalTotalInstance()).thenReturn(false);

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

        instance = mock(RateInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isDimensionalTotalInstance()).thenReturn(true);

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
        when(instance.isDimensionalTotalInstance()).thenReturn(false);

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

        // histogram
        instance = mock(HistogramInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isDimensionalTotalInstance()).thenReturn(false);

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

        instance = mock(HistogramInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isDimensionalTotalInstance()).thenReturn(true);

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
        when(instance.isDimensionalTotalInstance()).thenReturn(false);

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

        // timer
        instance = mock(TimerInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isDimensionalTotalInstance()).thenReturn(false);
        when(instance.measurables()).thenReturn(Set.of(SEC_1_BUCKET));

        expectedInstanceSample = new PrometheusInstanceSample(
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
        when(instance.isDimensionalTotalInstance()).thenReturn(true);

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
        when(instance.isDimensionalTotalInstance()).thenReturn(false);

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
        when(instance.isDimensionalTotalInstance()).thenReturn(false);
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
            DEFAULT_DIMENSIONAL_TOTAL_INSTANCE_NAME_SUFFIX,
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
        when(doubleVarInstance.isDimensionalTotalInstance()).thenReturn(false);
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