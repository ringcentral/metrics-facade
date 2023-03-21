package com.ringcentral.platform.metrics.reporters.jmx;

import com.ringcentral.platform.metrics.AbstractMeter.MeasurableValueProvider;
import com.ringcentral.platform.metrics.MetricListener;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.meter.*;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.timer.Timer;
import com.ringcentral.platform.metrics.utils.Ref;
import com.ringcentral.platform.metrics.var.DefaultVarInstance;
import com.ringcentral.platform.metrics.var.doubleVar.*;
import com.ringcentral.platform.metrics.var.longVar.*;
import com.ringcentral.platform.metrics.var.objectVar.*;
import com.ringcentral.platform.metrics.var.stringVar.*;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.*;

import javax.management.*;
import java.util.*;
import java.util.function.Supplier;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.rate.Rate.*;
import static com.ringcentral.platform.metrics.var.doubleVar.DoubleVar.DOUBLE_VALUE;
import static com.ringcentral.platform.metrics.var.longVar.LongVar.LONG_VALUE;
import static com.ringcentral.platform.metrics.var.objectVar.ObjectVar.OBJECT_VALUE;
import static com.ringcentral.platform.metrics.var.stringVar.StringVar.STRING_VALUE;
import static java.util.Collections.emptyList;
import static javax.management.MBeanServerFactory.newMBeanServer;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings({ "unchecked", "SameParameterValue" })
public class JmxMetricsReporterTest {

    static final Label LABEL_1 = new Label("label_1");
    static final Label LABEL_2 = new Label("label_2");

    MBeanServer mBeanServer = newMBeanServer();
    JmxMetricsReporter reporter = new JmxMetricsReporter(mBeanServer);
    int beforeMBeanCount;

    @Before
    public void before() {
        beforeMBeanCount = mBeanServer.getMBeanCount();
        assertThat(mBeanServer.getMBeanCount(), is(beforeMBeanCount));
    }

    @After
    public void after() {
        reporter.close();
        assertThat(mBeanServer.getMBeanCount(), is(beforeMBeanCount));
    }

    @Test
    public void objectVar() {
        for (int i = 0; i < 2; ++i) {
            ObjectVar objectVar = mock(ObjectVar.class);
            Supplier<Object> valueSupplier = mock(Supplier.class);
            when(valueSupplier.get()).thenReturn(1L, "2");

            DefaultVarInstance<Object> instance = new DefaultVarInstance<>(
                withName("objectVar", "a", "b"),
                emptyList(),
                true,
                false,
                false,
                OBJECT_VALUE,
                valueSupplier);

            Ref<MetricListener> listenerRef = new Ref<>();

            doAnswer(invocation -> {
                MetricListener listener = (MetricListener)invocation.getArguments()[0];
                listenerRef.setValue(listener);
                listener.metricInstanceAdded(instance);
                return null;
            }).when(objectVar).addListener(any());

            reporter.objectVarAdded(objectVar);
            assertThat(attrValue("objectVar.a.b", "value"), is(1L));
            assertThat(attrValue("objectVar.a.b", "value"), is("2"));
            listenerRef.value().metricInstanceRemoved(instance);
            reporter.objectVarRemoved(objectVar);
        }
    }

    @Test
    public void cachingObjectVar() {
        for (int i = 0; i < 2; ++i) {
            CachingObjectVar cachingObjectVar = mock(CachingObjectVar.class);
            Supplier<Object> valueSupplier = mock(Supplier.class);
            when(valueSupplier.get()).thenReturn(1L, "2");

            DefaultVarInstance<Object> instance = new DefaultVarInstance<>(
                withName("cachingObjectVar", "a", "b"),
                emptyList(),
                true,
                false,
                false,
                OBJECT_VALUE,
                valueSupplier);

            Ref<MetricListener> listenerRef = new Ref<>();

            doAnswer(invocation -> {
                MetricListener listener = (MetricListener)invocation.getArguments()[0];
                listenerRef.setValue(listener);
                listener.metricInstanceAdded(instance);
                return null;
            }).when(cachingObjectVar).addListener(any());

            reporter.cachingObjectVarAdded(cachingObjectVar);
            assertThat(attrValue("cachingObjectVar.a.b", "value"), is(1L));
            assertThat(attrValue("cachingObjectVar.a.b", "value"), is("2"));
            listenerRef.value().metricInstanceRemoved(instance);
            reporter.cachingObjectVarRemoved(cachingObjectVar);
        }
    }

