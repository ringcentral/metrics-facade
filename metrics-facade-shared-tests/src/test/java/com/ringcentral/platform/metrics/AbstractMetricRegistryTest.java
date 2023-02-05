package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.timer.Timer;
import com.ringcentral.platform.metrics.var.doubleVar.CachingDoubleVar;
import com.ringcentral.platform.metrics.var.doubleVar.DoubleVar;
import com.ringcentral.platform.metrics.var.longVar.CachingLongVar;
import com.ringcentral.platform.metrics.var.longVar.LongVar;
import com.ringcentral.platform.metrics.var.objectVar.CachingObjectVar;
import com.ringcentral.platform.metrics.var.objectVar.ObjectVar;
import com.ringcentral.platform.metrics.var.stringVar.CachingStringVar;
import com.ringcentral.platform.metrics.var.stringVar.StringVar;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.MetricModBuilder.modifying;
import static com.ringcentral.platform.metrics.PrefixLabelValuesMetricKey.withKey;
import static com.ringcentral.platform.metrics.configs.builders.BaseMeterConfigBuilder.withMeter;
import static com.ringcentral.platform.metrics.configs.builders.BaseMetricConfigBuilder.withMetric;
import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.counterConfigBuilder;
import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.withCounter;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.histogramConfigBuilder;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.labels.LabelValues.labelValues;
import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.metricWithName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.metricsWithNamePrefix;
import static com.ringcentral.platform.metrics.rate.configs.builders.RateConfigBuilder.rateConfigBuilder;
import static com.ringcentral.platform.metrics.rate.configs.builders.RateConfigBuilder.withRate;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.timerConfigBuilder;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;
import static com.ringcentral.platform.metrics.var.configs.builders.BaseCachingVarConfigBuilder.withCachingVar;
import static com.ringcentral.platform.metrics.var.configs.builders.BaseVarConfigBuilder.withVar;
import static com.ringcentral.platform.metrics.var.doubleVar.configs.builders.CachingDoubleVarConfigBuilder.cachingDoubleVarConfigBuilder;
import static com.ringcentral.platform.metrics.var.doubleVar.configs.builders.CachingDoubleVarConfigBuilder.withCachingDoubleVar;
import static com.ringcentral.platform.metrics.var.doubleVar.configs.builders.DoubleVarConfigBuilder.doubleVarConfigBuilder;
import static com.ringcentral.platform.metrics.var.doubleVar.configs.builders.DoubleVarConfigBuilder.withDoubleVar;
import static com.ringcentral.platform.metrics.var.longVar.configs.builders.CachingLongVarConfigBuilder.cachingLongVarConfigBuilder;
import static com.ringcentral.platform.metrics.var.longVar.configs.builders.CachingLongVarConfigBuilder.withCachingLongVar;
import static com.ringcentral.platform.metrics.var.longVar.configs.builders.LongVarConfigBuilder.longVarConfigBuilder;
import static com.ringcentral.platform.metrics.var.longVar.configs.builders.LongVarConfigBuilder.withLongVar;
import static com.ringcentral.platform.metrics.var.objectVar.configs.builders.CachingObjectVarConfigBuilder.cachingObjectVarConfigBuilder;
import static com.ringcentral.platform.metrics.var.objectVar.configs.builders.CachingObjectVarConfigBuilder.withCachingObjectVar;
import static com.ringcentral.platform.metrics.var.objectVar.configs.builders.ObjectVarConfigBuilder.objectVarConfigBuilder;
import static com.ringcentral.platform.metrics.var.objectVar.configs.builders.ObjectVarConfigBuilder.withObjectVar;
import static com.ringcentral.platform.metrics.var.stringVar.configs.builders.CachingStringVarConfigBuilder.cachingStringVarConfigBuilder;
import static com.ringcentral.platform.metrics.var.stringVar.configs.builders.CachingStringVarConfigBuilder.withCachingStringVar;
import static com.ringcentral.platform.metrics.var.stringVar.configs.builders.StringVarConfigBuilder.stringVarConfigBuilder;
import static com.ringcentral.platform.metrics.var.stringVar.configs.builders.StringVarConfigBuilder.withStringVar;
import static junit.framework.TestCase.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public abstract class AbstractMetricRegistryTest<R extends MetricRegistry> {

    static final Label LABEL_1 = new Label("label_1");
    static final Label LABEL_2 = new Label("label_2");
    static final Label LABEL_3 = new Label("label_3");
    static final Label LABEL_4 = new Label("label_4");
    static final Label LABEL_5 = new Label("label_5");

    protected R registry;
    MetricRegistryListener listener = mock(MetricRegistryListener.class);

    public AbstractMetricRegistryTest(R registry) {
        this.registry = registry;
    }

    @Before
    public void before() {
        registry.addListener(listener);
    }

    /* Object var */

    @Test
    public void objectVar() {
        ObjectVar objectVar_1 = registry.objectVar(
            withName("objectVar"),
            () -> 1,
            () -> withObjectVar().disable());

        assertFalse(objectVar_1.isEnabled());
        verify(listener).objectVarAdded(objectVar_1);
        verifyNoMoreInteractions(listener);

        ObjectVar objectVar_2 = registry.objectVar(withName("objectVar"), () -> 2);

        assertFalse(objectVar_2.isEnabled());
        assertSame(objectVar_2, objectVar_1);
        verifyNoMoreInteractions(listener);

        ObjectVar objectVar_3 = registry.newObjectVar(withName("objectVar"), () -> 3);

        assertTrue(objectVar_3.isEnabled());
        assertNotSame(objectVar_3, objectVar_2);
        verify(listener).objectVarAdded(objectVar_1);
        verify(listener).objectVarRemoved(objectVar_1);
        verify(listener).objectVarAdded(objectVar_3);
        verifyNoMoreInteractions(listener);

        ObjectVar objectVar_4 = registry.newObjectVar(
            withName("objectVar"),
            () -> 4,
            () -> objectVarConfigBuilder().disable());

        assertFalse(objectVar_4.isEnabled());
        assertNotSame(objectVar_4, objectVar_3);
        verify(listener).objectVarAdded(objectVar_1);
        verify(listener).objectVarRemoved(objectVar_1);
        verify(listener).objectVarAdded(objectVar_3);
        verify(listener).objectVarRemoved(objectVar_3);
        verify(listener).objectVarAdded(objectVar_4);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void objectVar_PrefixLabelValuesKey() {
        ObjectVar objectVar = registry.objectVar(
            withKey(name("objectVar"), labelValues(LABEL_1.value("1"))),
            () -> 1,
            () -> withObjectVar().prefix(labelValues(LABEL_2.value("2"))));

        assertThat(objectVar.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));

        objectVar = registry.newObjectVar(
            withKey(name("objectVar"), labelValues(LABEL_1.value("1"))),
            () -> 1);

        assertThat(objectVar.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));
    }

    @Test
    public void objectVar_PreConfig_And_PostConfig() {
        // pre
        registry.preConfigure(
            metricWithName("objectVar"),
            modifying().variable(withVar().prefix(labelValues(LABEL_1.value("1")))));

        registry.preConfigure(
            metricWithName("objectVar"),
            modifying().variable(withVar().disable().prefix(labelValues(LABEL_2.value("2")))));

        ObjectVar objectVar = registry.objectVar(withName("objectVar"), () -> 1);
        assertFalse(objectVar.isEnabled());

        registry.preConfigure(
            metricWithName("objectVar"),
            modifying().metric(withMetric().enable()));

        objectVar = registry.objectVar(withName("objectVar"), () -> 1);
        assertFalse(objectVar.isEnabled());

        objectVar = registry.newObjectVar(withName("objectVar"), () -> 1);

        assertTrue(objectVar.isEnabled());
        assertThat(objectVar.iterator().next().labelValues(), is(List.of(LABEL_2.value("2"))));

        // and post
        registry.postConfigure(
            metricsWithNamePrefix("objectVar"),
            modifying().variable(withVar().prefix(labelValues(LABEL_3.value("3")))));

        registry.postConfigure(
            metricsWithNamePrefix("objectVar"),
            modifying().variable(withVar().disable().prefix(labelValues(LABEL_4.value("4")))));

        objectVar = registry.newObjectVar(withName("objectVar"), () -> 1);
        assertFalse(objectVar.isEnabled());

        registry.postConfigure(
            metricsWithNamePrefix("objectVar"),
            modifying().metric(withMetric().enable()));

        objectVar = registry.newObjectVar(withName("objectVar"), () -> 1);

        assertTrue(objectVar.isEnabled());
        assertThat(objectVar.iterator().next().labelValues(), is(List.of(LABEL_4.value("4"))));
    }

    /* Caching object var */

    @Test
    public void cachingObjectVar() {
        CachingObjectVar cachingObjectVar_1 = registry.cachingObjectVar(
            withName("cachingObjectVar"),
            () -> 1,
            () -> withCachingObjectVar().disable());

        assertFalse(cachingObjectVar_1.isEnabled());
        verify(listener).cachingObjectVarAdded(cachingObjectVar_1);
        verifyNoMoreInteractions(listener);

        CachingObjectVar cachingObjectVar_2 = registry.cachingObjectVar(
            withName("cachingObjectVar"),
            () -> 2);

        assertFalse(cachingObjectVar_2.isEnabled());
        assertSame(cachingObjectVar_2, cachingObjectVar_1);
        verifyNoMoreInteractions(listener);

        CachingObjectVar cachingObjectVar_3 = registry.newCachingObjectVar(
            withName("cachingObjectVar"),
            () -> 3);

        assertTrue(cachingObjectVar_3.isEnabled());
        assertNotSame(cachingObjectVar_3, cachingObjectVar_2);
        verify(listener).cachingObjectVarAdded(cachingObjectVar_1);
        verify(listener).cachingObjectVarRemoved(cachingObjectVar_1);
        verify(listener).cachingObjectVarAdded(cachingObjectVar_3);
        verifyNoMoreInteractions(listener);

        CachingObjectVar cachingObjectVar_4 = registry.newCachingObjectVar(
            withName("cachingObjectVar"),
            () -> 4,
            () -> cachingObjectVarConfigBuilder().disable());

        assertFalse(cachingObjectVar_4.isEnabled());
        assertNotSame(cachingObjectVar_4, cachingObjectVar_3);
        verify(listener).cachingObjectVarAdded(cachingObjectVar_1);
        verify(listener).cachingObjectVarRemoved(cachingObjectVar_1);
        verify(listener).cachingObjectVarAdded(cachingObjectVar_3);
        verify(listener).cachingObjectVarRemoved(cachingObjectVar_3);
        verify(listener).cachingObjectVarAdded(cachingObjectVar_4);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void cachingObjectVar_PrefixLabelValuesKey() {
        CachingObjectVar cachingObjectVar = registry.cachingObjectVar(
            withKey(name("cachingObjectVar"), labelValues(LABEL_1.value("1"))),
            () -> 1,
            () -> withCachingObjectVar().prefix(labelValues(LABEL_2.value("2"))));

        assertThat(cachingObjectVar.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));

        cachingObjectVar = registry.newCachingObjectVar(
            withKey(name("cachingObjectVar"), labelValues(LABEL_1.value("1"))),
            () -> 1);

        assertThat(cachingObjectVar.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));
    }

    @Test
    public void cachingObjectVar_PreConfig_And_PostConfig() {
        // pre
        registry.preConfigure(
            metricWithName("cachingObjectVar"),
            modifying().cachingVar(withCachingVar().prefix(labelValues(LABEL_1.value("1")))));

        registry.preConfigure(
            metricWithName("cachingObjectVar"),
            modifying().cachingVar(withCachingVar().prefix(labelValues(LABEL_2.value("2")))));

        CachingObjectVar cachingObjectVar = registry.cachingObjectVar(withName("cachingObjectVar"), () -> 1);
        assertThat(cachingObjectVar.iterator().next().labelValues(), is(List.of(LABEL_2.value("2"))));

        registry.preConfigure(
            metricWithName("cachingObjectVar"),
            modifying().variable(withVar().disable().prefix(labelValues(LABEL_3.value("3")))));

        cachingObjectVar = registry.newCachingObjectVar(withName("cachingObjectVar"), () -> 1);
        assertFalse(cachingObjectVar.isEnabled());

        registry.preConfigure(
            metricWithName("cachingObjectVar"),
            modifying().metric(withMetric().enable()));

        cachingObjectVar = registry.cachingObjectVar(withName("cachingObjectVar"), () -> 1);
        assertFalse(cachingObjectVar.isEnabled());

        cachingObjectVar = registry.newCachingObjectVar(withName("cachingObjectVar"), () -> 1);
        assertTrue(cachingObjectVar.isEnabled());
        assertThat(cachingObjectVar.iterator().next().labelValues(), is(List.of(LABEL_3.value("3"))));

        // and post
        registry.postConfigure(
            metricsWithNamePrefix("cachingObjectVar"),
            modifying().cachingVar(withCachingVar().prefix(labelValues(LABEL_4.value("4")))));

        cachingObjectVar = registry.newCachingObjectVar(withName("cachingObjectVar"), () -> 1);
        assertThat(cachingObjectVar.iterator().next().labelValues(), is(List.of(LABEL_4.value("4"))));

        registry.postConfigure(
            metricsWithNamePrefix("cachingObjectVar"),
            modifying().variable(withVar().disable().prefix(labelValues(LABEL_5.value("5")))));

        cachingObjectVar = registry.newCachingObjectVar(withName("cachingObjectVar"), () -> 1);
        assertFalse(cachingObjectVar.isEnabled());

        registry.postConfigure(
            metricsWithNamePrefix("cachingObjectVar"),
            modifying().metric(withMetric().enable()));

        cachingObjectVar = registry.newCachingObjectVar(withName("cachingObjectVar"), () -> 1);
        assertThat(cachingObjectVar.iterator().next().labelValues(), is(List.of(LABEL_5.value("5"))));
    }

    /* Long var */

    @Test
    public void longVar() {
        LongVar longVar_1 = registry.longVar(
            withName("longVar"),
            () -> 1L,
            () -> withLongVar().disable());

        assertFalse(longVar_1.isEnabled());
        verify(listener).longVarAdded(longVar_1);
        verifyNoMoreInteractions(listener);

        LongVar longVar_2 = registry.longVar(withName("longVar"), () -> 2L);

        assertFalse(longVar_2.isEnabled());
        assertSame(longVar_2, longVar_1);
        verifyNoMoreInteractions(listener);

        LongVar longVar_3 = registry.newLongVar(withName("longVar"), () -> 3L);

        assertTrue(longVar_3.isEnabled());
        assertNotSame(longVar_3, longVar_2);
        verify(listener).longVarAdded(longVar_1);
        verify(listener).longVarRemoved(longVar_1);
        verify(listener).longVarAdded(longVar_3);
        verifyNoMoreInteractions(listener);

        LongVar longVar_4 = registry.newLongVar(
            withName("longVar"),
            () -> 4L,
            () -> longVarConfigBuilder().disable());

        assertFalse(longVar_4.isEnabled());
        assertNotSame(longVar_4, longVar_3);
        verify(listener).longVarAdded(longVar_1);
        verify(listener).longVarRemoved(longVar_1);
        verify(listener).longVarAdded(longVar_3);
        verify(listener).longVarRemoved(longVar_3);
        verify(listener).longVarAdded(longVar_4);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void longVar_PrefixLabelValuesKey() {
        LongVar longVar = registry.longVar(
            withKey(name("longVar"), labelValues(LABEL_1.value("1"))),
            () -> 1L,
            () -> withLongVar().prefix(labelValues(LABEL_2.value("2"))));

        assertThat(longVar.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));

        longVar = registry.newLongVar(
            withKey(name("longVar"), labelValues(LABEL_1.value("1"))),
            () -> 1L);

        assertThat(longVar.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));
    }

    @Test
    public void longVar_PreConfig_And_PostConfig() {
        // pre
        registry.preConfigure(
            metricWithName("longVar"),
            modifying().variable(withVar().prefix(labelValues(LABEL_1.value("1")))));

        registry.preConfigure(
            metricWithName("longVar"),
            modifying().variable(withVar().disable().prefix(labelValues(LABEL_2.value("2")))));

        LongVar longVar = registry.longVar(withName("longVar"), () -> 1L);
        assertFalse(longVar.isEnabled());

        registry.preConfigure(
            metricWithName("longVar"),
            modifying().metric(withMetric().enable()));

        longVar = registry.longVar(withName("longVar"), () -> 1L);
        assertFalse(longVar.isEnabled());

        longVar = registry.newLongVar(withName("longVar"), () -> 1L);

        assertTrue(longVar.isEnabled());
        assertThat(longVar.iterator().next().labelValues(), is(List.of(LABEL_2.value("2"))));

        // and post
        registry.postConfigure(
            metricsWithNamePrefix("longVar"),
            modifying().variable(withVar().prefix(labelValues(LABEL_3.value("3")))));

        registry.postConfigure(
            metricsWithNamePrefix("longVar"),
            modifying().variable(withVar().disable().prefix(labelValues(LABEL_4.value("4")))));

        longVar = registry.newLongVar(withName("longVar"), () -> 1L);
        assertFalse(longVar.isEnabled());

        registry.postConfigure(
            metricsWithNamePrefix("longVar"),
            modifying().metric(withMetric().enable()));

        longVar = registry.newLongVar(withName("longVar"), () -> 1L);

        assertTrue(longVar.isEnabled());
        assertThat(longVar.iterator().next().labelValues(), is(List.of(LABEL_4.value("4"))));
    }

    /* Caching long var */

    @Test
    public void cachingLongVar() {
        CachingLongVar cachingLongVar_1 = registry.cachingLongVar(
            withName("cachingLongVar"),
            () -> 1L,
            () -> withCachingLongVar().disable());

        assertFalse(cachingLongVar_1.isEnabled());
        verify(listener).cachingLongVarAdded(cachingLongVar_1);
        verifyNoMoreInteractions(listener);

        CachingLongVar cachingLongVar_2 = registry.cachingLongVar(
            withName("cachingLongVar"),
            () -> 2L);

        assertFalse(cachingLongVar_2.isEnabled());
        assertSame(cachingLongVar_2, cachingLongVar_1);
        verifyNoMoreInteractions(listener);

        CachingLongVar cachingLongVar_3 = registry.newCachingLongVar(
            withName("cachingLongVar"),
            () -> 3L);

        assertTrue(cachingLongVar_3.isEnabled());
        assertNotSame(cachingLongVar_3, cachingLongVar_2);
        verify(listener).cachingLongVarAdded(cachingLongVar_1);
        verify(listener).cachingLongVarRemoved(cachingLongVar_1);
        verify(listener).cachingLongVarAdded(cachingLongVar_3);
        verifyNoMoreInteractions(listener);

        CachingLongVar cachingLongVar_4 = registry.newCachingLongVar(
            withName("cachingLongVar"),
            () -> 4L,
            () -> cachingLongVarConfigBuilder().disable());

        assertFalse(cachingLongVar_4.isEnabled());
        assertNotSame(cachingLongVar_4, cachingLongVar_3);
        verify(listener).cachingLongVarAdded(cachingLongVar_1);
        verify(listener).cachingLongVarRemoved(cachingLongVar_1);
        verify(listener).cachingLongVarAdded(cachingLongVar_3);
        verify(listener).cachingLongVarRemoved(cachingLongVar_3);
        verify(listener).cachingLongVarAdded(cachingLongVar_4);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void cachingLongVar_PrefixLabelValuesKey() {
        CachingLongVar cachingLongVar = registry.cachingLongVar(
            withKey(name("cachingLongVar"), labelValues(LABEL_1.value("1"))),
            () -> 1L,
            () -> withCachingLongVar().prefix(labelValues(LABEL_2.value("2"))));

        assertThat(cachingLongVar.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));

        cachingLongVar = registry.newCachingLongVar(
            withKey(name("cachingLongVar"), labelValues(LABEL_1.value("1"))),
            () -> 1L);

        assertThat(cachingLongVar.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));
    }

    @Test
    public void cachingLongVar_PreConfig_And_PostConfig() {
        // pre
        registry.preConfigure(
            metricWithName("cachingLongVar"),
            modifying().cachingVar(withCachingVar().prefix(labelValues(LABEL_1.value("1")))));

        registry.preConfigure(
            metricWithName("cachingLongVar"),
            modifying().cachingVar(withCachingVar().prefix(labelValues(LABEL_2.value("2")))));

        CachingLongVar cachingLongVar = registry.cachingLongVar(
            withName("cachingLongVar"),
            () -> 1L);

        assertThat(cachingLongVar.iterator().next().labelValues(), is(List.of(LABEL_2.value("2"))));

        registry.preConfigure(
            metricWithName("cachingLongVar"),
            modifying().variable(withVar().disable().prefix(labelValues(LABEL_3.value("3")))));

        cachingLongVar = registry.newCachingLongVar(withName("cachingLongVar"), () -> 1L);
        assertFalse(cachingLongVar.isEnabled());

        registry.preConfigure(
            metricWithName("cachingLongVar"),
            modifying().metric(withMetric().enable()));

        cachingLongVar = registry.cachingLongVar(withName("cachingLongVar"), () -> 1L);
        assertFalse(cachingLongVar.isEnabled());

        cachingLongVar = registry.newCachingLongVar(withName("cachingLongVar"), () -> 1L);
        assertTrue(cachingLongVar.isEnabled());
        assertThat(cachingLongVar.iterator().next().labelValues(), is(List.of(LABEL_3.value("3"))));

        // and post
        registry.postConfigure(
            metricsWithNamePrefix("cachingLongVar"),
            modifying().cachingVar(withCachingVar().prefix(labelValues(LABEL_4.value("4")))));

        cachingLongVar = registry.newCachingLongVar(withName("cachingLongVar"), () -> 1L);
        assertThat(cachingLongVar.iterator().next().labelValues(), is(List.of(LABEL_4.value("4"))));

        registry.postConfigure(
            metricsWithNamePrefix("cachingLongVar"),
            modifying().variable(withVar().disable().prefix(labelValues(LABEL_5.value("5")))));

        cachingLongVar = registry.newCachingLongVar(withName("cachingLongVar"), () -> 1L);
        assertFalse(cachingLongVar.isEnabled());

        registry.postConfigure(
            metricsWithNamePrefix("cachingLongVar"),
            modifying().metric(withMetric().enable()));

        cachingLongVar = registry.newCachingLongVar(withName("cachingLongVar"), () -> 1L);
        assertThat(cachingLongVar.iterator().next().labelValues(), is(List.of(LABEL_5.value("5"))));
    }

    /* Double var */

    @Test
    public void doubleVar() {
        DoubleVar doubleVar_1 = registry.doubleVar(
            withName("doubleVar"),
            () -> 1.0,
            () -> withDoubleVar().disable());

        assertFalse(doubleVar_1.isEnabled());
        verify(listener).doubleVarAdded(doubleVar_1);
        verifyNoMoreInteractions(listener);

        DoubleVar doubleVar_2 = registry.doubleVar(withName("doubleVar"), () -> 2.0);

        assertFalse(doubleVar_2.isEnabled());
        assertSame(doubleVar_2, doubleVar_1);
        verifyNoMoreInteractions(listener);

        DoubleVar doubleVar_3 = registry.newDoubleVar(withName("doubleVar"), () -> 3.0);

        assertTrue(doubleVar_3.isEnabled());
        assertNotSame(doubleVar_3, doubleVar_2);
        verify(listener).doubleVarAdded(doubleVar_1);
        verify(listener).doubleVarRemoved(doubleVar_1);
        verify(listener).doubleVarAdded(doubleVar_3);
        verifyNoMoreInteractions(listener);

        DoubleVar doubleVar_4 = registry.newDoubleVar(
            withName("doubleVar"),
            () -> 4.0,
            () -> doubleVarConfigBuilder().disable());

        assertFalse(doubleVar_4.isEnabled());
        assertNotSame(doubleVar_4, doubleVar_3);
        verify(listener).doubleVarAdded(doubleVar_1);
        verify(listener).doubleVarRemoved(doubleVar_1);
        verify(listener).doubleVarAdded(doubleVar_3);
        verify(listener).doubleVarRemoved(doubleVar_3);
        verify(listener).doubleVarAdded(doubleVar_4);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void doubleVar_PrefixLabelValuesKey() {
        DoubleVar doubleVar = registry.doubleVar(
            withKey(name("doubleVar"), labelValues(LABEL_1.value("1"))),
            () -> 1.0,
            () -> withDoubleVar().prefix(labelValues(LABEL_2.value("2"))));

        assertThat(doubleVar.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));

        doubleVar = registry.newDoubleVar(
            withKey(name("doubleVar"), labelValues(LABEL_1.value("1"))),
            () -> 1.0);

        assertThat(doubleVar.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));
    }

    @Test
    public void doubleVar_PreConfig_And_PostConfig() {
        // pre
        registry.preConfigure(
            metricWithName("doubleVar"),
            modifying().variable(withVar().prefix(labelValues(LABEL_1.value("1")))));

        registry.preConfigure(
            metricWithName("doubleVar"),
            modifying().variable(withVar().disable().prefix(labelValues(LABEL_2.value("2")))));

        DoubleVar doubleVar = registry.doubleVar(withName("doubleVar"), () -> 1.0);
        assertFalse(doubleVar.isEnabled());

        registry.preConfigure(
            metricWithName("doubleVar"),
            modifying().metric(withMetric().enable()));

        doubleVar = registry.doubleVar(withName("doubleVar"), () -> 1.0);
        assertFalse(doubleVar.isEnabled());

        doubleVar = registry.newDoubleVar(withName("doubleVar"), () -> 1.0);

        assertTrue(doubleVar.isEnabled());
        assertThat(doubleVar.iterator().next().labelValues(), is(List.of(LABEL_2.value("2"))));

        // and post
        registry.postConfigure(
            metricsWithNamePrefix("doubleVar"),
            modifying().variable(withVar().prefix(labelValues(LABEL_3.value("3")))));

        registry.postConfigure(
            metricsWithNamePrefix("doubleVar"),
            modifying().variable(withVar().disable().prefix(labelValues(LABEL_4.value("4")))));

        doubleVar = registry.newDoubleVar(withName("doubleVar"), () -> 1.0);
        assertFalse(doubleVar.isEnabled());

        registry.postConfigure(
            metricsWithNamePrefix("doubleVar"),
            modifying().metric(withMetric().enable()));

        doubleVar = registry.newDoubleVar(withName("doubleVar"), () -> 1.0);

        assertTrue(doubleVar.isEnabled());
        assertThat(doubleVar.iterator().next().labelValues(), is(List.of(LABEL_4.value("4"))));
    }

    /* Caching double var */

    @Test
    public void cachingDoubleVar() {
        CachingDoubleVar cachingDoubleVar_1 = registry.cachingDoubleVar(
            withName("cachingDoubleVar"),
            () -> 1.0,
            () -> withCachingDoubleVar().disable());

        assertFalse(cachingDoubleVar_1.isEnabled());
        verify(listener).cachingDoubleVarAdded(cachingDoubleVar_1);
        verifyNoMoreInteractions(listener);

        CachingDoubleVar cachingDoubleVar_2 = registry.cachingDoubleVar(
            withName("cachingDoubleVar"),
            () -> 2.0);

        assertFalse(cachingDoubleVar_2.isEnabled());
        assertSame(cachingDoubleVar_2, cachingDoubleVar_1);
        verifyNoMoreInteractions(listener);

        CachingDoubleVar cachingDoubleVar_3 = registry.newCachingDoubleVar(
            withName("cachingDoubleVar"),
            () -> 3.0);

        assertTrue(cachingDoubleVar_3.isEnabled());
        assertNotSame(cachingDoubleVar_3, cachingDoubleVar_2);
        verify(listener).cachingDoubleVarAdded(cachingDoubleVar_1);
        verify(listener).cachingDoubleVarRemoved(cachingDoubleVar_1);
        verify(listener).cachingDoubleVarAdded(cachingDoubleVar_3);
        verifyNoMoreInteractions(listener);

        CachingDoubleVar cachingDoubleVar_4 = registry.newCachingDoubleVar(
            withName("cachingDoubleVar"),
            () -> 4.0,
            () -> cachingDoubleVarConfigBuilder().disable());

        assertFalse(cachingDoubleVar_4.isEnabled());
        assertNotSame(cachingDoubleVar_4, cachingDoubleVar_3);
        verify(listener).cachingDoubleVarAdded(cachingDoubleVar_1);
        verify(listener).cachingDoubleVarRemoved(cachingDoubleVar_1);
        verify(listener).cachingDoubleVarAdded(cachingDoubleVar_3);
        verify(listener).cachingDoubleVarRemoved(cachingDoubleVar_3);
        verify(listener).cachingDoubleVarAdded(cachingDoubleVar_4);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void cachingDoubleVar_PrefixLabelValuesKey() {
        CachingDoubleVar cachingDoubleVar = registry.cachingDoubleVar(
            withKey(name("cachingDoubleVar"), labelValues(LABEL_1.value("1"))),
            () -> 1.0,
            () -> withCachingDoubleVar().prefix(labelValues(LABEL_2.value("2"))));

        assertThat(cachingDoubleVar.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));

        cachingDoubleVar = registry.newCachingDoubleVar(
            withKey(name("cachingDoubleVar"), labelValues(LABEL_1.value("1"))),
            () -> 1.0);

        assertThat(cachingDoubleVar.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));
    }

    @Test
    public void cachingDoubleVar_PreConfig_And_PostConfig() {
        // pre
        registry.preConfigure(
            metricWithName("cachingDoubleVar"),
            modifying().cachingVar(withCachingVar().prefix(labelValues(LABEL_1.value("1")))));

        registry.preConfigure(
            metricWithName("cachingDoubleVar"),
            modifying().cachingVar(withCachingVar().prefix(labelValues(LABEL_2.value("2")))));

        CachingDoubleVar cachingDoubleVar = registry.cachingDoubleVar(withName("cachingDoubleVar"), () -> 1.0);
        assertThat(cachingDoubleVar.iterator().next().labelValues(), is(List.of(LABEL_2.value("2"))));

        registry.preConfigure(
            metricWithName("cachingDoubleVar"),
            modifying().variable(withVar().disable().prefix(labelValues(LABEL_3.value("3")))));

        cachingDoubleVar = registry.newCachingDoubleVar(withName("cachingDoubleVar"), () -> 1.0);
        assertFalse(cachingDoubleVar.isEnabled());

        registry.preConfigure(
            metricWithName("cachingDoubleVar"),
            modifying().metric(withMetric().enable()));

        cachingDoubleVar = registry.cachingDoubleVar(withName("cachingDoubleVar"), () -> 1.0);
        assertFalse(cachingDoubleVar.isEnabled());

        cachingDoubleVar = registry.newCachingDoubleVar(withName("cachingDoubleVar"), () -> 1.0);
        assertTrue(cachingDoubleVar.isEnabled());
        assertThat(cachingDoubleVar.iterator().next().labelValues(), is(List.of(LABEL_3.value("3"))));

        // and post
        registry.postConfigure(
            metricsWithNamePrefix("cachingDoubleVar"),
            modifying().cachingVar(withCachingVar().prefix(labelValues(LABEL_4.value("4")))));

        cachingDoubleVar = registry.newCachingDoubleVar(withName("cachingDoubleVar"), () -> 1.0);
        assertThat(cachingDoubleVar.iterator().next().labelValues(), is(List.of(LABEL_4.value("4"))));

        registry.postConfigure(
            metricsWithNamePrefix("cachingDoubleVar"),
            modifying().variable(withVar().disable().prefix(labelValues(LABEL_5.value("5")))));

        cachingDoubleVar = registry.newCachingDoubleVar(withName("cachingDoubleVar"), () -> 1.0);
        assertFalse(cachingDoubleVar.isEnabled());

        registry.postConfigure(
            metricsWithNamePrefix("cachingDoubleVar"),
            modifying().metric(withMetric().enable()));

        cachingDoubleVar = registry.newCachingDoubleVar(withName("cachingDoubleVar"), () -> 1.0);
        assertThat(cachingDoubleVar.iterator().next().labelValues(), is(List.of(LABEL_5.value("5"))));
    }

    /* String var */

    @Test
    public void stringVar() {
        StringVar stringVar_1 = registry.stringVar(
            withName("stringVar"),
            () -> "1",
            () -> withStringVar().disable());

        assertFalse(stringVar_1.isEnabled());
        verify(listener).stringVarAdded(stringVar_1);
        verifyNoMoreInteractions(listener);

        StringVar stringVar_2 = registry.stringVar(withName("stringVar"), () -> "2");

        assertFalse(stringVar_2.isEnabled());
        assertSame(stringVar_2, stringVar_1);
        verifyNoMoreInteractions(listener);

        StringVar stringVar_3 = registry.newStringVar(withName("stringVar"), () -> "3");

        assertTrue(stringVar_3.isEnabled());
        assertNotSame(stringVar_3, stringVar_2);
        verify(listener).stringVarAdded(stringVar_1);
        verify(listener).stringVarRemoved(stringVar_1);
        verify(listener).stringVarAdded(stringVar_3);
        verifyNoMoreInteractions(listener);

        StringVar stringVar_4 = registry.newStringVar(
            withName("stringVar"),
            () -> "4",
            () -> stringVarConfigBuilder().disable());

        assertFalse(stringVar_4.isEnabled());
        assertNotSame(stringVar_4, stringVar_3);
        verify(listener).stringVarAdded(stringVar_1);
        verify(listener).stringVarRemoved(stringVar_1);
        verify(listener).stringVarAdded(stringVar_3);
        verify(listener).stringVarRemoved(stringVar_3);
        verify(listener).stringVarAdded(stringVar_4);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void stringVar_PrefixLabelValuesKey() {
        StringVar stringVar = registry.stringVar(
            withKey(name("stringVar"), labelValues(LABEL_1.value("1"))),
            () -> "1",
            () -> withStringVar().prefix(labelValues(LABEL_2.value("2"))));

        assertThat(stringVar.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));

        stringVar = registry.newStringVar(
            withKey(name("stringVar"), labelValues(LABEL_1.value("1"))),
            () -> "1");

        assertThat(stringVar.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));
    }

    @Test
    public void stringVar_PreConfig_And_PostConfig() {
        // pre
        registry.preConfigure(
            metricWithName("stringVar"),
            modifying().variable(withVar().prefix(labelValues(LABEL_1.value("1")))));

        registry.preConfigure(
            metricWithName("stringVar"),
            modifying().variable(withVar().disable().prefix(labelValues(LABEL_2.value("2")))));

        StringVar stringVar = registry.stringVar(withName("stringVar"), () -> "1");
        assertFalse(stringVar.isEnabled());

        registry.preConfigure(
            metricWithName("stringVar"),
            modifying().metric(withMetric().enable()));

        stringVar = registry.stringVar(withName("stringVar"), () -> "1");
        assertFalse(stringVar.isEnabled());

        stringVar = registry.newStringVar(withName("stringVar"), () -> "1");

        assertTrue(stringVar.isEnabled());
        assertThat(stringVar.iterator().next().labelValues(), is(List.of(LABEL_2.value("2"))));

        // and post
        registry.postConfigure(
            metricsWithNamePrefix("stringVar"),
            modifying().variable(withVar().prefix(labelValues(LABEL_3.value("3")))));

        registry.postConfigure(
            metricsWithNamePrefix("stringVar"),
            modifying().variable(withVar().disable().prefix(labelValues(LABEL_4.value("4")))));

        stringVar = registry.newStringVar(withName("stringVar"), () -> "1");
        assertFalse(stringVar.isEnabled());

        registry.postConfigure(
            metricsWithNamePrefix("stringVar"),
            modifying().metric(withMetric().enable()));

        stringVar = registry.newStringVar(withName("stringVar"), () -> "1");

        assertTrue(stringVar.isEnabled());
        assertThat(stringVar.iterator().next().labelValues(), is(List.of(LABEL_4.value("4"))));
    }

    /* Caching string var */

    @Test
    public void cachingStringVar() {
        CachingStringVar cachingStringVar_1 = registry.cachingStringVar(
            withName("cachingStringVar"),
            () -> "1",
            () -> withCachingStringVar().disable());

        assertFalse(cachingStringVar_1.isEnabled());
        verify(listener).cachingStringVarAdded(cachingStringVar_1);
        verifyNoMoreInteractions(listener);

        CachingStringVar cachingStringVar_2 = registry.cachingStringVar(
            withName("cachingStringVar"),
            () -> "2");

        assertFalse(cachingStringVar_2.isEnabled());
        assertSame(cachingStringVar_2, cachingStringVar_1);
        verifyNoMoreInteractions(listener);

        CachingStringVar cachingStringVar_3 = registry.newCachingStringVar(
            withName("cachingStringVar"),
            () -> "3");

        assertTrue(cachingStringVar_3.isEnabled());
        assertNotSame(cachingStringVar_3, cachingStringVar_2);
        verify(listener).cachingStringVarAdded(cachingStringVar_1);
        verify(listener).cachingStringVarRemoved(cachingStringVar_1);
        verify(listener).cachingStringVarAdded(cachingStringVar_3);
        verifyNoMoreInteractions(listener);

        CachingStringVar cachingStringVar_4 = registry.newCachingStringVar(
            withName("cachingStringVar"),
            () -> "4",
            () -> cachingStringVarConfigBuilder().disable());

        assertFalse(cachingStringVar_4.isEnabled());
        assertNotSame(cachingStringVar_4, cachingStringVar_3);
        verify(listener).cachingStringVarAdded(cachingStringVar_1);
        verify(listener).cachingStringVarRemoved(cachingStringVar_1);
        verify(listener).cachingStringVarAdded(cachingStringVar_3);
        verify(listener).cachingStringVarRemoved(cachingStringVar_3);
        verify(listener).cachingStringVarAdded(cachingStringVar_4);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void cachingStringVar_PrefixLabelValuesKey() {
        CachingStringVar cachingStringVar = registry.cachingStringVar(
            withKey(name("cachingStringVar"), labelValues(LABEL_1.value("1"))),
            () -> "1",
            () -> withCachingStringVar().prefix(labelValues(LABEL_2.value("2"))));

        assertThat(cachingStringVar.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));

        cachingStringVar = registry.newCachingStringVar(
            withKey(name("cachingStringVar"), labelValues(LABEL_1.value("1"))),
            () -> "1");

        assertThat(cachingStringVar.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));
    }

    @Test
    public void cachingStringVar_PreConfig_And_PostConfig() {
        // pre
        registry.preConfigure(
            metricWithName("cachingStringVar"),
            modifying().cachingVar(withCachingVar().prefix(labelValues(LABEL_1.value("1")))));

        registry.preConfigure(
            metricWithName("cachingStringVar"),
            modifying().cachingVar(withCachingVar().prefix(labelValues(LABEL_2.value("2")))));

        CachingStringVar cachingStringVar = registry.cachingStringVar(withName("cachingStringVar"), () -> "1");
        assertThat(cachingStringVar.iterator().next().labelValues(), is(List.of(LABEL_2.value("2"))));

        registry.preConfigure(
            metricWithName("cachingStringVar"),
            modifying().variable(withVar().disable().prefix(labelValues(LABEL_3.value("3")))));

        cachingStringVar = registry.newCachingStringVar(withName("cachingStringVar"), () -> "1");
        assertFalse(cachingStringVar.isEnabled());

        registry.preConfigure(
            metricWithName("cachingStringVar"),
            modifying().metric(withMetric().enable()));

        cachingStringVar = registry.cachingStringVar(withName("cachingStringVar"), () -> "1");
        assertFalse(cachingStringVar.isEnabled());

        cachingStringVar = registry.newCachingStringVar(withName("cachingStringVar"), () -> "1");
        assertTrue(cachingStringVar.isEnabled());
        assertThat(cachingStringVar.iterator().next().labelValues(), is(List.of(LABEL_3.value("3"))));

        // and post
        registry.postConfigure(
            metricsWithNamePrefix("cachingStringVar"),
            modifying().cachingVar(withCachingVar().prefix(labelValues(LABEL_4.value("4")))));

        cachingStringVar = registry.newCachingStringVar(withName("cachingStringVar"), () -> "1");
        assertThat(cachingStringVar.iterator().next().labelValues(), is(List.of(LABEL_4.value("4"))));

        registry.postConfigure(
            metricsWithNamePrefix("cachingStringVar"),
            modifying().variable(withVar().disable().prefix(labelValues(LABEL_5.value("5")))));

        cachingStringVar = registry.newCachingStringVar(withName("cachingStringVar"), () -> "1");
        assertFalse(cachingStringVar.isEnabled());

        registry.postConfigure(
            metricsWithNamePrefix("cachingStringVar"),
            modifying().metric(withMetric().enable()));

        cachingStringVar = registry.newCachingStringVar(withName("cachingStringVar"), () -> "1");
        assertThat(cachingStringVar.iterator().next().labelValues(), is(List.of(LABEL_5.value("5"))));
    }

    /* Counter */

    @Test
    public void counter() {
        Counter counter_1 = registry.counter(
            withName("counter"),
            () -> withCounter().disable());

        assertFalse(counter_1.isEnabled());
        verify(listener).counterAdded(counter_1);
        verifyNoMoreInteractions(listener);

        Counter counter_2 = registry.counter(withName("counter") );

        assertFalse(counter_2.isEnabled());
        assertSame(counter_2, counter_1);
        verifyNoMoreInteractions(listener);

        Counter counter_3 = registry.newCounter(withName("counter"));

        assertTrue(counter_3.isEnabled());
        assertNotSame(counter_3, counter_2);
        verify(listener).counterAdded(counter_1);
        verify(listener).counterRemoved(counter_1);
        verify(listener).counterAdded(counter_3);
        verifyNoMoreInteractions(listener);

        Counter counter_4 = registry.newCounter(
            withName("counter"),
            () -> counterConfigBuilder().disable());

        assertFalse(counter_4.isEnabled());
        assertNotSame(counter_4, counter_3);
        verify(listener).counterAdded(counter_1);
        verify(listener).counterRemoved(counter_1);
        verify(listener).counterAdded(counter_3);
        verify(listener).counterRemoved(counter_3);
        verify(listener).counterAdded(counter_4);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void counter_PrefixLabelValuesKey() {
        Counter counter = registry.counter(
            withKey(name("counter"), labelValues(LABEL_1.value("1"))),
            () -> withCounter().prefix(labelValues(LABEL_2.value("2"))));

        assertThat(counter.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));

        counter = registry.newCounter(withKey(name("counter"), labelValues(LABEL_1.value("1"))));
        assertThat(counter.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));
    }

    @Test
    public void counter_PreConfig_And_PostConfig() {
        // pre
        registry.preConfigure(
            metricWithName("counter"),
            modifying().counter(withCounter().prefix(labelValues(LABEL_1.value("1")))));

        registry.preConfigure(
            metricWithName("counter"),
            modifying().counter(withCounter().prefix(labelValues(LABEL_2.value("2")))));

        Counter counter = registry.counter(withName("counter"));
        assertThat(counter.iterator().next().labelValues(), is(List.of(LABEL_2.value("2"))));

        registry.preConfigure(
            metricWithName("counter"),
            modifying().meter(withMeter().disable().prefix(labelValues(LABEL_3.value("3")))));

        counter = registry.newCounter(withName("counter"));
        assertFalse(counter.isEnabled());

        registry.preConfigure(
            metricWithName("counter"),
            modifying().metric(withMetric().enable()));

        counter = registry.counter(withName("counter"));
        assertFalse(counter.isEnabled());

        counter = registry.newCounter(withName("counter"));
        assertTrue(counter.isEnabled());
        assertThat(counter.iterator().next().labelValues(), is(List.of(LABEL_3.value("3"))));

        // and post
        registry.postConfigure(
            metricsWithNamePrefix("counter"),
            modifying().counter(withCounter().prefix(labelValues(LABEL_4.value("4")))));

        counter = registry.newCounter(withName("counter"));
        assertThat(counter.iterator().next().labelValues(), is(List.of(LABEL_4.value("4"))));

        registry.postConfigure(
            metricsWithNamePrefix("counter"),
            modifying().meter(withMeter().disable().prefix(labelValues(LABEL_5.value("5")))));

        counter = registry.newCounter(withName("counter"));
        assertFalse(counter.isEnabled());

        registry.postConfigure(
            metricsWithNamePrefix("counter"),
            modifying().metric(withMetric().enable()));

        counter = registry.newCounter(withName("counter"));
        assertThat(counter.iterator().next().labelValues(), is(List.of(LABEL_5.value("5"))));
    }

    /* Rate */

    @Test
    public void rate() {
        Rate rate_1 = registry.rate(
            withName("rate"),
            () -> withRate().disable());

        assertFalse(rate_1.isEnabled());
        verify(listener).rateAdded(rate_1);
        verifyNoMoreInteractions(listener);

        Rate rate_2 = registry.rate(withName("rate"));

        assertFalse(rate_2.isEnabled());
        assertSame(rate_2, rate_1);
        verifyNoMoreInteractions(listener);

        Rate rate_3 = registry.newRate(withName("rate"));

        assertTrue(rate_3.isEnabled());
        assertNotSame(rate_3, rate_2);
        verify(listener).rateAdded(rate_1);
        verify(listener).rateRemoved(rate_1);
        verify(listener).rateAdded(rate_3);
        verifyNoMoreInteractions(listener);

        Rate rate_4 = registry.newRate(
            withName("rate"),
            () -> rateConfigBuilder().disable());

        assertFalse(rate_4.isEnabled());
        assertNotSame(rate_4, rate_3);
        verify(listener).rateAdded(rate_1);
        verify(listener).rateRemoved(rate_1);
        verify(listener).rateAdded(rate_3);
        verify(listener).rateRemoved(rate_3);
        verify(listener).rateAdded(rate_4);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void rate_PrefixLabelValuesKey() {
        Rate rate = registry.rate(
            withKey(name("rate"), labelValues(LABEL_1.value("1"))),
            () -> withRate().prefix(labelValues(LABEL_2.value("2"))));

        assertThat(rate.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));

        rate = registry.newRate(withKey(name("rate"), labelValues(LABEL_1.value("1"))));
        assertThat(rate.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));
    }

    @Test
    public void rate_PreConfig_And_PostConfig() {
        // pre
        registry.preConfigure(
            metricWithName("rate"),
            modifying().rate(withRate().prefix(labelValues(LABEL_1.value("1")))));

        registry.preConfigure(
            metricWithName("rate"),
            modifying().rate(withRate().prefix(labelValues(LABEL_2.value("2")))));

        Rate rate = registry.rate(withName("rate"));
        assertThat(rate.iterator().next().labelValues(), is(List.of(LABEL_2.value("2"))));

        registry.preConfigure(
            metricWithName("rate"),
            modifying().meter(withMeter().disable().prefix(labelValues(LABEL_3.value("3")))));

        rate = registry.newRate(withName("rate"));
        assertFalse(rate.isEnabled());

        registry.preConfigure(
            metricWithName("rate"),
            modifying().metric(withMetric().enable()));

        rate = registry.rate(withName("rate"));
        assertFalse(rate.isEnabled());

        rate = registry.newRate(withName("rate"));
        assertTrue(rate.isEnabled());
        assertThat(rate.iterator().next().labelValues(), is(List.of(LABEL_3.value("3"))));

        // and post
        registry.postConfigure(
            metricsWithNamePrefix("rate"),
            modifying().rate(withRate().prefix(labelValues(LABEL_4.value("4")))));

        rate = registry.newRate(withName("rate"));
        assertThat(rate.iterator().next().labelValues(), is(List.of(LABEL_4.value("4"))));

        registry.postConfigure(
            metricsWithNamePrefix("rate"),
            modifying().meter(withMeter().disable().prefix(labelValues(LABEL_5.value("5")))));

        rate = registry.newRate(withName("rate"));
        assertFalse(rate.isEnabled());

        registry.postConfigure(
            metricsWithNamePrefix("rate"),
            modifying().metric(withMetric().enable()));

        rate = registry.newRate(withName("rate"));
        assertThat(rate.iterator().next().labelValues(), is(List.of(LABEL_5.value("5"))));
    }

    /* Histogram */

    @Test
    public void histogram() {
        Histogram histogram_1 = registry.histogram(
            withName("histogram"),
            () -> withHistogram().disable());

        assertFalse(histogram_1.isEnabled());
        verify(listener).histogramAdded(histogram_1);
        verifyNoMoreInteractions(listener);

        Histogram histogram_2 = registry.histogram(withName("histogram"));

        assertFalse(histogram_2.isEnabled());
        assertSame(histogram_2, histogram_1);
        verifyNoMoreInteractions(listener);

        Histogram histogram_3 = registry.newHistogram(withName("histogram"));

        assertTrue(histogram_3.isEnabled());
        assertNotSame(histogram_3, histogram_2);
        verify(listener).histogramAdded(histogram_1);
        verify(listener).histogramRemoved(histogram_1);
        verify(listener).histogramAdded(histogram_3);
        verifyNoMoreInteractions(listener);

        Histogram histogram_4 = registry.newHistogram(
            withName("histogram"),
            () -> histogramConfigBuilder().disable());

        assertFalse(histogram_4.isEnabled());
        assertNotSame(histogram_4, histogram_3);
        verify(listener).histogramAdded(histogram_1);
        verify(listener).histogramRemoved(histogram_1);
        verify(listener).histogramAdded(histogram_3);
        verify(listener).histogramRemoved(histogram_3);
        verify(listener).histogramAdded(histogram_4);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void histogram_PrefixLabelValuesKey() {
        Histogram histogram = registry.histogram(
            withKey(name("histogram"), labelValues(LABEL_1.value("1"))),
            () -> withHistogram().prefix(labelValues(LABEL_2.value("2"))));

        assertThat(histogram.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));

        histogram = registry.newHistogram(withKey(name("histogram"), labelValues(LABEL_1.value("1"))));
        assertThat(histogram.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));
    }

    @Test
    public void histogram_PreConfig_And_PostConfig() {
        // pre
        registry.preConfigure(
            metricWithName("histogram"),
            modifying().histogram(withHistogram().prefix(labelValues(LABEL_1.value("1")))));

        registry.preConfigure(
            metricWithName("histogram"),
            modifying().histogram(withHistogram().prefix(labelValues(LABEL_2.value("2")))));

        Histogram histogram = registry.histogram(withName("histogram"));
        assertThat(histogram.iterator().next().labelValues(), is(List.of(LABEL_2.value("2"))));

        registry.preConfigure(
            metricWithName("histogram"),
            modifying().meter(withMeter().disable().prefix(labelValues(LABEL_3.value("3")))));

        histogram = registry.newHistogram(withName("histogram"));
        assertFalse(histogram.isEnabled());

        registry.preConfigure(
            metricWithName("histogram"),
            modifying().metric(withMetric().enable()));

        histogram = registry.histogram(withName("histogram"));
        assertFalse(histogram.isEnabled());

        histogram = registry.newHistogram(withName("histogram"));
        assertTrue(histogram.isEnabled());
        assertThat(histogram.iterator().next().labelValues(), is(List.of(LABEL_3.value("3"))));

        // and post
        registry.postConfigure(
            metricsWithNamePrefix("histogram"),
            modifying().histogram(withHistogram().prefix(labelValues(LABEL_4.value("4")))));

        histogram = registry.newHistogram(withName("histogram"));
        assertThat(histogram.iterator().next().labelValues(), is(List.of(LABEL_4.value("4"))));

        registry.postConfigure(
            metricsWithNamePrefix("histogram"),
            modifying().meter(withMeter().disable().prefix(labelValues(LABEL_5.value("5")))));

        histogram = registry.newHistogram(withName("histogram"));
        assertFalse(histogram.isEnabled());

        registry.postConfigure(
            metricsWithNamePrefix("histogram"),
            modifying().metric(withMetric().enable()));

        histogram = registry.newHistogram(withName("histogram"));
        assertThat(histogram.iterator().next().labelValues(), is(List.of(LABEL_5.value("5"))));
    }

    /* Timer */

    @Test
    public void timer() {
        Timer timer_1 = registry.timer(
            withName("timer"),
            () -> withTimer().disable());

        assertFalse(timer_1.isEnabled());
        verify(listener).timerAdded(timer_1);
        verifyNoMoreInteractions(listener);

        Timer timer_2 = registry.timer(withName("timer"));

        assertFalse(timer_2.isEnabled());
        assertSame(timer_2, timer_1);
        verifyNoMoreInteractions(listener);

        Timer timer_3 = registry.newTimer(withName("timer"));

        assertTrue(timer_3.isEnabled());
        assertNotSame(timer_3, timer_2);
        verify(listener).timerAdded(timer_1);
        verify(listener).timerRemoved(timer_1);
        verify(listener).timerAdded(timer_3);
        verifyNoMoreInteractions(listener);

        Timer timer_4 = registry.newTimer(
            withName("timer"),
            () -> timerConfigBuilder().disable());

        assertFalse(timer_4.isEnabled());
        assertNotSame(timer_4, timer_3);
        verify(listener).timerAdded(timer_1);
        verify(listener).timerRemoved(timer_1);
        verify(listener).timerAdded(timer_3);
        verify(listener).timerRemoved(timer_3);
        verify(listener).timerAdded(timer_4);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void timer_PrefixLabelValuesKey() {
        Timer timer = registry.timer(
            withKey(name("timer"), labelValues(LABEL_1.value("1"))),
            () -> withTimer().prefix(labelValues(LABEL_2.value("2"))));

        assertThat(timer.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));

        timer = registry.newTimer(withKey(name("timer"), labelValues(LABEL_1.value("1"))));
        assertThat(timer.iterator().next().labelValues(), is(List.of(LABEL_1.value("1"))));
    }

    @Test
    public void timer_PreConfig_And_PostConfig() {
        // pre
        registry.preConfigure(
            metricWithName("timer"),
            modifying().timer(withTimer().prefix(labelValues(LABEL_1.value("1")))));

        registry.preConfigure(
            metricWithName("timer"),
            modifying().timer(withTimer().prefix(labelValues(LABEL_2.value("2")))));

        Timer timer = registry.timer(withName("timer"));
        assertThat(timer.iterator().next().labelValues(), is(List.of(LABEL_2.value("2"))));

        registry.preConfigure(
            metricWithName("timer"),
            modifying().meter(withMeter().disable().prefix(labelValues(LABEL_3.value("3")))));

        timer = registry.newTimer(withName("timer"));
        assertFalse(timer.isEnabled());

        registry.preConfigure(
            metricWithName("timer"),
            modifying().metric(withMetric().enable()));

        timer = registry.timer(withName("timer"));
        assertFalse(timer.isEnabled());

        timer = registry.newTimer(withName("timer"));
        assertTrue(timer.isEnabled());
        assertThat(timer.iterator().next().labelValues(), is(List.of(LABEL_3.value("3"))));

        // and post
        registry.postConfigure(
            metricsWithNamePrefix("timer"),
            modifying().timer(withTimer().prefix(labelValues(LABEL_4.value("4")))));

        timer = registry.newTimer(withName("timer"));
        assertThat(timer.iterator().next().labelValues(), is(List.of(LABEL_4.value("4"))));

        registry.postConfigure(
            metricsWithNamePrefix("timer"),
            modifying().meter(withMeter().disable().prefix(labelValues(LABEL_5.value("5")))));

        timer = registry.newTimer(withName("timer"));
        assertFalse(timer.isEnabled());

        registry.postConfigure(
            metricsWithNamePrefix("timer"),
            modifying().metric(withMetric().enable()));

        timer = registry.newTimer(withName("timer"));
        assertThat(timer.iterator().next().labelValues(), is(List.of(LABEL_5.value("5"))));
    }

    /* Metric collision */

    @Test(expected = MetricCollisionException.class)
    public void collision_NameKey() {
        registry.counter(withName("name"));
        registry.rate(withName("name"));
    }

    @Test(expected = MetricCollisionException.class)
    public void collision_PrefixLabelValuesKey() {
        registry.histogram(withKey(name("name"), labelValues(LABEL_1.value("1"))));
        registry.timer(withKey(name("name"), labelValues(LABEL_1.value("1"))));
    }

    @Test
    public void noCollision_PrefixLabelValuesKey_With_SameNames_And_DifferentLabelValues() {
        registry.histogram(withKey(name("name"), labelValues(LABEL_1.value("1"))));
        registry.timer(withKey(name("name"), labelValues(LABEL_1.value("1_2"))));
        registry.timer(withKey(name("name"), labelValues(LABEL_2.value("2"))));
    }
}