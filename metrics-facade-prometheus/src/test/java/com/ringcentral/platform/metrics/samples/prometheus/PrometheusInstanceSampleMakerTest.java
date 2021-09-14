package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.counter.CounterInstance;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.rate.RateInstance;
import com.ringcentral.platform.metrics.timer.TimerInstance;
import com.ringcentral.platform.metrics.var.VarInstance;
import io.prometheus.client.Collector;
import org.junit.Test;

import static com.ringcentral.platform.metrics.names.MetricName.name;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class PrometheusInstanceSampleMakerTest {

    @Test
    public void makingInstanceSample() {
        PrometheusInstanceSampleMaker maker = new PrometheusInstanceSampleMaker();

        assertNull(maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
            false,
            mock(MetricInstance.class),
            name("a"),
            emptyList())));

        assertNull(maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
            true,
            null,
            name("a"),
            emptyList())));

        assertNull(maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
            true,
            mock(MetricInstance.class),
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
            Collector.Type.GAUGE);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(VarInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isDimensionalTotalInstance()).thenReturn(true);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b", "all"),
            Collector.Type.GAUGE);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(VarInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(false);
        when(instance.isDimensionalTotalInstance()).thenReturn(false);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b"),
            Collector.Type.GAUGE);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
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
            Collector.Type.GAUGE);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(VarInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isDimensionalTotalInstance()).thenReturn(true);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b", "all"),
            Collector.Type.GAUGE);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(VarInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(false);
        when(instance.isDimensionalTotalInstance()).thenReturn(false);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b"),
            Collector.Type.GAUGE);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
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
            Collector.Type.COUNTER);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(RateInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isDimensionalTotalInstance()).thenReturn(true);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b", "all", "total"),
            Collector.Type.COUNTER);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(RateInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(false);
        when(instance.isDimensionalTotalInstance()).thenReturn(false);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b", "total"),
            Collector.Type.COUNTER);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
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
            Collector.Type.SUMMARY);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(HistogramInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isDimensionalTotalInstance()).thenReturn(true);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b", "all"),
            Collector.Type.SUMMARY);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(HistogramInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(false);
        when(instance.isDimensionalTotalInstance()).thenReturn(false);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b"),
            Collector.Type.SUMMARY);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        // timer
        instance = mock(TimerInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isDimensionalTotalInstance()).thenReturn(false);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b"),
            Collector.Type.SUMMARY);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(TimerInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(true);
        when(instance.isDimensionalTotalInstance()).thenReturn(true);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b", "all"),
            Collector.Type.SUMMARY);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                emptyList())),
            expectedInstanceSample);

        instance = mock(TimerInstance.class);
        when(instance.name()).thenReturn(name("a", "b"));
        when(instance.isTotalInstance()).thenReturn(false);
        when(instance.isDimensionalTotalInstance()).thenReturn(false);

        expectedInstanceSample = new PrometheusInstanceSample(
            name("a", "b"),
            name("a", "b"),
            Collector.Type.SUMMARY);

        check(
            maker.makeInstanceSample(new PrometheusInstanceSampleSpec(
                true,
                instance,
                name("a", "b"),
                emptyList())),
            expectedInstanceSample);
    }

    public void check(PrometheusInstanceSample actual, PrometheusInstanceSample expected) {
        assertThat(actual.instanceName(), is(expected.instanceName()));
        assertThat(actual.name(), is(expected.name()));
        assertThat(actual.type(), is(expected.type()));
        assertThat(actual.children(), is(expected.children()));
    }
}