    @Test
    public void longVar() {
        for (int i = 0; i < 2; ++i) {
            LongVar longVar = mock(LongVar.class);
            Supplier<Long> valueSupplier = mock(Supplier.class);
            when(valueSupplier.get()).thenReturn(1L, 2L);

            DefaultVarInstance<Long> instance = new DefaultVarInstance<>(
                withName("longVar", "a", "b"),
                emptyList(),
                true,
                false,
                false,
                LONG_VALUE,
                valueSupplier);

            Ref<MetricListener> listenerRef = new Ref<>();

            doAnswer(invocation -> {
                MetricListener listener = (MetricListener)invocation.getArguments()[0];
                listenerRef.setValue(listener);
                listener.metricInstanceAdded(instance);
                return null;
            }).when(longVar).addListener(any());

            reporter.longVarAdded(longVar);
            assertThat(attrValue("longVar.a.b", "value"), is(1L));
            assertThat(attrValue("longVar.a.b", "value"), is(2L));
            listenerRef.value().metricInstanceRemoved(instance);
            reporter.longVarRemoved(longVar);
        }
    }

    @Test
    public void cachingLongVar() {
        for (int i = 0; i < 2; ++i) {
            CachingLongVar cachingLongVar = mock(CachingLongVar.class);
            Supplier<Long> valueSupplier = mock(Supplier.class);
            when(valueSupplier.get()).thenReturn(1L, 2L);

            DefaultVarInstance<Long> instance = new DefaultVarInstance<>(
                withName("cachingLongVar", "a", "b"),
                emptyList(),
                true,
                false,
                false,
                LONG_VALUE,
                valueSupplier);

            Ref<MetricListener> listenerRef = new Ref<>();

            doAnswer(invocation -> {
                MetricListener listener = (MetricListener)invocation.getArguments()[0];
                listenerRef.setValue(listener);
                listener.metricInstanceAdded(instance);
                return null;
            }).when(cachingLongVar).addListener(any());

            reporter.cachingLongVarAdded(cachingLongVar);
            assertThat(attrValue("cachingLongVar.a.b", "value"), is(1L));
            assertThat(attrValue("cachingLongVar.a.b", "value"), is(2L));
            listenerRef.value().metricInstanceRemoved(instance);
            reporter.cachingLongVarRemoved(cachingLongVar);
        }
    }

    @Test
    public void doubleVar() {
        for (int i = 0; i < 2; ++i) {
            DoubleVar doubleVar = mock(DoubleVar.class);
            Supplier<Double> valueSupplier = mock(Supplier.class);
            when(valueSupplier.get()).thenReturn(1.0, 2.0);

            DefaultVarInstance<Double> instance = new DefaultVarInstance<>(
                withName("doubleVar", "a", "b"),
                emptyList(),
                true,
                false,
                false,
                DOUBLE_VALUE,
                valueSupplier);

            Ref<MetricListener> listenerRef = new Ref<>();

            doAnswer(invocation -> {
                MetricListener listener = (MetricListener)invocation.getArguments()[0];
                listenerRef.setValue(listener);
                listener.metricInstanceAdded(instance);
                return null;
            }).when(doubleVar).addListener(any());

            reporter.doubleVarAdded(doubleVar);
            assertThat(attrValue("doubleVar.a.b", "value"), is(1.0));
            assertThat(attrValue("doubleVar.a.b", "value"), is(2.0));
            listenerRef.value().metricInstanceRemoved(instance);
            reporter.doubleVarRemoved(doubleVar);
        }
    }

