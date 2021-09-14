package com.ringcentral.platform.metrics.var;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.test.time.*;
import com.ringcentral.platform.metrics.var.configs.builders.BaseVarConfigBuilder;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static com.ringcentral.platform.metrics.TestMetricListener.NotificationType.*;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.*;
import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.utils.CollectionUtils.iterToSet;
import static com.ringcentral.platform.metrics.var.Var.noTotal;
import static com.ringcentral.platform.metrics.var.configs.builders.BaseVarConfigBuilder.variable;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public abstract class AbstractVarTest<V, T extends AbstractVar<V>> {

    protected interface VarMaker<V, T extends AbstractVar<V>> {
        T makeVar(
            MetricName name,
            BaseVarConfigBuilder configBuilder,
            Supplier<V> valueSupplier,
            ScheduledExecutorService executor);
    }

    protected interface ValueSupplierMaker<V> {
        Supplier<V> makeValueSupplier();
    }

    protected static final MetricDimension SUBSYSTEM = new MetricDimension("subsystem");
    protected static final MetricDimension SERVICE = new MetricDimension("service");
    protected static final MetricDimension SERVER = new MetricDimension("server");

    protected VarMaker<V, T> varMaker;
    protected ValueSupplierMaker<V> valueSupplierMaker;

    protected TestTimeNanosProvider timeNanosProvider = new TestTimeNanosProvider();
    protected TestScheduledExecutorService executor = new TestScheduledExecutorService(timeNanosProvider);

    public AbstractVarTest(
        VarMaker<V, T> varMaker,
        ValueSupplierMaker<V> valueSupplierMaker) {

        this.varMaker = varMaker;
        this.valueSupplierMaker = valueSupplierMaker;
    }

    @Test
    public void defaultConfig() {
        CountingValueSupplier<V> valueSupplier = new CountingValueSupplier<>(valueSupplierMaker.makeValueSupplier());

        T var = varMaker.makeVar(
            name("var"),
            variable(),
            valueSupplier,
            executor);

        var.metricAdded();
        TestMetricListener listener = new TestMetricListener();
        var.addListener(listener);
        assertThat(listener.notificationCount(), is(1));
        assertThat(listener.notification(0).type(), is(INSTANCE_ADDED));

        MetricInstance instance = listener.instances().iterator().next();
        assertThat(instance.name(), is(var.name()));
        assertFalse(instance.hasDimensionValues());
        assertTrue(instance.isTotalInstance());
        assertFalse(instance.isDimensionalTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(Set.of(var.valueMeasurable())));
        instance.valueOf(var.valueMeasurable());
        assertThat(valueSupplier.count(), is(1));
        assertThat(iterToSet(var.iterator()), is(Set.of(instance)));

        var.metricRemoved();

        assertThat(listener.notificationCount(), is(2));
        assertThat(listener.notification(1).type(), is(INSTANCE_REMOVED));
        assertTrue(listener.instances().isEmpty());
        assertThat(iterToSet(var.iterator()), is(Set.of(instance)));
    }

    @Test
    public void disabled() {
        Supplier<V> valueSupplier = valueSupplierMaker.makeValueSupplier();

        T var = varMaker.makeVar(
            name("var"),
            variable().disable(),
            valueSupplier,
            executor);

        var.metricAdded();
        TestMetricListener listener = new TestMetricListener();
        var.addListener(listener);
        assertThat(listener.notificationCount(), is(0));
        assertTrue(iterToSet(var.iterator()).isEmpty());
        var.metricRemoved();
        assertThat(listener.notificationCount(), is(0));
        assertTrue(iterToSet(var.iterator()).isEmpty());
    }

    @Test
    public void dimensional() {
        CountingValueSupplier<V> valueSupplier_1 = new CountingValueSupplier<>(valueSupplierMaker.makeValueSupplier());

        T var = varMaker.makeVar(
            name("var"),
            variable().dimensions(SERVICE, SERVER),
            valueSupplier_1,
            executor);

        var.metricAdded();
        TestMetricListener listener = new TestMetricListener();
        var.addListener(listener);
        assertThat(listener.notificationCount(), is(1));
        assertThat(listener.notification(0).type(), is(INSTANCE_ADDED));

        MetricInstance instance = listener.notification(0).instance();
        assertThat(instance.name(), is(var.name()));
        assertFalse(instance.hasDimensionValues());
        assertTrue(instance.isTotalInstance());
        assertTrue(instance.isDimensionalTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(Set.of(var.valueMeasurable())));
        instance.valueOf(var.valueMeasurable());
        assertThat(valueSupplier_1.count(), is(1));
        assertThat(iterToSet(var.iterator()), is(Set.of(instance)));

        CountingValueSupplier<V> valueSupplier_2 = new CountingValueSupplier<>(valueSupplierMaker.makeValueSupplier());
        var.register(valueSupplier_2, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1")));

        assertThat(listener.notificationCount(), is(2));
        assertThat(listener.notification(1).type(), is(INSTANCE_ADDED));

        instance = listener.notification(1).instance();
        assertThat(instance.name(), is(var.name()));
        assertThat(instance.dimensionValues(), is(List.of(SERVICE.value("service_1"), SERVER.value("server_1"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isDimensionalTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(Set.of(var.valueMeasurable())));
        instance.valueOf(var.valueMeasurable());
        assertThat(valueSupplier_1.count(), is(1));
        assertThat(valueSupplier_2.count(), is(1));

        assertThat(iterToSet(var.iterator()), is(Set.of(
            listener.notification(0).instance(),
            listener.notification(1).instance())));

        CountingValueSupplier<V> valueSupplier_3 = new CountingValueSupplier<>(valueSupplierMaker.makeValueSupplier());
        var.register(valueSupplier_3, forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2")));

        assertThat(listener.notificationCount(), is(3));
        assertThat(listener.notification(2).type(), is(INSTANCE_ADDED));

        instance = listener.notification(2).instance();
        assertThat(instance.name(), is(var.name()));
        assertThat(instance.dimensionValues(), is(List.of(SERVICE.value("service_2"), SERVER.value("server_2"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isDimensionalTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(Set.of(var.valueMeasurable())));
        instance.valueOf(var.valueMeasurable());
        assertThat(valueSupplier_1.count(), is(1));
        assertThat(valueSupplier_2.count(), is(1));
        assertThat(valueSupplier_3.count(), is(1));

        assertThat(iterToSet(var.iterator()), is(Set.of(
            listener.notification(0).instance(),
            listener.notification(1).instance(),
            listener.notification(2).instance())));

        var.deregister(forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1")));

        assertThat(listener.notificationCount(), is(4));
        assertThat(listener.notification(3).type(), is(INSTANCE_REMOVED));
        assertThat(listener.notification(3).instance(), is(listener.notification(1).instance()));

        var.metricRemoved();

        assertThat(listener.notificationCount(), is(6));
        assertThat(listener.notification(4).type(), is(INSTANCE_REMOVED));
        assertThat(listener.notification(4).instance(), is(listener.notification(0).instance()));
        assertThat(listener.notification(5).type(), is(INSTANCE_REMOVED));
        assertThat(listener.notification(5).instance(), is(listener.notification(2).instance()));
        assertTrue(listener.instances().isEmpty());

        assertThat(iterToSet(var.iterator()), is(Set.of(
            listener.notification(0).instance(),
            listener.notification(2).instance())));
    }

    @Test
    public void dimensional_noTotal() {
        CountingValueSupplier<V> valueSupplier_1 = new CountingValueSupplier<>(valueSupplierMaker.makeValueSupplier());

        T var = varMaker.makeVar(
            name("var"),
            variable()
                .prefix(dimensionValues(SUBSYSTEM.value("sub")))
                .dimensions(SERVICE, SERVER),
            noTotal(),
            executor);

        var.metricAdded();
        TestMetricListener listener = new TestMetricListener();
        var.addListener(listener);
        assertThat(listener.notificationCount(), is(0));

        var.register(valueSupplier_1, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1")));

        assertThat(listener.notificationCount(), is(1));
        assertThat(listener.notification(0).type(), is(INSTANCE_ADDED));

        MetricInstance instance = listener.notification(0).instance();
        assertThat(instance.name(), is(var.name()));
        assertThat(instance.dimensionValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"), SERVER.value("server_1"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isDimensionalTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(Set.of(var.valueMeasurable())));
        instance.valueOf(var.valueMeasurable());
        assertThat(valueSupplier_1.count(), is(1));
        assertThat(iterToSet(var.iterator()), is(Set.of(listener.notification(0).instance())));

        var.metricRemoved();

        assertThat(listener.notificationCount(), is(2));
        assertThat(listener.notification(1).type(), is(INSTANCE_REMOVED));
        assertTrue(listener.instances().isEmpty());
        assertThat(iterToSet(var.iterator()), is(Set.of(listener.notification(0).instance())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidDimensionValues() {
        CountingValueSupplier<V> valueSupplier_1 = new CountingValueSupplier<>(valueSupplierMaker.makeValueSupplier());

        T var = varMaker.makeVar(
            name("var"),
            variable().dimensions(SERVICE, SERVER),
            noTotal(),
            executor);

        var.metricAdded();
        TestMetricListener listener = new TestMetricListener();
        var.addListener(listener);
        assertThat(listener.notificationCount(), is(0));

        var.register(valueSupplier_1, forDimensionValues(SERVER.value("server_1"), SERVICE.value("service_1")));
    }
}