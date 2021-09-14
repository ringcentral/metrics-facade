package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.AbstractMeter.MeasurableValueProvider;
import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.meter.*;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.timer.Timer;
import com.ringcentral.platform.metrics.var.DefaultVarInstance;
import com.ringcentral.platform.metrics.var.doubleVar.*;
import com.ringcentral.platform.metrics.var.longVar.*;
import com.ringcentral.platform.metrics.var.objectVar.*;
import com.ringcentral.platform.metrics.var.stringVar.*;
import org.junit.*;

import java.util.*;
import java.util.function.*;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.dimensionValues;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.names.MetricName.*;
import static com.ringcentral.platform.metrics.names.MetricNameMask.*;
import static com.ringcentral.platform.metrics.predicates.CompositeMetricNamedPredicateBuilder.forMetrics;
import static com.ringcentral.platform.metrics.predicates.DefaultMetricInstancePredicate.forMetricInstancesMatching;
import static com.ringcentral.platform.metrics.rate.Rate.*;
import static com.ringcentral.platform.metrics.samples.DefaultInstanceSampleSpec.instanceSampleSpec;
import static com.ringcentral.platform.metrics.samples.DefaultSampleSpec.sampleSpec;
import static com.ringcentral.platform.metrics.samples.SampleTypes.*;
import static com.ringcentral.platform.metrics.timer.Timer.DURATION_UNIT;
import static com.ringcentral.platform.metrics.utils.CollectionUtils.linkedHashMapOf;
import static com.ringcentral.platform.metrics.var.longVar.LongVar.LONG_VALUE;
import static com.ringcentral.platform.metrics.var.objectVar.ObjectVar.OBJECT_VALUE;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class DefaultInstanceSamplesProviderTest {

    static final MetricDimension DIMENSION_1 = new MetricDimension("dimension_1");
    static final MetricDimension DIMENSION_2 = new MetricDimension("dimension_2");
    static final MetricDimension DIMENSION_3 = new MetricDimension("dimension_3");

    MetricRegistry registry = mock(MetricRegistry.class);
    Map<MetricKey, Metric> metrics = new LinkedHashMap<>();
    DefaultInstanceSamplesProvider provider = new DefaultInstanceSamplesProvider(registry);

    @Before
    public void before() {
        when(registry.metrics()).thenReturn(metrics);
    }

    @Test
    public void objectVar() {
        ObjectVar objectVar = mock(ObjectVar.class);
        metrics.put(name("objectVar", "a", "b"), objectVar);

        Supplier<Object> valueSupplier = mock(Supplier.class);
        when(valueSupplier.get()).thenReturn(1L);

        MetricInstance instance = new DefaultVarInstance<>(
            withName("objectVar", "a", "b"),
            emptyList(),
            true,
            false,
            OBJECT_VALUE,
            valueSupplier);

        when(objectVar.iterator()).thenReturn(List.of(instance).iterator());
        Set<InstanceSample<DefaultSample>> instanceSamples = provider.instanceSamples();
        assertThat(instanceSamples.size(), is(0));
    }

    @Test
    public void cachingObjectVar() {
        CachingObjectVar cachingObjectVar = mock(CachingObjectVar.class);
        metrics.put(name("cachingObjectVar", "a", "b"), cachingObjectVar);

        Supplier<Object> valueSupplier = mock(Supplier.class);
        when(valueSupplier.get()).thenReturn(1L);

        MetricInstance instance = new DefaultVarInstance<>(
            withName("objectVar", "a", "b"),
            emptyList(),
            true,
            false,
            OBJECT_VALUE,
            valueSupplier);

        when(cachingObjectVar.iterator()).thenReturn(List.of(instance).iterator());
        Set<InstanceSample<DefaultSample>> instanceSamples = provider.instanceSamples();
        assertThat(instanceSamples.size(), is(0));
    }

    @Test
    public void longVar() {
        // rewriting names
        DefaultInstanceSampleSpecModsProvider instanceSampleSpecModsProvider = new DefaultInstanceSampleSpecModsProvider();

        instanceSampleSpecModsProvider.addMod(
            forMetricInstancesMatching(nameMask("longVar.a.b.c.d.e"), instance -> instance.hasDimension(DIMENSION_3)),
            (metric, instance) -> instanceSampleSpec()
                .name(instance.name().withNewPart(instance.valueOf(DIMENSION_3), 1))
                .dimensionValues(instance.dimensionValuesWithout(DIMENSION_3)));

        // move certain values to DELTA bucket
        DefaultSampleSpecModsProvider sampleSpecModsProvider = new DefaultSampleSpecModsProvider();

        sampleSpecModsProvider.addMod(
            forMetrics()
                .including(metricWithName("longVar.a.b.c"))
                .including(metricWithName("longVar.a.b.c.d")),
            (instanceSampleSpec, instance, measurableValues, measurable) -> sampleSpec().type(DELTA));

        provider = new DefaultInstanceSamplesProvider(instanceSampleSpecModsProvider, sampleSpecModsProvider, registry);

        LongVar longVar = mock(LongVar.class);
        doCallRealMethod().when(longVar).forEach(any(Consumer.class));
        metrics.put(name("longVar", "a", "b"), longVar);

        Supplier<Long> valueSupplier = mock(Supplier.class);
        when(valueSupplier.get()).thenReturn(1L, 2L, 3L, 4L);

        MetricInstance instance_1 = new DefaultVarInstance<>(
            withName("longVar", "a", "b"),
            emptyList(),
            true,
            false,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_2 = new DefaultVarInstance<>(
            withName("longVar", "a", "b", "c"),
            dimensionValues(DIMENSION_1.value("1"), DIMENSION_2.value("2")).list(),
            true,
            false,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_3 = new DefaultVarInstance<>(
            withName("longVar", "a", "b", "c", "d"),
            emptyList(),
            true,
            true,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_4 = new DefaultVarInstance<>(
            withName("longVar", "a", "b", "c", "d", "e"),
            dimensionValues(
                DIMENSION_1.value("1"),
                DIMENSION_2.value("2"),
                DIMENSION_3.value("3")).list(),
            false,
            false,
            LONG_VALUE,
            valueSupplier);

        when(longVar.iterator()).thenReturn(List.of(instance_1, instance_2, instance_3, instance_4).iterator());
        Set<InstanceSample<DefaultSample>> instanceSamples = provider.instanceSamples();

        assertThat(instanceSamples.size(), is(4));
        Iterator<InstanceSample<DefaultSample>> instanceSamplesIter = instanceSamples.iterator();

        InstanceSample<DefaultSample> instanceSample = instanceSamplesIter.next();
        List<DefaultSample> samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        DefaultSample sample = samples.get(0);
        assertThat(sample.name(), is("longVar.a.b.value"));
        assertThat(sample.value(), is(1L));
        assertThat(sample.type(), is(INSTANT));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("longVar.a.b.c.1.2.value"));
        assertThat(sample.value(), is(2L));
        assertThat(sample.type(), is(DELTA));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("longVar.a.b.c.d.value"));
        assertThat(sample.value(), is(3L));
        assertThat(sample.type(), is(DELTA));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("longVar.3.a.b.c.d.e.1.2.value"));
        assertThat(sample.value(), is(4L));
        assertThat(sample.type(), is(INSTANT));
    }

    @Test
    public void cachingLongVar() {
        // disable instance
        DefaultInstanceSampleSpecModsProvider instanceSampleSpecModsProvider = new DefaultInstanceSampleSpecModsProvider();

        instanceSampleSpecModsProvider.addMod(
            forMetricInstancesMatching(nameMask("cachingLongVar.a.b.c.d.e")),
            (metric, instance) -> instanceSampleSpec().disable());

        // disable certain samples
        DefaultSampleSpecModsProvider sampleSpecModsProvider = new DefaultSampleSpecModsProvider();

        sampleSpecModsProvider.addMod(
            forMetrics()
                .including(metricWithName("cachingLongVar.a.b.c"))
                .including(metricWithName("cachingLongVar.a.b.c.d")),
            (instanceSampleSpec, instance, measurableValues, measurable) -> sampleSpec().disable());

        provider = new DefaultInstanceSamplesProvider(instanceSampleSpecModsProvider, sampleSpecModsProvider, registry);

        CachingLongVar longVar = mock(CachingLongVar.class);
        doCallRealMethod().when(longVar).forEach(any(Consumer.class));
        metrics.put(name("cachingLongVar", "a", "b"), longVar);

        Supplier<Long> valueSupplier = mock(Supplier.class);
        when(valueSupplier.get()).thenReturn(1L, 2L, 3L, 4L);

        MetricInstance instance_1 = new DefaultVarInstance<>(
            withName("cachingLongVar", "a", "b"),
            emptyList(),
            true,
            false,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_2 = new DefaultVarInstance<>(
            withName("cachingLongVar", "a", "b", "c"),
            dimensionValues(DIMENSION_1.value("1"), DIMENSION_2.value("2")).list(),
            true,
            false,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_3 = new DefaultVarInstance<>(
            withName("cachingLongVar", "a", "b", "c", "d"),
            emptyList(),
            true,
            true,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_4 = new DefaultVarInstance<>(
            withName("cachingLongVar", "a", "b", "c", "d", "e"),
            dimensionValues(
                DIMENSION_1.value("1"),
                DIMENSION_2.value("2"),
                DIMENSION_3.value("3")).list(),
            false,
            false,
            LONG_VALUE,
            valueSupplier);

        when(longVar.iterator()).thenReturn(List.of(instance_1, instance_2, instance_3, instance_4).iterator());
        Set<InstanceSample<DefaultSample>> instanceSamples = provider.instanceSamples();

        assertThat(instanceSamples.size(), is(1));
        Iterator<InstanceSample<DefaultSample>> instanceSamplesIter = instanceSamples.iterator();

        InstanceSample<DefaultSample> instanceSample = instanceSamplesIter.next();
        List<DefaultSample> samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        DefaultSample sample = samples.get(0);
        assertThat(sample.name(), is("cachingLongVar.a.b.value"));
        assertThat(sample.value(), is(1L));
        assertThat(sample.type(), is(INSTANT));
    }

    @Test
    public void doubleVar() {
        DoubleVar doubleVar = mock(DoubleVar.class);
        doCallRealMethod().when(doubleVar).forEach(any(Consumer.class));
        metrics.put(name("doubleVar", "a", "b"), doubleVar);

        Supplier<Double> valueSupplier = mock(Supplier.class);
        when(valueSupplier.get()).thenReturn(1.0, 2.0, 3.0, 4.0);

        MetricInstance instance_1 = new DefaultVarInstance<>(
            withName("doubleVar", "a", "b"),
            emptyList(),
            true,
            false,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_2 = new DefaultVarInstance<>(
            withName("doubleVar", "a", "b", "c"),
            dimensionValues(DIMENSION_1.value("1"), DIMENSION_2.value("2")).list(),
            true,
            false,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_3 = new DefaultVarInstance<>(
            withName("doubleVar", "a", "b", "c", "d"),
            emptyList(),
            true,
            true,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_4 = new DefaultVarInstance<>(
            withName("doubleVar", "a", "b", "c", "d", "e"),
            dimensionValues(
                DIMENSION_1.value("1"),
                DIMENSION_2.value("2"),
                DIMENSION_3.value("3")).list(),
            false,
            false,
            LONG_VALUE,
            valueSupplier);

        when(doubleVar.iterator()).thenReturn(List.of(instance_1, instance_2, instance_3, instance_4).iterator());
        Set<InstanceSample<DefaultSample>> instanceSamples = provider.instanceSamples();

        assertThat(instanceSamples.size(), is(4));
        Iterator<InstanceSample<DefaultSample>> instanceSamplesIter = instanceSamples.iterator();

        InstanceSample<DefaultSample> instanceSample = instanceSamplesIter.next();
        List<DefaultSample> samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        DefaultSample sample = samples.get(0);
        assertThat(sample.name(), is("doubleVar.a.b.value"));
        assertThat(sample.value(), is(1.0));
        assertThat(sample.type(), is(INSTANT));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("doubleVar.a.b.c.1.2.value"));
        assertThat(sample.value(), is(2.0));
        assertThat(sample.type(), is(INSTANT));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("doubleVar.a.b.c.d.value"));
        assertThat(sample.value(), is(3.0));
        assertThat(sample.type(), is(INSTANT));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("doubleVar.a.b.c.d.e.1.2.3.value"));
        assertThat(sample.value(), is(4.0));
        assertThat(sample.type(), is(INSTANT));
    }

    @Test
    public void cachingDoubleVar() {
        CachingDoubleVar doubleVar = mock(CachingDoubleVar.class);
        doCallRealMethod().when(doubleVar).forEach(any(Consumer.class));
        metrics.put(name("cachingDoubleVar", "a", "b"), doubleVar);

        Supplier<Double> valueSupplier = mock(Supplier.class);
        when(valueSupplier.get()).thenReturn(1.0, 2.0, 3.0, 4.0);

        MetricInstance instance_1 = new DefaultVarInstance<>(
            withName("cachingDoubleVar", "a", "b"),
            emptyList(),
            true,
            false,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_2 = new DefaultVarInstance<>(
            withName("cachingDoubleVar", "a", "b", "c"),
            dimensionValues(DIMENSION_1.value("1"), DIMENSION_2.value("2")).list(),
            true,
            false,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_3 = new DefaultVarInstance<>(
            withName("cachingDoubleVar", "a", "b", "c", "d"),
            emptyList(),
            true,
            true,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_4 = new DefaultVarInstance<>(
            withName("cachingDoubleVar", "a", "b", "c", "d", "e"),
            dimensionValues(
                DIMENSION_1.value("1"),
                DIMENSION_2.value("2"),
                DIMENSION_3.value("3")).list(),
            false,
            false,
            LONG_VALUE,
            valueSupplier);

        when(doubleVar.iterator()).thenReturn(List.of(instance_1, instance_2, instance_3, instance_4).iterator());
        Set<InstanceSample<DefaultSample>> instanceSamples = provider.instanceSamples();

        assertThat(instanceSamples.size(), is(4));
        Iterator<InstanceSample<DefaultSample>> instanceSamplesIter = instanceSamples.iterator();

        InstanceSample<DefaultSample> instanceSample = instanceSamplesIter.next();
        List<DefaultSample> samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        DefaultSample sample = samples.get(0);
        assertThat(sample.name(), is("cachingDoubleVar.a.b.value"));
        assertThat(sample.value(), is(1.0));
        assertThat(sample.type(), is(INSTANT));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("cachingDoubleVar.a.b.c.1.2.value"));
        assertThat(sample.value(), is(2.0));
        assertThat(sample.type(), is(INSTANT));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("cachingDoubleVar.a.b.c.d.value"));
        assertThat(sample.value(), is(3.0));
        assertThat(sample.type(), is(INSTANT));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("cachingDoubleVar.a.b.c.d.e.1.2.3.value"));
        assertThat(sample.value(), is(4.0));
        assertThat(sample.type(), is(INSTANT));
    }

    @Test
    public void stringVar() {
        StringVar stringVar = mock(StringVar.class);
        doCallRealMethod().when(stringVar).forEach(any(Consumer.class));
        metrics.put(name("stringVar", "a", "b"), stringVar);

        Supplier<String> valueSupplier = mock(Supplier.class);
        when(valueSupplier.get()).thenReturn("1", "2", "3", "4");

        MetricInstance instance_1 = new DefaultVarInstance<>(
            withName("stringVar", "a", "b"),
            emptyList(),
            true,
            false,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_2 = new DefaultVarInstance<>(
            withName("stringVar", "a", "b", "c"),
            dimensionValues(DIMENSION_1.value("1"), DIMENSION_2.value("2")).list(),
            true,
            false,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_3 = new DefaultVarInstance<>(
            withName("stringVar", "a", "b", "c", "d"),
            emptyList(),
            true,
            true,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_4 = new DefaultVarInstance<>(
            withName("stringVar", "a", "b", "c", "d", "e"),
            dimensionValues(
                DIMENSION_1.value("1"),
                DIMENSION_2.value("2"),
                DIMENSION_3.value("3")).list(),
            false,
            false,
            LONG_VALUE,
            valueSupplier);

        when(stringVar.iterator()).thenReturn(List.of(instance_1, instance_2, instance_3, instance_4).iterator());
        Set<InstanceSample<DefaultSample>> instanceSamples = provider.instanceSamples();

        assertThat(instanceSamples.size(), is(4));
        Iterator<InstanceSample<DefaultSample>> instanceSamplesIter = instanceSamples.iterator();

        InstanceSample<DefaultSample> instanceSample = instanceSamplesIter.next();
        List<DefaultSample> samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        DefaultSample sample = samples.get(0);
        assertThat(sample.name(), is("stringVar.a.b.value"));
        assertThat(sample.value(), is("1"));
        assertThat(sample.type(), is(INSTANT));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("stringVar.a.b.c.1.2.value"));
        assertThat(sample.value(), is("2"));
        assertThat(sample.type(), is(INSTANT));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("stringVar.a.b.c.d.value"));
        assertThat(sample.value(), is("3"));
        assertThat(sample.type(), is(INSTANT));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("stringVar.a.b.c.d.e.1.2.3.value"));
        assertThat(sample.value(), is("4"));
        assertThat(sample.type(), is(INSTANT));
    }

    @Test
    public void cachingStringVar() {
        CachingStringVar cachingStringVar = mock(CachingStringVar.class);
        doCallRealMethod().when(cachingStringVar).forEach(any(Consumer.class));
        metrics.put(name("cachingStringVar", "a", "b"), cachingStringVar);

        Supplier<String> valueSupplier = mock(Supplier.class);
        when(valueSupplier.get()).thenReturn("1", "2", "3", "4");

        MetricInstance instance_1 = new DefaultVarInstance<>(
            withName("cachingStringVar", "a", "b"),
            emptyList(),
            true,
            false,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_2 = new DefaultVarInstance<>(
            withName("cachingStringVar", "a", "b", "c"),
            dimensionValues(DIMENSION_1.value("1"), DIMENSION_2.value("2")).list(),
            true,
            false,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_3 = new DefaultVarInstance<>(
            withName("cachingStringVar", "a", "b", "c", "d"),
            emptyList(),
            true,
            true,
            LONG_VALUE,
            valueSupplier);

        MetricInstance instance_4 = new DefaultVarInstance<>(
            withName("cachingStringVar", "a", "b", "c", "d", "e"),
            dimensionValues(
                DIMENSION_1.value("1"),
                DIMENSION_2.value("2"),
                DIMENSION_3.value("3")).list(),
            false,
            false,
            LONG_VALUE,
            valueSupplier);

        when(cachingStringVar.iterator()).thenReturn(List.of(instance_1, instance_2, instance_3, instance_4).iterator());
        Set<InstanceSample<DefaultSample>> instanceSamples = provider.instanceSamples();

        assertThat(instanceSamples.size(), is(4));
        Iterator<InstanceSample<DefaultSample>> instanceSamplesIter = instanceSamples.iterator();

        InstanceSample<DefaultSample> instanceSample = instanceSamplesIter.next();
        List<DefaultSample> samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        DefaultSample sample = samples.get(0);
        assertThat(sample.name(), is("cachingStringVar.a.b.value"));
        assertThat(sample.value(), is("1"));
        assertThat(sample.type(), is(INSTANT));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("cachingStringVar.a.b.c.1.2.value"));
        assertThat(sample.value(), is("2"));
        assertThat(sample.type(), is(INSTANT));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("cachingStringVar.a.b.c.d.value"));
        assertThat(sample.value(), is("3"));
        assertThat(sample.type(), is(INSTANT));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("cachingStringVar.a.b.c.d.e.1.2.3.value"));
        assertThat(sample.value(), is("4"));
        assertThat(sample.type(), is(INSTANT));
    }

    @Test
    public void counter() {
        Counter counter = mock(Counter.class);
        doCallRealMethod().when(counter).forEach(any(Consumer.class));
        metrics.put(name("counter", "a", "b"), counter);

        MeasurableValueProvider<TestCounterImpl> countProvider = mock(MeasurableValueProvider.class);
        when(countProvider.valueFor(any())).thenReturn(1L, 2L, 3L, 4L);

        MetricInstance instance_1 = new TestCounterInstance(
            withName("counter", "a", "b"),
            emptyList(),
            true,
            false,
            false,
            Map.of(COUNT, countProvider),
            new TestCounterImpl());

        MetricInstance instance_2 = new TestCounterInstance(
            withName("counter", "a", "b", "c"),
            dimensionValues(DIMENSION_1.value("1"), DIMENSION_2.value("2")).list(),
            true,
            false,
            true,
            Map.of(COUNT, countProvider),
            new TestCounterImpl());

        MetricInstance instance_3 = new TestCounterInstance(
            withName("counter", "a", "b", "c", "d"),
            emptyList(),
            true,
            true,
            false,
            Map.of(COUNT, countProvider),
            new TestCounterImpl());

        MetricInstance instance_4 = new TestCounterInstance(
            withName("counter", "a", "b", "c", "d", "e"),
            dimensionValues(
                DIMENSION_1.value("1"),
                DIMENSION_2.value("2"),
                DIMENSION_3.value("3")).list(),
            false,
            false,
            false,
            Map.of(COUNT, countProvider),
            new TestCounterImpl());

        when(counter.iterator()).thenReturn(List.of(instance_1, instance_2, instance_3, instance_4).iterator());
        Set<InstanceSample<DefaultSample>> instanceSamples = provider.instanceSamples();

        assertThat(instanceSamples.size(), is(4));
        Iterator<InstanceSample<DefaultSample>> instanceSamplesIter = instanceSamples.iterator();

        InstanceSample<DefaultSample> instanceSample = instanceSamplesIter.next();
        List<DefaultSample> samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        DefaultSample sample = samples.get(0);
        assertThat(sample.name(), is("counter.a.b.count"));
        assertThat(sample.value(), is(1L));
        assertThat(sample.type(), is(INSTANT));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("counter.a.b.c.1.2.count"));
        assertThat(sample.value(), is(2L));
        assertThat(sample.type(), is(INSTANT));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("counter.a.b.c.d.count"));
        assertThat(sample.value(), is(3L));
        assertThat(sample.type(), is(INSTANT));

        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(1));
        sample = samples.get(0);
        assertThat(sample.name(), is("counter.a.b.c.d.e.1.2.3.count"));
        assertThat(sample.value(), is(4L));
        assertThat(sample.type(), is(INSTANT));
    }

    @Test
    public void rate() {
        Rate rate = mock(Rate.class);
        doCallRealMethod().when(rate).forEach(any(Consumer.class));
        metrics.put(name("rate", "a", "b"), rate);

        MeasurableValueProvider<TestRateImpl> countProvider = mock(MeasurableValueProvider.class);
        when(countProvider.valueFor(any())).thenReturn(1L, 2L, 3L, 4L);

        MeasurableValueProvider<TestRateImpl> meanProvider = mock(MeasurableValueProvider.class);
        when(meanProvider.valueFor(any())).thenReturn(5.0, 6.0, 7.0, 8.0);

        MeasurableValueProvider<TestRateImpl> minute_1_provider = mock(MeasurableValueProvider.class);
        when(minute_1_provider.valueFor(any())).thenReturn(9.0, 10.0, 11.0, 12.0);

        MeasurableValueProvider<TestRateImpl> minute_5_provider = mock(MeasurableValueProvider.class);
        when(minute_5_provider.valueFor(any())).thenReturn(13.0, 14.0, 15.0, 16.0);

        MeasurableValueProvider<TestRateImpl> minute_15_provider = mock(MeasurableValueProvider.class);
        when(minute_15_provider.valueFor(any())).thenReturn(17.0, 18.0, 19.0, 20.0);

        MeasurableValueProvider<TestRateImpl> unitProvider = mock(MeasurableValueProvider.class);
        when(unitProvider.valueFor(any())).thenReturn("events/hours");

        MetricInstance instance_1 = new TestRateInstance(
            withName("rate", "a", "b"),
            emptyList(),
            true,
            false,
            false,
            linkedHashMapOf(
                List.of(COUNT, MEAN_RATE, ONE_MINUTE_RATE, FIVE_MINUTES_RATE, FIFTEEN_MINUTES_RATE, RATE_UNIT),
                List.of(countProvider, meanProvider, minute_1_provider, minute_5_provider, minute_15_provider, unitProvider)),
            new TestRateImpl());

        MetricInstance instance_2 = new TestRateInstance(
            withName("rate", "a", "b", "c"),
            dimensionValues(DIMENSION_1.value("1"), DIMENSION_2.value("2")).list(),
            true,
            false,
            true,
            linkedHashMapOf(
                List.of(COUNT, MEAN_RATE, ONE_MINUTE_RATE, FIVE_MINUTES_RATE, FIFTEEN_MINUTES_RATE, RATE_UNIT),
                List.of(countProvider, meanProvider, minute_1_provider, minute_5_provider, minute_15_provider, unitProvider)),
            new TestRateImpl());

        MetricInstance instance_3 = new TestRateInstance(
            withName("rate", "a", "b", "c", "d"),
            emptyList(),
            true,
            true,
            false,
            linkedHashMapOf(
                List.of(COUNT, MEAN_RATE, ONE_MINUTE_RATE, FIVE_MINUTES_RATE, FIFTEEN_MINUTES_RATE, RATE_UNIT),
                List.of(countProvider, meanProvider, minute_1_provider, minute_5_provider, minute_15_provider, unitProvider)),
            new TestRateImpl());

        MetricInstance instance_4 = new TestRateInstance(
            withName("rate", "a", "b", "c", "d", "e"),
            dimensionValues(
                DIMENSION_1.value("1"),
                DIMENSION_2.value("2"),
                DIMENSION_3.value("3")).list(),
            false,
            false,
            false,
            linkedHashMapOf(
                List.of(COUNT, MEAN_RATE, ONE_MINUTE_RATE, FIVE_MINUTES_RATE, FIFTEEN_MINUTES_RATE, RATE_UNIT),
                List.of(countProvider, meanProvider, minute_1_provider, minute_5_provider, minute_15_provider, unitProvider)),
            new TestRateImpl());

        when(rate.iterator()).thenReturn(List.of(instance_1, instance_2, instance_3, instance_4).iterator());
        Set<InstanceSample<DefaultSample>> instanceSamples = provider.instanceSamples();

        assertThat(instanceSamples.size(), is(4));
        Iterator<InstanceSample<DefaultSample>> instanceSamplesIter = instanceSamples.iterator();

        // instance 1
        InstanceSample<DefaultSample> instanceSample = instanceSamplesIter.next();
        List<DefaultSample> samples = instanceSample.samples();
        assertThat(samples.size(), is(6));

        DefaultSample sample = samples.get(0);
        assertThat(sample.name(), is("rate.a.b.count"));
        assertThat(sample.value(), is(1L));
        assertThat(sample.type(), is(DELTA));

        sample = samples.get(1);
        assertThat(sample.name(), is("rate.a.b.rate.mean"));
        assertThat(sample.value(), is(5.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(2);
        assertThat(sample.name(), is("rate.a.b.rate.1_minute"));
        assertThat(sample.value(), is(9.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(3);
        assertThat(sample.name(), is("rate.a.b.rate.5_minutes"));
        assertThat(sample.value(), is(13.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(4);
        assertThat(sample.name(), is("rate.a.b.rate.15_minutes"));
        assertThat(sample.value(), is(17.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(5);
        assertThat(sample.name(), is("rate.a.b.rate.unit"));
        assertThat(sample.value(), is("events/hours"));
        assertThat(sample.type(), is(INSTANT));

        // instance 2
        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(6));

        sample = samples.get(0);
        assertThat(sample.name(), is("rate.a.b.c.1.2.count"));
        assertThat(sample.value(), is(2L));
        assertThat(sample.type(), is(DELTA));

        sample = samples.get(1);
        assertThat(sample.name(), is("rate.a.b.c.1.2.rate.mean"));
        assertThat(sample.value(), is(6.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(2);
        assertThat(sample.name(), is("rate.a.b.c.1.2.rate.1_minute"));
        assertThat(sample.value(), is(10.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(3);
        assertThat(sample.name(), is("rate.a.b.c.1.2.rate.5_minutes"));
        assertThat(sample.value(), is(14.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(4);
        assertThat(sample.name(), is("rate.a.b.c.1.2.rate.15_minutes"));
        assertThat(sample.value(), is(18.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(5);
        assertThat(sample.name(), is("rate.a.b.c.1.2.rate.unit"));
        assertThat(sample.value(), is("events/hours"));
        assertThat(sample.type(), is(INSTANT));

        // instance 3
        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(6));

        sample = samples.get(0);
        assertThat(sample.name(), is("rate.a.b.c.d.count"));
        assertThat(sample.value(), is(3L));
        assertThat(sample.type(), is(DELTA));

        sample = samples.get(1);
        assertThat(sample.name(), is("rate.a.b.c.d.rate.mean"));
        assertThat(sample.value(), is(7.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(2);
        assertThat(sample.name(), is("rate.a.b.c.d.rate.1_minute"));
        assertThat(sample.value(), is(11.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(3);
        assertThat(sample.name(), is("rate.a.b.c.d.rate.5_minutes"));
        assertThat(sample.value(), is(15.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(4);
        assertThat(sample.name(), is("rate.a.b.c.d.rate.15_minutes"));
        assertThat(sample.value(), is(19.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(5);
        assertThat(sample.name(), is("rate.a.b.c.d.rate.unit"));
        assertThat(sample.value(), is("events/hours"));
        assertThat(sample.type(), is(INSTANT));

        // instance 4
        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(6));

        sample = samples.get(0);
        assertThat(sample.name(), is("rate.a.b.c.d.e.1.2.3.count"));
        assertThat(sample.value(), is(4L));
        assertThat(sample.type(), is(DELTA));

        sample = samples.get(1);
        assertThat(sample.name(), is("rate.a.b.c.d.e.1.2.3.rate.mean"));
        assertThat(sample.value(), is(8.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(2);
        assertThat(sample.name(), is("rate.a.b.c.d.e.1.2.3.rate.1_minute"));
        assertThat(sample.value(), is(12.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(3);
        assertThat(sample.name(), is("rate.a.b.c.d.e.1.2.3.rate.5_minutes"));
        assertThat(sample.value(), is(16.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(4);
        assertThat(sample.name(), is("rate.a.b.c.d.e.1.2.3.rate.15_minutes"));
        assertThat(sample.value(), is(20.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(5);
        assertThat(sample.name(), is("rate.a.b.c.d.e.1.2.3.rate.unit"));
        assertThat(sample.value(), is("events/hours"));
        assertThat(sample.type(), is(INSTANT));
    }

    @Test
    public void histogram() {
        Histogram histogram = mock(Histogram.class);
        doCallRealMethod().when(histogram).forEach(any(Consumer.class));
        metrics.put(name("histogram", "a", "b"), histogram);

        MeasurableValueProvider<TestHistogramImpl> countProvider = mock(MeasurableValueProvider.class);
        when(countProvider.valueFor(any())).thenReturn(1L, 2L, 3L, 4L);

        MeasurableValueProvider<TestHistogramImpl> minProvider = mock(MeasurableValueProvider.class);
        when(minProvider.valueFor(any())).thenReturn(5.0, 6.0, 7.0, 8.0);

        MeasurableValueProvider<TestHistogramImpl> maxProvider = mock(MeasurableValueProvider.class);
        when(maxProvider.valueFor(any())).thenReturn(9.0, 10.0, 11.0, 12.0);

        MeasurableValueProvider<TestHistogramImpl> meanProvider = mock(MeasurableValueProvider.class);
        when(meanProvider.valueFor(any())).thenReturn(13.0, 14.0, 15.0, 16.0);

        MeasurableValueProvider<TestHistogramImpl> standardDeviationProvider = mock(MeasurableValueProvider.class);
        when(standardDeviationProvider.valueFor(any())).thenReturn(17.0, 18.0, 19.0, 20.0);

        MeasurableValueProvider<TestHistogramImpl> percentile_50_provider = mock(MeasurableValueProvider.class);
        when(percentile_50_provider.valueFor(any())).thenReturn(21.0, 22.0, 23.0, 24.0);

        MeasurableValueProvider<TestHistogramImpl> percentile_75_provider = mock(MeasurableValueProvider.class);
        when(percentile_75_provider.valueFor(any())).thenReturn(25.0, 26.0, 27.0, 28.0);

        MetricInstance instance_1 = new TestHistogramInstance(
            withName("histogram", "a", "b"),
            emptyList(),
            true,
            false,
            false,
            linkedHashMapOf(
                List.of(COUNT, MIN, MAX, MEAN, STANDARD_DEVIATION, PERCENTILE_50, PERCENTILE_75),
                List.of(countProvider, minProvider, maxProvider, meanProvider, standardDeviationProvider, percentile_50_provider, percentile_75_provider)),
            new TestHistogramImpl());

        MetricInstance instance_2 = new TestHistogramInstance(
            withName("histogram", "a", "b", "c"),
            dimensionValues(DIMENSION_1.value("1"), DIMENSION_2.value("2")).list(),
            true,
            false,
            true,
            linkedHashMapOf(
                List.of(COUNT, MIN, MAX, MEAN, STANDARD_DEVIATION, PERCENTILE_50, PERCENTILE_75),
                List.of(countProvider, minProvider, maxProvider, meanProvider, standardDeviationProvider, percentile_50_provider, percentile_75_provider)),
            new TestHistogramImpl());

        MetricInstance instance_3 = new TestHistogramInstance(
            withName("histogram", "a", "b", "c", "d"),
            emptyList(),
            true,
            true,
            false,
            linkedHashMapOf(
                List.of(COUNT, MIN, MAX, MEAN, STANDARD_DEVIATION, PERCENTILE_50, PERCENTILE_75),
                List.of(countProvider, minProvider, maxProvider, meanProvider, standardDeviationProvider, percentile_50_provider, percentile_75_provider)),
            new TestHistogramImpl());

        MetricInstance instance_4 = new TestHistogramInstance(
            withName("histogram", "a", "b", "c", "d", "e"),
            dimensionValues(
                DIMENSION_1.value("1"),
                DIMENSION_2.value("2"),
                DIMENSION_3.value("3")).list(),
            false,
            false,
            false,
            linkedHashMapOf(
                List.of(COUNT, MIN, MAX, MEAN, STANDARD_DEVIATION, PERCENTILE_50, PERCENTILE_75),
                List.of(countProvider, minProvider, maxProvider, meanProvider, standardDeviationProvider, percentile_50_provider, percentile_75_provider)),
            new TestHistogramImpl());

        when(histogram.iterator()).thenReturn(List.of(instance_1, instance_2, instance_3, instance_4).iterator());
        Set<InstanceSample<DefaultSample>> instanceSamples = provider.instanceSamples();

        assertThat(instanceSamples.size(), is(4));
        Iterator<InstanceSample<DefaultSample>> instanceSamplesIter = instanceSamples.iterator();

        // instance 1
        InstanceSample<DefaultSample> instanceSample = instanceSamplesIter.next();
        List<DefaultSample> samples = instanceSample.samples();
        assertThat(samples.size(), is(7));

        DefaultSample sample = samples.get(0);
        assertThat(sample.name(), is("histogram.a.b.count"));
        assertThat(sample.value(), is(1L));
        assertThat(sample.type(), is(DELTA));

        sample = samples.get(1);
        assertThat(sample.name(), is("histogram.a.b.min"));
        assertThat(sample.value(), is(5.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(2);
        assertThat(sample.name(), is("histogram.a.b.max"));
        assertThat(sample.value(), is(9.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(3);
        assertThat(sample.name(), is("histogram.a.b.mean"));
        assertThat(sample.value(), is(13.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(4);
        assertThat(sample.name(), is("histogram.a.b.stdDev"));
        assertThat(sample.value(), is(17.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(5);
        assertThat(sample.name(), is("histogram.a.b.50_percentile"));
        assertThat(sample.value(), is(21.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(6);
        assertThat(sample.name(), is("histogram.a.b.75_percentile"));
        assertThat(sample.value(), is(25.0));
        assertThat(sample.type(), is(INSTANT));

        // instance 2
        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(7));

        sample = samples.get(0);
        assertThat(sample.name(), is("histogram.a.b.c.1.2.count"));
        assertThat(sample.value(), is(2L));
        assertThat(sample.type(), is(DELTA));

        sample = samples.get(1);
        assertThat(sample.name(), is("histogram.a.b.c.1.2.min"));
        assertThat(sample.value(), is(6.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(2);
        assertThat(sample.name(), is("histogram.a.b.c.1.2.max"));
        assertThat(sample.value(), is(10.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(3);
        assertThat(sample.name(), is("histogram.a.b.c.1.2.mean"));
        assertThat(sample.value(), is(14.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(4);
        assertThat(sample.name(), is("histogram.a.b.c.1.2.stdDev"));
        assertThat(sample.value(), is(18.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(5);
        assertThat(sample.name(), is("histogram.a.b.c.1.2.50_percentile"));
        assertThat(sample.value(), is(22.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(6);
        assertThat(sample.name(), is("histogram.a.b.c.1.2.75_percentile"));
        assertThat(sample.value(), is(26.0));
        assertThat(sample.type(), is(INSTANT));

        // instance 3
        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(7));

        sample = samples.get(0);
        assertThat(sample.name(), is("histogram.a.b.c.d.count"));
        assertThat(sample.value(), is(3L));
        assertThat(sample.type(), is(DELTA));

        sample = samples.get(1);
        assertThat(sample.name(), is("histogram.a.b.c.d.min"));
        assertThat(sample.value(), is(7.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(2);
        assertThat(sample.name(), is("histogram.a.b.c.d.max"));
        assertThat(sample.value(), is(11.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(3);
        assertThat(sample.name(), is("histogram.a.b.c.d.mean"));
        assertThat(sample.value(), is(15.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(4);
        assertThat(sample.name(), is("histogram.a.b.c.d.stdDev"));
        assertThat(sample.value(), is(19.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(5);
        assertThat(sample.name(), is("histogram.a.b.c.d.50_percentile"));
        assertThat(sample.value(), is(23.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(6);
        assertThat(sample.name(), is("histogram.a.b.c.d.75_percentile"));
        assertThat(sample.value(), is(27.0));
        assertThat(sample.type(), is(INSTANT));

        // instance 4
        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(7));

        sample = samples.get(0);
        assertThat(sample.name(), is("histogram.a.b.c.d.e.1.2.3.count"));
        assertThat(sample.value(), is(4L));
        assertThat(sample.type(), is(DELTA));

        sample = samples.get(1);
        assertThat(sample.name(), is("histogram.a.b.c.d.e.1.2.3.min"));
        assertThat(sample.value(), is(8.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(2);
        assertThat(sample.name(), is("histogram.a.b.c.d.e.1.2.3.max"));
        assertThat(sample.value(), is(12.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(3);
        assertThat(sample.name(), is("histogram.a.b.c.d.e.1.2.3.mean"));
        assertThat(sample.value(), is(16.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(4);
        assertThat(sample.name(), is("histogram.a.b.c.d.e.1.2.3.stdDev"));
        assertThat(sample.value(), is(20.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(5);
        assertThat(sample.name(), is("histogram.a.b.c.d.e.1.2.3.50_percentile"));
        assertThat(sample.value(), is(24.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(6);
        assertThat(sample.name(), is("histogram.a.b.c.d.e.1.2.3.75_percentile"));
        assertThat(sample.value(), is(28.0));
        assertThat(sample.type(), is(INSTANT));
    }

    @Test
    public void timer() {
        Timer timer = mock(Timer.class);
        doCallRealMethod().when(timer).forEach(any(Consumer.class));
        metrics.put(name("timer", "a", "b"), timer);

        MeasurableValueProvider<TestTimerImpl> countProvider = mock(MeasurableValueProvider.class);
        when(countProvider.valueFor(any())).thenReturn(1L, 2L, 3L, 4L);

        // rate
        MeasurableValueProvider<TestTimerImpl> rateMeanProvider = mock(MeasurableValueProvider.class);
        when(rateMeanProvider.valueFor(any())).thenReturn(5.0, 6.0, 7.0, 8.0);

        MeasurableValueProvider<TestTimerImpl> rateMinute_1_provider = mock(MeasurableValueProvider.class);
        when(rateMinute_1_provider.valueFor(any())).thenReturn(9.0, 10.0, 11.0, 12.0);

        MeasurableValueProvider<TestTimerImpl> rateMinute_5_provider = mock(MeasurableValueProvider.class);
        when(rateMinute_5_provider.valueFor(any())).thenReturn(13.0, 14.0, 15.0, 16.0);

        MeasurableValueProvider<TestTimerImpl> rateMinute_15_provider = mock(MeasurableValueProvider.class);
        when(rateMinute_15_provider.valueFor(any())).thenReturn(17.0, 18.0, 19.0, 20.0);

        MeasurableValueProvider<TestTimerImpl> rateUnitProvider = mock(MeasurableValueProvider.class);
        when(rateUnitProvider.valueFor(any())).thenReturn("events/hours");

        // duration
        MeasurableValueProvider<TestTimerImpl> durationMinProvider = mock(MeasurableValueProvider.class);
        when(durationMinProvider.valueFor(any())).thenReturn(21.0, 22.0, 23.0, 24.0);

        MeasurableValueProvider<TestTimerImpl> durationMaxProvider = mock(MeasurableValueProvider.class);
        when(durationMaxProvider.valueFor(any())).thenReturn(25.0, 26.0, 27.0, 28.0);

        MeasurableValueProvider<TestTimerImpl> durationMeanProvider = mock(MeasurableValueProvider.class);
        when(durationMeanProvider.valueFor(any())).thenReturn(29.0, 30.0, 31.0, 32.0);

        MeasurableValueProvider<TestTimerImpl> durationStandardDeviationProvider = mock(MeasurableValueProvider.class);
        when(durationStandardDeviationProvider.valueFor(any())).thenReturn(33.0, 34.0, 35.0, 36.0);

        MeasurableValueProvider<TestTimerImpl> durationPercentile_50_provider = mock(MeasurableValueProvider.class);
        when(durationPercentile_50_provider.valueFor(any())).thenReturn(37.0, 38.0, 39.0, 40.0);

        MeasurableValueProvider<TestTimerImpl> durationPercentile_75_provider = mock(MeasurableValueProvider.class);
        when(durationPercentile_75_provider.valueFor(any())).thenReturn(41.0, 42.0, 43.0, 44.0);

        MeasurableValueProvider<TestTimerImpl> durationUnitProvider = mock(MeasurableValueProvider.class);
        when(durationUnitProvider.valueFor(any())).thenReturn("days");

        MetricInstance instance_1 = new TestTimerInstance(
            withName("timer", "a", "b"),
            emptyList(),
            true,
            false,
            false,
            linkedHashMapOf(
                List.of(
                    COUNT,
                    MEAN_RATE,
                    ONE_MINUTE_RATE,
                    FIVE_MINUTES_RATE,
                    FIFTEEN_MINUTES_RATE,
                    RATE_UNIT,
                    MIN,
                    MAX,
                    MEAN,
                    STANDARD_DEVIATION,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    DURATION_UNIT),
                List.of(
                    countProvider,
                    rateMeanProvider,
                    rateMinute_1_provider,
                    rateMinute_5_provider,
                    rateMinute_15_provider,
                    rateUnitProvider,
                    durationMinProvider,
                    durationMaxProvider,
                    durationMeanProvider,
                    durationStandardDeviationProvider,
                    durationPercentile_50_provider,
                    durationPercentile_75_provider,
                    durationUnitProvider)),
            new TestTimerImpl());

        MetricInstance instance_2 = new TestTimerInstance(
            withName("timer", "a", "b", "c"),
            dimensionValues(DIMENSION_1.value("1"), DIMENSION_2.value("2")).list(),
            true,
            false,
            true,
            linkedHashMapOf(
                List.of(
                    COUNT,
                    MEAN_RATE,
                    ONE_MINUTE_RATE,
                    FIVE_MINUTES_RATE,
                    FIFTEEN_MINUTES_RATE,
                    RATE_UNIT,
                    MIN,
                    MAX,
                    MEAN,
                    STANDARD_DEVIATION,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    DURATION_UNIT),
                List.of(
                    countProvider,
                    rateMeanProvider,
                    rateMinute_1_provider,
                    rateMinute_5_provider,
                    rateMinute_15_provider,
                    rateUnitProvider,
                    durationMinProvider,
                    durationMaxProvider,
                    durationMeanProvider,
                    durationStandardDeviationProvider,
                    durationPercentile_50_provider,
                    durationPercentile_75_provider,
                    durationUnitProvider)),
            new TestTimerImpl());

        MetricInstance instance_3 = new TestTimerInstance(
            withName("timer", "a", "b", "c", "d"),
            emptyList(),
            true,
            true,
            false,
            linkedHashMapOf(
                List.of(
                    COUNT,
                    MEAN_RATE,
                    ONE_MINUTE_RATE,
                    FIVE_MINUTES_RATE,
                    FIFTEEN_MINUTES_RATE,
                    RATE_UNIT,
                    MIN,
                    MAX,
                    MEAN,
                    STANDARD_DEVIATION,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    DURATION_UNIT),
                List.of(
                    countProvider,
                    rateMeanProvider,
                    rateMinute_1_provider,
                    rateMinute_5_provider,
                    rateMinute_15_provider,
                    rateUnitProvider,
                    durationMinProvider,
                    durationMaxProvider,
                    durationMeanProvider,
                    durationStandardDeviationProvider,
                    durationPercentile_50_provider,
                    durationPercentile_75_provider,
                    durationUnitProvider)),
            new TestTimerImpl());

        MetricInstance instance_4 = new TestTimerInstance(
            withName("timer", "a", "b", "c", "d", "e"),
            dimensionValues(
                DIMENSION_1.value("1"),
                DIMENSION_2.value("2"),
                DIMENSION_3.value("3")).list(),
            false,
            false,
            false,
            linkedHashMapOf(
                List.of(
                    COUNT,
                    MEAN_RATE,
                    ONE_MINUTE_RATE,
                    FIVE_MINUTES_RATE,
                    FIFTEEN_MINUTES_RATE,
                    RATE_UNIT,
                    MIN,
                    MAX,
                    MEAN,
                    STANDARD_DEVIATION,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    DURATION_UNIT),
                List.of(
                    countProvider,
                    rateMeanProvider,
                    rateMinute_1_provider,
                    rateMinute_5_provider,
                    rateMinute_15_provider,
                    rateUnitProvider,
                    durationMinProvider,
                    durationMaxProvider,
                    durationMeanProvider,
                    durationStandardDeviationProvider,
                    durationPercentile_50_provider,
                    durationPercentile_75_provider,
                    durationUnitProvider)),
            new TestTimerImpl());

        when(timer.iterator()).thenReturn(List.of(instance_1, instance_2, instance_3, instance_4).iterator());
        Set<InstanceSample<DefaultSample>> instanceSamples = provider.instanceSamples();

        assertThat(instanceSamples.size(), is(4));
        Iterator<InstanceSample<DefaultSample>> instanceSamplesIter = instanceSamples.iterator();

        // instance 1
        InstanceSample<DefaultSample> instanceSample = instanceSamplesIter.next();
        List<DefaultSample> samples = instanceSample.samples();
        assertThat(samples.size(), is(13));

        DefaultSample sample = samples.get(0);
        assertThat(sample.name(), is("timer.a.b.count"));
        assertThat(sample.value(), is(1L));
        assertThat(sample.type(), is(DELTA));

        sample = samples.get(1);
        assertThat(sample.name(), is("timer.a.b.rate.mean"));
        assertThat(sample.value(), is(5.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(2);
        assertThat(sample.name(), is("timer.a.b.rate.1_minute"));
        assertThat(sample.value(), is(9.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(3);
        assertThat(sample.name(), is("timer.a.b.rate.5_minutes"));
        assertThat(sample.value(), is(13.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(4);
        assertThat(sample.name(), is("timer.a.b.rate.15_minutes"));
        assertThat(sample.value(), is(17.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(5);
        assertThat(sample.name(), is("timer.a.b.rate.unit"));
        assertThat(sample.value(), is("events/hours"));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(6);
        assertThat(sample.name(), is("timer.a.b.duration.min"));
        assertThat(sample.value(), is(21.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(7);
        assertThat(sample.name(), is("timer.a.b.duration.max"));
        assertThat(sample.value(), is(25.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(8);
        assertThat(sample.name(), is("timer.a.b.duration.mean"));
        assertThat(sample.value(), is(29.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(9);
        assertThat(sample.name(), is("timer.a.b.duration.stdDev"));
        assertThat(sample.value(), is(33.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(10);
        assertThat(sample.name(), is("timer.a.b.duration.50_percentile"));
        assertThat(sample.value(), is(37.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(11);
        assertThat(sample.name(), is("timer.a.b.duration.75_percentile"));
        assertThat(sample.value(), is(41.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(12);
        assertThat(sample.name(), is("timer.a.b.duration.unit"));
        assertThat(sample.value(), is("days"));
        assertThat(sample.type(), is(INSTANT));

        // instance 2
        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(13));

        sample = samples.get(0);
        assertThat(sample.name(), is("timer.a.b.c.1.2.count"));
        assertThat(sample.value(), is(2L));
        assertThat(sample.type(), is(DELTA));

        sample = samples.get(1);
        assertThat(sample.name(), is("timer.a.b.c.1.2.rate.mean"));
        assertThat(sample.value(), is(6.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(2);
        assertThat(sample.name(), is("timer.a.b.c.1.2.rate.1_minute"));
        assertThat(sample.value(), is(10.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(3);
        assertThat(sample.name(), is("timer.a.b.c.1.2.rate.5_minutes"));
        assertThat(sample.value(), is(14.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(4);
        assertThat(sample.name(), is("timer.a.b.c.1.2.rate.15_minutes"));
        assertThat(sample.value(), is(18.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(5);
        assertThat(sample.name(), is("timer.a.b.c.1.2.rate.unit"));
        assertThat(sample.value(), is("events/hours"));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(6);
        assertThat(sample.name(), is("timer.a.b.c.1.2.duration.min"));
        assertThat(sample.value(), is(22.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(7);
        assertThat(sample.name(), is("timer.a.b.c.1.2.duration.max"));
        assertThat(sample.value(), is(26.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(8);
        assertThat(sample.name(), is("timer.a.b.c.1.2.duration.mean"));
        assertThat(sample.value(), is(30.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(9);
        assertThat(sample.name(), is("timer.a.b.c.1.2.duration.stdDev"));
        assertThat(sample.value(), is(34.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(10);
        assertThat(sample.name(), is("timer.a.b.c.1.2.duration.50_percentile"));
        assertThat(sample.value(), is(38.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(11);
        assertThat(sample.name(), is("timer.a.b.c.1.2.duration.75_percentile"));
        assertThat(sample.value(), is(42.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(12);
        assertThat(sample.name(), is("timer.a.b.c.1.2.duration.unit"));
        assertThat(sample.value(), is("days"));
        assertThat(sample.type(), is(INSTANT));

        // instance 3
        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(13));

        sample = samples.get(0);
        assertThat(sample.name(), is("timer.a.b.c.d.count"));
        assertThat(sample.value(), is(3L));
        assertThat(sample.type(), is(DELTA));

        sample = samples.get(1);
        assertThat(sample.name(), is("timer.a.b.c.d.rate.mean"));
        assertThat(sample.value(), is(7.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(2);
        assertThat(sample.name(), is("timer.a.b.c.d.rate.1_minute"));
        assertThat(sample.value(), is(11.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(3);
        assertThat(sample.name(), is("timer.a.b.c.d.rate.5_minutes"));
        assertThat(sample.value(), is(15.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(4);
        assertThat(sample.name(), is("timer.a.b.c.d.rate.15_minutes"));
        assertThat(sample.value(), is(19.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(5);
        assertThat(sample.name(), is("timer.a.b.c.d.rate.unit"));
        assertThat(sample.value(), is("events/hours"));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(6);
        assertThat(sample.name(), is("timer.a.b.c.d.duration.min"));
        assertThat(sample.value(), is(23.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(7);
        assertThat(sample.name(), is("timer.a.b.c.d.duration.max"));
        assertThat(sample.value(), is(27.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(8);
        assertThat(sample.name(), is("timer.a.b.c.d.duration.mean"));
        assertThat(sample.value(), is(31.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(9);
        assertThat(sample.name(), is("timer.a.b.c.d.duration.stdDev"));
        assertThat(sample.value(), is(35.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(10);
        assertThat(sample.name(), is("timer.a.b.c.d.duration.50_percentile"));
        assertThat(sample.value(), is(39.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(11);
        assertThat(sample.name(), is("timer.a.b.c.d.duration.75_percentile"));
        assertThat(sample.value(), is(43.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(12);
        assertThat(sample.name(), is("timer.a.b.c.d.duration.unit"));
        assertThat(sample.value(), is("days"));
        assertThat(sample.type(), is(INSTANT));

        // instance 4
        instanceSample = instanceSamplesIter.next();
        samples = instanceSample.samples();
        assertThat(samples.size(), is(13));

        sample = samples.get(0);
        assertThat(sample.name(), is("timer.a.b.c.d.e.1.2.3.count"));
        assertThat(sample.value(), is(4L));
        assertThat(sample.type(), is(DELTA));

        sample = samples.get(1);
        assertThat(sample.name(), is("timer.a.b.c.d.e.1.2.3.rate.mean"));
        assertThat(sample.value(), is(8.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(2);
        assertThat(sample.name(), is("timer.a.b.c.d.e.1.2.3.rate.1_minute"));
        assertThat(sample.value(), is(12.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(3);
        assertThat(sample.name(), is("timer.a.b.c.d.e.1.2.3.rate.5_minutes"));
        assertThat(sample.value(), is(16.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(4);
        assertThat(sample.name(), is("timer.a.b.c.d.e.1.2.3.rate.15_minutes"));
        assertThat(sample.value(), is(20.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(5);
        assertThat(sample.name(), is("timer.a.b.c.d.e.1.2.3.rate.unit"));
        assertThat(sample.value(), is("events/hours"));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(6);
        assertThat(sample.name(), is("timer.a.b.c.d.e.1.2.3.duration.min"));
        assertThat(sample.value(), is(24.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(7);
        assertThat(sample.name(), is("timer.a.b.c.d.e.1.2.3.duration.max"));
        assertThat(sample.value(), is(28.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(8);
        assertThat(sample.name(), is("timer.a.b.c.d.e.1.2.3.duration.mean"));
        assertThat(sample.value(), is(32.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(9);
        assertThat(sample.name(), is("timer.a.b.c.d.e.1.2.3.duration.stdDev"));
        assertThat(sample.value(), is(36.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(10);
        assertThat(sample.name(), is("timer.a.b.c.d.e.1.2.3.duration.50_percentile"));
        assertThat(sample.value(), is(40.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(11);
        assertThat(sample.name(), is("timer.a.b.c.d.e.1.2.3.duration.75_percentile"));
        assertThat(sample.value(), is(44.0));
        assertThat(sample.type(), is(INSTANT));

        sample = samples.get(12);
        assertThat(sample.name(), is("timer.a.b.c.d.e.1.2.3.duration.unit"));
        assertThat(sample.value(), is("days"));
        assertThat(sample.type(), is(INSTANT));
    }
}