    @Test
    public void cachingDoubleVar() {
        for (int i = 0; i < 2; ++i) {
            CachingDoubleVar cachingDoubleVar = mock(CachingDoubleVar.class);
            Supplier<Double> valueSupplier = mock(Supplier.class);
            when(valueSupplier.get()).thenReturn(1.0, 2.0);

            DefaultVarInstance<Double> instance = new DefaultVarInstance<>(
                withName("cachingDoubleVar", "a", "b"),
                emptyList(),
                true,
                false,
                false,
                DOUBLE_VALUE,
                valueSupplier);

            Ref<MetricListener> listenerRef = new Ref<>();

            doAnswer(invocation -> {
                MetricListener listener = (MetricListener)invocation.getArguments()[0];
                listenerRef.setValue(listener);
                listener.metricInstanceAdded(instance);
                return null;
            }).when(cachingDoubleVar).addListener(any());

            reporter.cachingDoubleVarAdded(cachingDoubleVar);
            assertThat(attrValue("cachingDoubleVar.a.b", "value"), is(1.0));
            assertThat(attrValue("cachingDoubleVar.a.b", "value"), is(2.0));
            listenerRef.value().metricInstanceRemoved(instance);
            reporter.cachingDoubleVarRemoved(cachingDoubleVar);
        }
    }

    @Test
    public void stringVar() {
        for (int i = 0; i < 2; ++i) {
            StringVar stringVar = mock(StringVar.class);
            Supplier<String> valueSupplier = mock(Supplier.class);
            when(valueSupplier.get()).thenReturn("V_1", "V_2");

            DefaultVarInstance<String> instance = new DefaultVarInstance<>(
                withName("stringVar", "a", "b"),
                emptyList(),
                true,
                false,
                false,
                STRING_VALUE,
                valueSupplier);

            Ref<MetricListener> listenerRef = new Ref<>();

            doAnswer(invocation -> {
                MetricListener listener = (MetricListener)invocation.getArguments()[0];
                listenerRef.setValue(listener);
                listener.metricInstanceAdded(instance);
                return null;
            }).when(stringVar).addListener(any());

            reporter.stringVarAdded(stringVar);
            assertThat(attrValue("stringVar.a.b", "value"), is("V_1"));
            assertThat(attrValue("stringVar.a.b", "value"), is("V_2"));
            listenerRef.value().metricInstanceRemoved(instance);
            reporter.stringVarRemoved(stringVar);
        }
    }

    @Test
    public void cachingStringVar() {
        for (int i = 0; i < 2; ++i) {
            CachingStringVar cachingStringVar = mock(CachingStringVar.class);
            Supplier<String> valueSupplier = mock(Supplier.class);
            when(valueSupplier.get()).thenReturn("V_1", "V_2");

            DefaultVarInstance<String> instance = new DefaultVarInstance<>(
                withName("cachingStringVar", "a", "b"),
                emptyList(),
                true,
                false,
                false,
                STRING_VALUE,
                valueSupplier);

            Ref<MetricListener> listenerRef = new Ref<>();

            doAnswer(invocation -> {
                MetricListener listener = (MetricListener)invocation.getArguments()[0];
                listenerRef.setValue(listener);
                listener.metricInstanceAdded(instance);
                return null;
            }).when(cachingStringVar).addListener(any());

            reporter.cachingStringVarAdded(cachingStringVar);
            assertThat(attrValue("cachingStringVar.a.b", "value"), is("V_1"));
            assertThat(attrValue("cachingStringVar.a.b", "value"), is("V_2"));
            listenerRef.value().metricInstanceRemoved(instance);
            reporter.cachingStringVarRemoved(cachingStringVar);
        }
    }

    @Test
    public void counter() {
        for (int i = 0; i < 2; ++i) {
            Counter counter = mock(Counter.class);

            MeasurableValueProvider<TestCounterImpl> countProvider_1 = mock(MeasurableValueProvider.class);
            when(countProvider_1.valueFor(any())).thenReturn(1L, 2L);

            TestCounterInstance instance_1 = new TestCounterInstance(
                withName("counter", "a", "b"),
                emptyList(),
                true,
                false,
                false,
                Map.of(COUNT, countProvider_1),
                new TestCounterImpl());

            MeasurableValueProvider<TestCounterImpl> countProvider_2 = mock(MeasurableValueProvider.class);
            when(countProvider_2.valueFor(any())).thenReturn(3L, 4L);

            TestCounterInstance instance_2 = new TestCounterInstance(
                withName("counter", "c", "d"),
                List.of(LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")),
                false,
                false,
                true,
                Map.of(COUNT, countProvider_2),
                new TestCounterImpl());

            Ref<MetricListener> listenerRef = new Ref<>();

            doAnswer(invocation -> {
                MetricListener listener = (MetricListener)invocation.getArguments()[0];
                listenerRef.setValue(listener);
                listener.metricInstanceAdded(instance_1);
                listener.metricInstanceAdded(instance_2);
                return null;
            }).when(counter).addListener(any());

            reporter.counterAdded(counter);
            assertThat(longAttrValue("counter.a.b", "count"), is(1L));
            assertThat(longAttrValue("counter.a.b", "count"), is(2L));
            assertThat(longAttrValue("counter.c.d", "count", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(3L));
            assertThat(longAttrValue("counter.c.d", "count", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(4L));
            listenerRef.value().metricInstanceRemoved(instance_1);
            listenerRef.value().metricInstanceRemoved(instance_2);
            reporter.counterRemoved(counter);
        }
    }

    @Test
    public void rate() {
        for (int i = 0; i < 2; ++i) {
            Rate rate = mock(Rate.class);

            MeasurableValueProvider<TestRateImpl> countProvider_1 = mock(MeasurableValueProvider.class);
            when(countProvider_1.valueFor(any())).thenReturn(1L, 2L);

            TestRateInstance instance_1 = new TestRateInstance(
                withName("rate", "a", "b"),
                emptyList(),
                true,
                false,
                false,
                Map.of(COUNT, countProvider_1),
                new TestRateImpl());

            MeasurableValueProvider<TestRateImpl> countProvider_2 = mock(MeasurableValueProvider.class);
            when(countProvider_2.valueFor(any())).thenReturn(3L, 4L);

            MeasurableValueProvider<TestRateImpl> meanProvider_2 = mock(MeasurableValueProvider.class);
            when(meanProvider_2.valueFor(any())).thenReturn(5.0, 6.0);

            MeasurableValueProvider<TestRateImpl> minute_1_provider_2 = mock(MeasurableValueProvider.class);
            when(minute_1_provider_2.valueFor(any())).thenReturn(7.0, 8.0);

            MeasurableValueProvider<TestRateImpl> minute_5_provider_2 = mock(MeasurableValueProvider.class);
            when(minute_5_provider_2.valueFor(any())).thenReturn(9.0, 10.0);

            MeasurableValueProvider<TestRateImpl> minute_15_provider_2 = mock(MeasurableValueProvider.class);
            when(minute_15_provider_2.valueFor(any())).thenReturn(11.0, 12.0);

            MeasurableValueProvider<TestRateImpl> unitProvider_2 = mock(MeasurableValueProvider.class);
            when(unitProvider_2.valueFor(any())).thenReturn("events/hours");

            TestRateInstance instance_2 = new TestRateInstance(
                withName("rate", "c", "d"),
                List.of(LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")),
                false,
                false,
                true,
                Map.of(
                    COUNT, countProvider_2,
                    MEAN_RATE, meanProvider_2,
                    ONE_MINUTE_RATE, minute_1_provider_2,
                    FIVE_MINUTES_RATE, minute_5_provider_2,
                    FIFTEEN_MINUTES_RATE, minute_15_provider_2,
                    RATE_UNIT, unitProvider_2),
                new TestRateImpl());

            Ref<MetricListener> listenerRef = new Ref<>();

            doAnswer(invocation -> {
                MetricListener listener = (MetricListener)invocation.getArguments()[0];
                listenerRef.setValue(listener);
                listener.metricInstanceAdded(instance_1);
                listener.metricInstanceAdded(instance_2);
                return null;
            }).when(rate).addListener(any());

            reporter.rateAdded(rate);

            assertThat(longAttrValue("rate.a.b", "count"), is(1L));
            assertThat(longAttrValue("rate.a.b", "count"), is(2L));
            assertFalse(attrExists("rate.a.b", "rate.mean"));
            assertFalse(attrExists("rate.a.b", "rate.1_minute"));
            assertFalse(attrExists("rate.a.b", "rate.5_minutes"));
            assertFalse(attrExists("rate.a.b", "rate.15_minutes"));
            assertFalse(attrExists("rate.a.b", "rate.unit"));

            assertThat(longAttrValue("rate.c.d", "count", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(3L));
            assertThat(longAttrValue("rate.c.d", "count", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(4L));

            assertThat(doubleAttrValue("rate.c.d", "rate.mean", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(5.0));
            assertThat(doubleAttrValue("rate.c.d", "rate.mean", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(6.0));

            assertThat(doubleAttrValue("rate.c.d", "rate.1_minute", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(7.0));
            assertThat(doubleAttrValue("rate.c.d", "rate.1_minute", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(8.0));

            assertThat(doubleAttrValue("rate.c.d", "rate.5_minutes", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(9.0));
            assertThat(doubleAttrValue("rate.c.d", "rate.5_minutes", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(10.0));

            assertThat(doubleAttrValue("rate.c.d", "rate.15_minutes", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(11.0));
            assertThat(doubleAttrValue("rate.c.d", "rate.15_minutes", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(12.0));

            assertThat(stringAttrValue("rate.c.d", "rate.unit", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is("events/hours"));

            listenerRef.value().metricInstanceRemoved(instance_1);
            listenerRef.value().metricInstanceRemoved(instance_2);
            reporter.rateRemoved(rate);
        }
    }

    @Test
    public void histogram() {
        for (int i = 0; i < 2; ++i) {
            Histogram histogram = mock(Histogram.class);

            MeasurableValueProvider<TestRateImpl> countProvider_1 = mock(MeasurableValueProvider.class);
            when(countProvider_1.valueFor(any())).thenReturn(1L, 2L);

            TestRateInstance instance_1 = new TestRateInstance(
                withName("histogram", "a", "b"),
                emptyList(),
                true,
                false,
                false,
                Map.of(COUNT, countProvider_1),
                new TestRateImpl());

            MeasurableValueProvider<TestHistogramImpl> countProvider_2 = mock(MeasurableValueProvider.class);
            when(countProvider_2.valueFor(any())).thenReturn(3L, 4L);

            MeasurableValueProvider<TestHistogramImpl> minProvider_2 = mock(MeasurableValueProvider.class);
            when(minProvider_2.valueFor(any())).thenReturn(5.0, 6.0);

            MeasurableValueProvider<TestHistogramImpl> maxProvider_2 = mock(MeasurableValueProvider.class);
            when(maxProvider_2.valueFor(any())).thenReturn(7.0, 8.0);

            MeasurableValueProvider<TestHistogramImpl> meanProvider_2 = mock(MeasurableValueProvider.class);
            when(meanProvider_2.valueFor(any())).thenReturn(9.0, 10.0);

            MeasurableValueProvider<TestHistogramImpl> standardDeviationProvider_2 = mock(MeasurableValueProvider.class);
            when(standardDeviationProvider_2.valueFor(any())).thenReturn(11.0, 12.0);

            MeasurableValueProvider<TestHistogramImpl> percentile_50_provider_2 = mock(MeasurableValueProvider.class);
            when(percentile_50_provider_2.valueFor(any())).thenReturn(13.0, 14.0);

            MeasurableValueProvider<TestHistogramImpl> percentile_75_provider_2 = mock(MeasurableValueProvider.class);
            when(percentile_75_provider_2.valueFor(any())).thenReturn(15.0, 16.0);

            TestHistogramInstance instance_2 = new TestHistogramInstance(
                withName("histogram", "c", "d"),
                List.of(LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")),
                false,
                false,
                true,
                Map.of(
                    COUNT, countProvider_2,
                    Histogram.MIN, minProvider_2,
                    Histogram.MAX, maxProvider_2,
                    Histogram.MEAN, meanProvider_2,
                    Histogram.STANDARD_DEVIATION, standardDeviationProvider_2,
                    Histogram.PERCENTILE_50, percentile_50_provider_2,
                    Histogram.PERCENTILE_75, percentile_75_provider_2),
                new TestHistogramImpl());

            Ref<MetricListener> listenerRef = new Ref<>();

            doAnswer(invocation -> {
                MetricListener listener = (MetricListener)invocation.getArguments()[0];
                listenerRef.setValue(listener);
                listener.metricInstanceAdded(instance_1);
                listener.metricInstanceAdded(instance_2);
                return null;
            }).when(histogram).addListener(any());

            reporter.histogramAdded(histogram);

            assertThat(longAttrValue("histogram.a.b", "count"), is(1L));
            assertThat(longAttrValue("histogram.a.b", "count"), is(2L));
            assertFalse(attrExists("histogram.a.b", "min"));
            assertFalse(attrExists("histogram.a.b", "max"));
            assertFalse(attrExists("histogram.a.b", "mean"));
            assertFalse(attrExists("histogram.a.b", "stdDev"));
            assertFalse(attrExists("histogram.a.b", "percentile_50"));
            assertFalse(attrExists("histogram.a.b", "percentile_75"));

            assertThat(longAttrValue("histogram.c.d", "count", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(3L));
            assertThat(longAttrValue("histogram.c.d", "count", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(4L));

            assertThat(doubleAttrValue("histogram.c.d", "min", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(5.0));
            assertThat(doubleAttrValue("histogram.c.d", "min", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(6.0));

            assertThat(doubleAttrValue("histogram.c.d", "max", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(7.0));
            assertThat(doubleAttrValue("histogram.c.d", "max", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(8.0));

            assertThat(doubleAttrValue("histogram.c.d", "mean", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(9.0));
            assertThat(doubleAttrValue("histogram.c.d", "mean", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(10.0));

            assertThat(doubleAttrValue("histogram.c.d", "stdDev", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(11.0));
            assertThat(doubleAttrValue("histogram.c.d", "stdDev", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(12.0));

            assertThat(doubleAttrValue("histogram.c.d", "50_percentile", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(13.0));
            assertThat(doubleAttrValue("histogram.c.d", "50_percentile", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(14.0));

            assertThat(doubleAttrValue("histogram.c.d", "75_percentile", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(15.0));
            assertThat(doubleAttrValue("histogram.c.d", "75_percentile", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(16.0));

            listenerRef.value().metricInstanceRemoved(instance_1);
            listenerRef.value().metricInstanceRemoved(instance_2);
            reporter.histogramRemoved(histogram);
        }
    }

    @Test
    public void timer() {
        for (int i = 0; i < 2; ++i) {
            Timer timer = mock(Timer.class);

            MeasurableValueProvider<TestTimerImpl> countProvider_1 = mock(MeasurableValueProvider.class);
            when(countProvider_1.valueFor(any())).thenReturn(1L, 2L);

            TestTimerInstance instance_1 = new TestTimerInstance(
                withName("timer", "a", "b"),
                emptyList(),
                true,
                false,
                false,
                Map.of(COUNT, countProvider_1),
                new TestTimerImpl());

            MeasurableValueProvider<TestTimerImpl> countProvider_2 = mock(MeasurableValueProvider.class);
            when(countProvider_2.valueFor(any())).thenReturn(3L, 4L);

            // rate
            MeasurableValueProvider<TestTimerImpl> rateMeanProvider_2 = mock(MeasurableValueProvider.class);
            when(rateMeanProvider_2.valueFor(any())).thenReturn(5.0, 6.0);

            MeasurableValueProvider<TestTimerImpl> rateMinute_1_provider_2 = mock(MeasurableValueProvider.class);
            when(rateMinute_1_provider_2.valueFor(any())).thenReturn(7.0, 8.0);

            MeasurableValueProvider<TestTimerImpl> rateMinute_5_provider_2 = mock(MeasurableValueProvider.class);
            when(rateMinute_5_provider_2.valueFor(any())).thenReturn(9.0, 10.0);

            MeasurableValueProvider<TestTimerImpl> rateMinute_15_provider_2 = mock(MeasurableValueProvider.class);
            when(rateMinute_15_provider_2.valueFor(any())).thenReturn(11.0, 12.0);

            MeasurableValueProvider<TestTimerImpl> rateUnitProvider_2 = mock(MeasurableValueProvider.class);
            when(rateUnitProvider_2.valueFor(any())).thenReturn("events/hours");

            // duration
            MeasurableValueProvider<TestTimerImpl> durationMinProvider_2 = mock(MeasurableValueProvider.class);
            when(durationMinProvider_2.valueFor(any())).thenReturn(13.0, 14.0);

            MeasurableValueProvider<TestTimerImpl> durationMaxProvider_2 = mock(MeasurableValueProvider.class);
            when(durationMaxProvider_2.valueFor(any())).thenReturn(15.0, 16.0);

            MeasurableValueProvider<TestTimerImpl> durationMeanProvider_2 = mock(MeasurableValueProvider.class);
            when(durationMeanProvider_2.valueFor(any())).thenReturn(17.0, 18.0);

            MeasurableValueProvider<TestTimerImpl> durationStandardDeviationProvider_2 = mock(MeasurableValueProvider.class);
            when(durationStandardDeviationProvider_2.valueFor(any())).thenReturn(19.0, 20.0);

            MeasurableValueProvider<TestTimerImpl> durationPercentile_50_provider_2 = mock(MeasurableValueProvider.class);
            when(durationPercentile_50_provider_2.valueFor(any())).thenReturn(21.0, 22.0);

            MeasurableValueProvider<TestTimerImpl> durationPercentile_75_provider_2 = mock(MeasurableValueProvider.class);
            when(durationPercentile_75_provider_2.valueFor(any())).thenReturn(23.0, 24.0);

            MeasurableValueProvider<TestTimerImpl> durationUnitProvider_2 = mock(MeasurableValueProvider.class);
            when(durationUnitProvider_2.valueFor(any())).thenReturn("days");

            TestTimerInstance instance_2 = new TestTimerInstance(
                withName("timer", "c", "d"),
                List.of(LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")),
                false,
                false,
                true,
                Map.ofEntries(
                    Pair.of(COUNT, countProvider_2),
                    Pair.of(Rate.MEAN_RATE, rateMeanProvider_2),
                    Pair.of(Rate.ONE_MINUTE_RATE, rateMinute_1_provider_2),
                    Pair.of(Rate.FIVE_MINUTES_RATE, rateMinute_5_provider_2),
                    Pair.of(Rate.FIFTEEN_MINUTES_RATE, rateMinute_15_provider_2),
                    Pair.of(Rate.RATE_UNIT, rateUnitProvider_2),
                    Pair.of(Histogram.MIN, durationMinProvider_2),
                    Pair.of(Histogram.MAX, durationMaxProvider_2),
                    Pair.of(Histogram.MEAN, durationMeanProvider_2),
                    Pair.of(Histogram.STANDARD_DEVIATION, durationStandardDeviationProvider_2),
                    Pair.of(Histogram.PERCENTILE_50, durationPercentile_50_provider_2),
                    Pair.of(Histogram.PERCENTILE_75, durationPercentile_75_provider_2),
                    Pair.of(Timer.DURATION_UNIT, durationUnitProvider_2)),
                new TestTimerImpl());

            Ref<MetricListener> listenerRef = new Ref<>();

            doAnswer(invocation -> {
                MetricListener listener = (MetricListener)invocation.getArguments()[0];
                listenerRef.setValue(listener);
                listener.metricInstanceAdded(instance_1);
                listener.metricInstanceAdded(instance_2);
                return null;
            }).when(timer).addListener(any());

            reporter.timerAdded(timer);

            assertThat(longAttrValue("timer.a.b", "count"), is(1L));
            assertThat(longAttrValue("timer.a.b", "count"), is(2L));

            assertFalse(attrExists("timer.a.b", "rate.mean"));
            assertFalse(attrExists("timer.a.b", "rate.1_minute"));
            assertFalse(attrExists("timer.a.b", "rate.5_minutes"));
            assertFalse(attrExists("timer.a.b", "rate.15_minutes"));
            assertFalse(attrExists("timer.a.b", "rate.unit"));

            assertFalse(attrExists("timer.a.b", "duration.min"));
            assertFalse(attrExists("timer.a.b", "duration.max"));
            assertFalse(attrExists("timer.a.b", "duration.mean"));
            assertFalse(attrExists("timer.a.b", "duration.stdDev"));
            assertFalse(attrExists("timer.a.b", "duration.percentile_50"));
            assertFalse(attrExists("timer.a.b", "duration.percentile_75"));
            assertFalse(attrExists("timer.a.b", "duration.unit"));

            assertThat(longAttrValue("timer.c.d", "count", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(3L));
            assertThat(longAttrValue("timer.c.d", "count", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(4L));

            // rate
            assertThat(doubleAttrValue("timer.c.d", "rate.mean", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(5.0));
            assertThat(doubleAttrValue("timer.c.d", "rate.mean", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(6.0));

            assertThat(doubleAttrValue("timer.c.d", "rate.1_minute", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(7.0));
            assertThat(doubleAttrValue("timer.c.d", "rate.1_minute", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(8.0));

            assertThat(doubleAttrValue("timer.c.d", "rate.5_minutes", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(9.0));
            assertThat(doubleAttrValue("timer.c.d", "rate.5_minutes", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(10.0));

            assertThat(doubleAttrValue("timer.c.d", "rate.15_minutes", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(11.0));
            assertThat(doubleAttrValue("timer.c.d", "rate.15_minutes", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(12.0));

            assertThat(stringAttrValue("timer.c.d", "rate.unit", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is("events/hours"));

            // duration
            assertThat(doubleAttrValue("timer.c.d", "duration.min", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(13.0));
            assertThat(doubleAttrValue("timer.c.d", "duration.min", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(14.0));

            assertThat(doubleAttrValue("timer.c.d", "duration.max", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(15.0));
            assertThat(doubleAttrValue("timer.c.d", "duration.max", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(16.0));

            assertThat(doubleAttrValue("timer.c.d", "duration.mean", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(17.0));
            assertThat(doubleAttrValue("timer.c.d", "duration.mean", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(18.0));

            assertThat(doubleAttrValue("timer.c.d", "duration.stdDev", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(19.0));
            assertThat(doubleAttrValue("timer.c.d", "duration.stdDev", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(20.0));

            assertThat(doubleAttrValue("timer.c.d", "duration.50_percentile", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(21.0));
            assertThat(doubleAttrValue("timer.c.d", "duration.50_percentile", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(22.0));

            assertThat(doubleAttrValue("timer.c.d", "duration.75_percentile", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(23.0));
            assertThat(doubleAttrValue("timer.c.d", "duration.75_percentile", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is(24.0));

            assertThat(stringAttrValue("timer.c.d", "duration.unit", LABEL_1.value("l_1_value"), LABEL_2.value("l_2_value")), is("days"));

            listenerRef.value().metricInstanceRemoved(instance_1);
            listenerRef.value().metricInstanceRemoved(instance_2);
            reporter.timerRemoved(timer);
        }
    }

    boolean attrExists(String mBeanName, String mBeanAttrName, LabelValue... labelValues) {
        try {
            return mBeanServer.getAttribute(objectName(mBeanName, labelValues), mBeanAttrName) != null;
        } catch (AttributeNotFoundException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    long longAttrValue(String mBeanName, String mBeanAttrName, LabelValue... labelValues) {
        return (long)attrValue(mBeanName, mBeanAttrName, labelValues);
    }

    double doubleAttrValue(String mBeanName, String mBeanAttrName, LabelValue... labelValues) {
        return (double)attrValue(mBeanName, mBeanAttrName, labelValues);
    }

    String stringAttrValue(String mBeanName, String mBeanAttrName, LabelValue... labelValues) {
        return (String)attrValue(mBeanName, mBeanAttrName, labelValues);
    }

    Object attrValue(String mBeanName, String mBeanAttrName, LabelValue... labelValues) {
        try {
            return mBeanServer.getAttribute(objectName(mBeanName, labelValues), mBeanAttrName);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    ObjectName objectName(String mBeanName, LabelValue... labelValues) {
        try {
            if (labelValues.length == 0) {
                return new ObjectName("metrics", "name", mBeanName);
            } else {
                StringBuilder builder = new StringBuilder("metrics").append(":name=").append(mBeanName);
                List.of(labelValues).forEach(lv -> builder.append(',').append(escape(lv.label().name())).append('=').append(escape(lv.value())));
                return new ObjectName(builder.toString());
            }
        } catch (MalformedObjectNameException exception) {
            throw new RuntimeException(exception);
        }
    }

    private String escape(String v) {
        return v.replaceAll("[\\s*?,=:\\\\]", "_");
    }
}