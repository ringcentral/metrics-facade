package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.configs.MeterInstanceConfig;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.InstanceConfigBuilder;
import com.ringcentral.platform.metrics.configs.builders.MetricConfigBuilderProvider;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.test.time.TestScheduledExecutorService;
import com.ringcentral.platform.metrics.test.time.TestTimeMsProvider;
import com.ringcentral.platform.metrics.test.time.TestTimeNanosProvider;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.AbstractMeter.EXPIRED_INSTANCES_REMOVAL_ADDITIONAL_DELAY_MS;
import static com.ringcentral.platform.metrics.TestMetricListener.NotificationType.INSTANCE_ADDED;
import static com.ringcentral.platform.metrics.TestMetricListener.NotificationType.INSTANCE_REMOVED;
import static com.ringcentral.platform.metrics.labels.AllLabelValuesPredicate.labelValuesMatchingAll;
import static com.ringcentral.platform.metrics.labels.AnyLabelValuesPredicate.labelValuesMatchingAny;
import static com.ringcentral.platform.metrics.labels.LabelValues.*;
import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.utils.CollectionUtils.iterToSet;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractMeterTest<
    MeasurableType extends Measurable,
    InstanceConfigType extends MeterInstanceConfig,
    ConfigBuilderType extends AbstractMeterConfigBuilder<MeasurableType, InstanceConfigType, ?, ?, ?, ?, ConfigBuilderType>,
    Type extends AbstractMeter<?, InstanceConfigType, ?, ?>> {

    protected interface ConfigBuilderMaker<
        MeasurableType extends Measurable,
        InstanceConfigType extends MeterInstanceConfig,
        ConfigBuilderType extends AbstractMeterConfigBuilder<MeasurableType, InstanceConfigType, ?, ?, ?, ?, ConfigBuilderType>> {

        ConfigBuilderType makeConfigBuilder();
    }

    protected interface InstanceConfigBuilderMaker<
        MeasurableType extends Measurable,
        InstanceConfigType extends MeterInstanceConfig> {

        InstanceConfigBuilder<MeasurableType, InstanceConfigType, ?> makeInstanceConfigBuilder();
    }

    protected interface MeterMaker<
        MeasurableType extends Measurable,
        InstanceConfigType extends MeterInstanceConfig,
        ConfigBuilderType extends AbstractMeterConfigBuilder<MeasurableType, InstanceConfigType, ?, ?, ?, ?, ConfigBuilderType>,
        Type extends AbstractMeter<?, ?, ?, ?>> {

        default Type makeMeter(
            MetricName name,
            MetricConfigBuilderProvider<ConfigBuilderType> configBuilderProvider,
            TimeMsProvider timeMsProvider,
            ScheduledExecutorService executor) {

            return makeMeter(
                name,
                configBuilderProvider.builder(),
                timeMsProvider,
                executor);
        }

        Type makeMeter(
            MetricName name,
            ConfigBuilderType configBuilder,
            TimeMsProvider timeMsProvider,
            ScheduledExecutorService executor);
    }

    protected interface InstanceVerifier {
        boolean verifyInstance(MetricInstance instance, List<Long> expectedValues);
    }

    protected static final Label SUBSYSTEM = new Label("subsystem");
    protected static final Label SERVICE = new Label("service");
    protected static final Label SERVER = new Label("server");
    protected static final Label PORT = new Label("port");

    protected Set<MeasurableType> supportedMeasurables;
    protected Set<MeasurableType> notAllSupportedMeasurables;

    protected ConfigBuilderMaker<MeasurableType, InstanceConfigType, ConfigBuilderType> configBuilderMaker;
    protected InstanceConfigBuilderMaker<MeasurableType, InstanceConfigType> instanceConfigBuilderMaker;
    protected MeterMaker<MeasurableType, InstanceConfigType, ConfigBuilderType, Type> meterMaker;
    protected InstanceVerifier instanceVerifier;

    protected TestTimeNanosProvider timeNanosProvider = new TestTimeNanosProvider();
    protected TestTimeMsProvider timeMsProvider = new TestTimeMsProvider(timeNanosProvider);
    protected TestScheduledExecutorService executor = new TestScheduledExecutorService(timeNanosProvider);

    protected AbstractMeterTest(
        Set<MeasurableType> supportedMeasurables,
        ConfigBuilderMaker<MeasurableType, InstanceConfigType, ConfigBuilderType> configBuilderMaker,
        InstanceConfigBuilderMaker<MeasurableType, InstanceConfigType> instanceConfigBuilderMaker,
        MeterMaker<MeasurableType, InstanceConfigType, ConfigBuilderType, Type> meterMaker,
        InstanceVerifier instanceVerifier) {

        this.supportedMeasurables = supportedMeasurables;

        this.notAllSupportedMeasurables =
            supportedMeasurables.size() > 1 ?
            Set.of(supportedMeasurables.iterator().next()) :
            null;

        this.configBuilderMaker = configBuilderMaker;
        this.instanceConfigBuilderMaker = instanceConfigBuilderMaker;
        this.meterMaker = meterMaker;
        this.instanceVerifier = instanceVerifier;
    }

    @Test
    public void defaultConfig() {
        Type meter = meterMaker.makeMeter(
            name("meter"),
            configBuilderMaker.makeConfigBuilder(),
            timeMsProvider,
            executor);

        meter.metricAdded();
        TestMetricListener listener = new TestMetricListener();
        meter.addListener(listener);
        assertThat(listener.notificationCount(), is(1));
        assertThat(listener.notification(0).type(), is(INSTANCE_ADDED));

        MetricInstance instance = listener.instances().iterator().next();
        assertThat(instance.name(), is(meter.name()));
        assertFalse(instance.hasLabelValues());
        assertTrue(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, emptyList()));
        meter.update(1L, NO_LABEL_VALUES);
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(1L)));
        meter.update(2L, NO_LABEL_VALUES);
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(1L, 2L)));
        assertThat(iterToSet(meter.iterator()), is(Set.of(instance)));

        meter.metricRemoved();

        assertThat(listener.notificationCount(), is(2));
        assertThat(listener.notification(1).type(), is(INSTANCE_REMOVED));
        assertTrue(listener.instances().isEmpty());
        assertThat(iterToSet(meter.iterator()), is(Set.of(instance)));
    }

    @Test
    public void disabled() {
        Type meter = meterMaker.makeMeter(
            name("meter"),
            configBuilderMaker.makeConfigBuilder().disable(),
            timeMsProvider,
            executor);

        meter.metricAdded();
        TestMetricListener listener = new TestMetricListener();
        meter.addListener(listener);
        assertThat(listener.notificationCount(), is(0));
        assertTrue(iterToSet(meter.iterator()).isEmpty());
        meter.metricRemoved();
        assertThat(listener.notificationCount(), is(0));
        assertTrue(iterToSet(meter.iterator()).isEmpty());
    }

    @Test
    public void noTotal() {
        Type meter = meterMaker.makeMeter(
            name("meter"),
            configBuilderMaker.makeConfigBuilder().allSlice().noTotal(),
            timeMsProvider,
            executor);

        meter.metricAdded();
        TestMetricListener listener = new TestMetricListener();
        meter.addListener(listener);
        assertThat(listener.notificationCount(), is(0));
        meter.metricRemoved();
        assertThat(listener.notificationCount(), is(0));
    }

    @Test
    public void exclusions() {
        Type meter = meterMaker.makeMeter(
            withName("ActiveHealthChecker", "healthCheck"),
            configBuilderMaker.makeConfigBuilder()
                .labels(SERVICE, SERVER, PORT)
                .exclude(labelValuesMatchingAny(
                    SERVER.mask("server_1_*|*2_1*"),
                    PORT.predicate(p -> p.equals("9001"))))
                .allSlice().noLevels(),
            timeMsProvider,
            executor);

        meter.metricAdded();

        TestMetricListener listener = new TestMetricListener();
        meter.addListener(listener);
        assertThat(listener.notificationCount(), is(1));
        assertThat(listener.notification(0).type(), is(INSTANCE_ADDED));

        MetricInstance instance = listener.instances().iterator().next();
        assertThat(instance.name(), is(meter.name()));
        assertFalse(instance.hasLabelValues());
        assertTrue(instance.isTotalInstance());
        assertTrue(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, emptyList()));

        assertThat(iterToSet(meter.iterator()), is(Set.of(instance)));

        meter.update(25L, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("7001")));
        assertThat(listener.notificationCount(), is(1));
        assertTrue(instanceVerifier.verifyInstance(instance, emptyList()));

        meter.update(25L, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("7002")));
        assertThat(listener.notificationCount(), is(1));
        assertTrue(instanceVerifier.verifyInstance(instance, emptyList()));

        meter.update(75L, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("7001")));
        assertThat(listener.notificationCount(), is(1));
        assertTrue(instanceVerifier.verifyInstance(instance, emptyList()));

        meter.update(25L, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("8001")));
        assertThat(listener.notificationCount(), is(1));
        assertTrue(instanceVerifier.verifyInstance(instance, emptyList()));

        meter.update(75L, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_2"), PORT.value("8001")));

        assertTrue(instanceVerifier.verifyInstance(instance, List.of(75L)));
        assertThat(listener.notificationCount(), is(2));
        assertThat(listener.notification(1).type(), is(INSTANCE_ADDED));
        instance = listener.notification(1).instance();
        assertThat(instance.name(), is(meter.name()));
        assertThat(instance.labelValues(), is(List.of(SERVICE.value("service_2"), SERVER.value("server_2_2"), PORT.value("8001"))));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(75L)));

        meter.update(1000L, forLabelValues(SERVICE.value("service_3"), SERVER.value("server_3_1"), PORT.value("9001")));

        assertTrue(instanceVerifier.verifyInstance(instance, List.of(75L)));
        assertThat(listener.notificationCount(), is(2));
        assertThat(listener.notification(1).type(), is(INSTANCE_ADDED));
        instance = listener.notification(1).instance();
        assertThat(instance.labelValues(), is(List.of(SERVICE.value("service_2"), SERVER.value("server_2_2"), PORT.value("8001"))));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(75L)));

        assertThat(iterToSet(meter.iterator()), is(Set.of(
            listener.notification(0).instance(),
            listener.notification(1).instance())));
    }

    @Test
    public void slicesAndLevels() {
        Type meter = meterMaker.makeMeter(
            withName("ActiveHealthChecker", "healthCheck"),
            configBuilderMaker.makeConfigBuilder()
                .prefix(labelValues(SUBSYSTEM.value("sub")))
                .labels(SERVICE, SERVER, PORT)
                .maxLabeledInstancesPerSlice(5)
                .expireLabeledInstanceAfter(75, SECONDS)
                .measurables(supportedMeasurables)
                .allSlice()
                    .noMaxLabeledInstances()
                    .total(instanceConfigBuilderMaker.makeInstanceConfigBuilder()
                        .name(name("total"))
                        .measurables(notAllSupportedMeasurables))
                .slice("byServer")
                    .labels(SERVER)
                    .notExpireLabeledInstances()
                .slice("server_1_or_2_1", "port_not_7002")
                    .predicate(labelValuesMatchingAll(
                        SERVER.mask("server_1_*|*2_1*"),
                        PORT.predicate(p -> !p.equals("7002"))))
                    .labels(SERVICE, SERVER)
                    .maxLabeledInstances(4)
                    .expireLabeledInstanceAfter(25, SECONDS)
                    .measurables(notAllSupportedMeasurables)
                    .enableLevels(),
            timeMsProvider,
            executor);

        meter.metricAdded();
        TestMetricListener listener = new TestMetricListener();
        meter.addListener(listener);

        // check n11s
        assertThat(listener.notificationCount(), is(3));

        // ActiveHealthChecker.healthCheck.total, label values = [subsystem=sub], total = true, level = false
        assertThat(listener.notification(0).type(), is(INSTANCE_ADDED));
        MetricInstance instance = listener.notification(0).instance();
        assertThat(instance.name(), is(meter.name().withNewPart("total")));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"))));
        assertTrue(instance.isTotalInstance());
        assertTrue(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(notAllSupportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, emptyList()));

        // ActiveHealthChecker.healthCheck.byServer, label values = [subsystem=sub], total = true, level = false
        assertThat(listener.notification(1).type(), is(INSTANCE_ADDED));
        instance = listener.notification(1).instance();
        assertThat(instance.name(), is(meter.name().withNewPart("byServer")));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"))));
        assertTrue(instance.isTotalInstance());
        assertTrue(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, emptyList()));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub], total = true, level = false
        assertThat(listener.notification(2).type(), is(INSTANCE_ADDED));
        instance = listener.notification(2).instance();
        assertThat(instance.name(), is(name(meter.name(), "server_1_or_2_1", "port_not_7002")));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"))));
        assertTrue(instance.isTotalInstance());
        assertTrue(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(notAllSupportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, emptyList()));

        printlnSeparator();
        meter.update(1L, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("7001")));
        timeNanosProvider.increaseSec(5L);

        assertTrue(instanceVerifier.verifyInstance(listener.notification(0).instance(), List.of(1L)));
        assertTrue(instanceVerifier.verifyInstance(listener.notification(1).instance(), List.of(1L)));
        assertTrue(instanceVerifier.verifyInstance(listener.notification(2).instance(), List.of(1L)));

        // check n11s
        assertThat(listener.notificationCount(), is(3 + 6));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1], total = false, level = true
        assertThat(listener.notification(3).type(), is(INSTANCE_ADDED));
        instance = listener.notification(3).instance();
        assertThat(instance.name(), is(meter.name()));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertTrue(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(1L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1,server=server_1_1], total = false, level = true
        assertThat(listener.notification(4).type(), is(INSTANCE_ADDED));
        instance = listener.notification(4).instance();
        assertThat(instance.name(), is(meter.name()));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"), SERVER.value("server_1_1"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertTrue(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(1L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1,server=server_1_1,port=7001], total = false, level = false
        assertThat(listener.notification(5).type(), is(INSTANCE_ADDED));
        instance = listener.notification(5).instance();
        assertThat(instance.name(), is(name(meter.name())));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("7001"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(1L)));

        // ActiveHealthChecker.healthCheck.byServer, label values = [subsystem=sub,server=server_1_1], total = false, level = false
        assertThat(listener.notification(6).type(), is(INSTANCE_ADDED));
        instance = listener.notification(6).instance();
        assertThat(instance.name(), is(meter.name().withNewPart("byServer")));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVER.value("server_1_1"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(1L)));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub,service=service_1], total = false, level = true
        assertThat(listener.notification(7).type(), is(INSTANCE_ADDED));
        instance = listener.notification(7).instance();
        assertThat(instance.name(), is(name(meter.name(), "server_1_or_2_1", "port_not_7002")));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertTrue(instance.isLevelInstance());
        assertThat(instance.measurables(), is(notAllSupportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(1L)));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub,service=service_1,server=server_1_1], total = false, level = false
        assertThat(listener.notification(8).type(), is(INSTANCE_ADDED));
        instance = listener.notification(8).instance();
        assertThat(instance.name(), is(name(meter.name(), "server_1_or_2_1", "port_not_7002")));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"), SERVER.value("server_1_1"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(notAllSupportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(1L)));

        printlnSeparator();
        meter.update(2L, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("7002")));
        timeNanosProvider.increaseSec(5L);

        // ActiveHealthChecker.healthCheck.total, label values = [subsystem=sub], total = true, level = false
        assertTrue(instanceVerifier.verifyInstance(listener.notification(0).instance(), List.of(1L, 2L)));

        // ActiveHealthChecker.healthCheck.byServer, label values = [subsystem=sub], total = true, level = false
        assertTrue(instanceVerifier.verifyInstance(listener.notification(1).instance(), List.of(1L, 2L)));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub], total = true, level = false
        assertTrue(instanceVerifier.verifyInstance(listener.notification(2).instance(), List.of(1L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1], total = false, level = true
        assertTrue(instanceVerifier.verifyInstance(listener.notification(3).instance(), List.of(1L, 2L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1,server=server_1_1], total = false, level = true
        assertTrue(instanceVerifier.verifyInstance(listener.notification(4).instance(), List.of(1L, 2L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1,server=server_1_1,port=7001], total = false, level = false
        assertTrue(instanceVerifier.verifyInstance(listener.notification(5).instance(), List.of(1L)));

        // ActiveHealthChecker.healthCheck.byServer, label values = [subsystem=sub,server=server_1_1], total = false, level = false
        assertTrue(instanceVerifier.verifyInstance(listener.notification(6).instance(), List.of(1L, 2L)));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub,service=service_1], total = false, level = true
        assertTrue(instanceVerifier.verifyInstance(listener.notification(7).instance(), List.of(1L)));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub,service=service_1,server=server_1_1], total = false, level = false
        assertTrue(instanceVerifier.verifyInstance(listener.notification(8).instance(), List.of(1L)));

        // check n11s
        assertThat(listener.notificationCount(), is(3 + 6 + 1));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1,server=server_1_1,port=7002], total = false, level = false
        assertThat(listener.notification(9).type(), is(INSTANCE_ADDED));
        instance = listener.notification(9).instance();
        assertThat(instance.name(), is(name(meter.name())));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("7002"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(2L)));

        printlnSeparator();
        meter.update(3L, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("7001")));
        timeNanosProvider.increaseSec(5L);

        // ActiveHealthChecker.healthCheck.total, label values = [subsystem=sub], total = true, level = false
        assertTrue(instanceVerifier.verifyInstance(listener.notification(0).instance(), List.of(1L, 2L, 3L)));

        // ActiveHealthChecker.healthCheck.byServer, label values = [subsystem=sub], total = true, level = false
        assertTrue(instanceVerifier.verifyInstance(listener.notification(1).instance(), List.of(1L, 2L, 3L)));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub], total = true, level = false
        assertTrue(instanceVerifier.verifyInstance(listener.notification(2).instance(), List.of(1L, 3L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1], total = false, level = true
        assertTrue(instanceVerifier.verifyInstance(listener.notification(3).instance(), List.of(1L, 2L, 3L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1,server=server_1_1], total = false, level = true
        assertTrue(instanceVerifier.verifyInstance(listener.notification(4).instance(), List.of(1L, 2L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1,server=server_1_1,port=7001], total = false, level = false
        assertTrue(instanceVerifier.verifyInstance(listener.notification(5).instance(), List.of(1L)));

        // ActiveHealthChecker.healthCheck.byServer, label values = [subsystem=sub,server=server_1_1], total = false, level = false
        assertTrue(instanceVerifier.verifyInstance(listener.notification(6).instance(), List.of(1L, 2L)));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub,service=service_1], total = false, level = true
        assertTrue(instanceVerifier.verifyInstance(listener.notification(7).instance(), List.of(1L, 3L)));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub,service=service_1,server=server_1_1], total = false, level = false
        assertTrue(instanceVerifier.verifyInstance(listener.notification(8).instance(), List.of(1L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1,server=server_1_1,port=7002], total = false, level = false
        assertTrue(instanceVerifier.verifyInstance(listener.notification(9).instance(), List.of(2L)));

        // check n11s
        assertThat(listener.notificationCount(), is(3 + 6 + 1 + 4));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1,server=server_1_2], total = false, level = true
        assertThat(listener.notification(10).type(), is(INSTANCE_ADDED));
        instance = listener.notification(10).instance();
        assertThat(instance.name(), is(name(meter.name())));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"), SERVER.value("server_1_2"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertTrue(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(3L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1,server=server_1_2,port=7001], total = false, level = false
        assertThat(listener.notification(11).type(), is(INSTANCE_ADDED));
        instance = listener.notification(11).instance();
        assertThat(instance.name(), is(name(meter.name())));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("7001"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(3L)));

        // ActiveHealthChecker.healthCheck.byServer, label values = [subsystem=sub,server=server_1_2], total = false, level = false
        assertThat(listener.notification(12).type(), is(INSTANCE_ADDED));
        instance = listener.notification(12).instance();
        assertThat(instance.name(), is(name(meter.name().withNewPart("byServer"))));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVER.value("server_1_2"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(3L)));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub,service=service_1,server=server_1_2], total = false, level = false
        assertThat(listener.notification(13).type(), is(INSTANCE_ADDED));
        instance = listener.notification(13).instance();
        assertThat(instance.name(), is(name(meter.name(), "server_1_or_2_1", "port_not_7002")));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"), SERVER.value("server_1_2"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(notAllSupportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(3L)));


        assertThat(iterToSet(meter.iterator()), is(Set.of(
            listener.notification(0).instance(),
            listener.notification(1).instance(),
            listener.notification(2).instance(),
            listener.notification(3).instance(),
            listener.notification(4).instance(),
            listener.notification(5).instance(),
            listener.notification(6).instance(),
            listener.notification(7).instance(),
            listener.notification(8).instance(),
            listener.notification(9).instance(),
            listener.notification(10).instance(),
            listener.notification(11).instance(),
            listener.notification(12).instance(),
            listener.notification(13).instance())));

        printlnSeparator();
        meter.update(4L, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("8001")));
        timeNanosProvider.increaseSec(5L);
        assertThat(listener.notificationCount(), is(3 + 6 + 1 + 4 + 7));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_2], total = false, level = true
        assertThat(listener.notification(14).type(), is(INSTANCE_ADDED));
        instance = listener.notification(14).instance();
        assertThat(instance.name(), is(meter.name()));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_2"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertTrue(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(4L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_2,server=server_2_1], total = false, level = true
        assertThat(listener.notification(15).type(), is(INSTANCE_ADDED));
        instance = listener.notification(15).instance();
        assertThat(instance.name(), is(meter.name()));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_2"), SERVER.value("server_2_1"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertTrue(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(4L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_2,server=server_2_1,port=8001], total = false, level = false
        assertThat(listener.notification(16).type(), is(INSTANCE_ADDED));
        instance = listener.notification(16).instance();
        assertThat(instance.name(), is(meter.name()));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("8001"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(4L)));

        // ActiveHealthChecker.healthCheck.byServer, label values = [subsystem=sub,server=server_2_1], total = false, level = false
        assertThat(listener.notification(17).type(), is(INSTANCE_ADDED));
        instance = listener.notification(17).instance();
        assertThat(instance.name(), is(meter.name().withNewPart("byServer")));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVER.value("server_2_1"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(4L)));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub,service=service_2], total = false, level = true
        assertThat(listener.notification(18).type(), is(INSTANCE_ADDED));
        instance = listener.notification(18).instance();
        assertThat(instance.name(), is(name(meter.name(), "server_1_or_2_1", "port_not_7002")));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_2"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertTrue(instance.isLevelInstance());
        assertThat(instance.measurables(), is(notAllSupportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(4L)));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub,service=service_1,server=server_1_1], total = false, level = false
        assertThat(listener.notification(19).type(), is(INSTANCE_REMOVED));
        instance = listener.notification(19).instance();
        assertThat(instance.name(), is(name(meter.name(), "server_1_or_2_1", "port_not_7002")));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"), SERVER.value("server_1_1"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(notAllSupportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(1L)));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub,service=service_2,server=server_2_1], total = false, level = false
        assertThat(listener.notification(20).type(), is(INSTANCE_ADDED));
        instance = listener.notification(20).instance();
        assertThat(instance.name(), is(name(meter.name(), "server_1_or_2_1", "port_not_7002")));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_2"), SERVER.value("server_2_1"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(notAllSupportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(4L)));

        assertThat(iterToSet(meter.iterator()), is(Set.of(
            listener.notification(0).instance(),
            listener.notification(1).instance(),
            listener.notification(2).instance(),
            listener.notification(3).instance(),
            listener.notification(4).instance(),
            listener.notification(5).instance(),
            listener.notification(6).instance(),
            listener.notification(7).instance(),
            // listener.notification(8).instance(),
            listener.notification(9).instance(),
            listener.notification(10).instance(),
            listener.notification(11).instance(),
            listener.notification(12).instance(),
            listener.notification(13).instance(),
            listener.notification(14).instance(),
            listener.notification(15).instance(),
            listener.notification(16).instance(),
            listener.notification(17).instance(),
            listener.notification(18).instance(),
            // listener.notification(19).instance(), - INSTANCE_REMOVED: listener.notification(8).instance()
            listener.notification(20).instance())));

        printlnSeparator();
        meter.update(5L, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("8001")));
        timeNanosProvider.increaseSec(5L);
        assertThat(listener.notificationCount(), is(3 + 6 + 1 + 4 + 7));
        assertTrue(instanceVerifier.verifyInstance(listener.notification(20).instance(), List.of(4L, 5L)));

        // expiration
        printlnSeparator();
        timeNanosProvider.increaseMs(EXPIRED_INSTANCES_REMOVAL_ADDITIONAL_DELAY_MS - 1L);
        assertThat(listener.notificationCount(), is(3 + 6 + 1 + 4 + 7));

        printlnSeparator();
        timeNanosProvider.increaseMs(1L);
        assertThat(listener.notificationCount(), is(3 + 6 + 1 + 4 + 7 + 2));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub,service=service_1,server=server_1_2], total = false, level = false
        assertThat(listener.notification(21).type(), is(INSTANCE_REMOVED));
        instance = listener.notification(21).instance();
        assertThat(instance.name(), is(name(meter.name(), "server_1_or_2_1", "port_not_7002")));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"), SERVER.value("server_1_2"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(notAllSupportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(3L)));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub,service=service_1], total = false, level = true
        assertThat(listener.notification(22).type(), is(INSTANCE_REMOVED));
        instance = listener.notification(22).instance();
        assertThat(instance.name(), is(name(meter.name(), "server_1_or_2_1", "port_not_7002")));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertTrue(instance.isLevelInstance());
        assertThat(instance.measurables(), is(notAllSupportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(1L, 3L)));

        printlnSeparator();
        timeNanosProvider.increaseMs(5000L + EXPIRED_INSTANCES_REMOVAL_ADDITIONAL_DELAY_MS - 1L);
        assertThat(listener.notificationCount(), is(3 + 6 + 1 + 4 + 7 + 2));

        printlnSeparator();
        timeNanosProvider.increaseMs(1L);
        assertThat(listener.notificationCount(), is(3 + 6 + 1 + 4 + 7 + 2 + 2));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub,service=service_2,server=server_2_1], total = false, level = false
        assertThat(listener.notification(23).type(), is(INSTANCE_REMOVED));
        instance = listener.notification(23).instance();
        assertThat(instance.name(), is(name(meter.name(), "server_1_or_2_1", "port_not_7002")));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_2"), SERVER.value("server_2_1"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(notAllSupportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(4L, 5L)));

        // ActiveHealthChecker.healthCheck.server_1_or_2_1.port_not_7002, label values = [subsystem=sub,service=service_2], total = false, level = true
        assertThat(listener.notification(24).type(), is(INSTANCE_REMOVED));
        instance = listener.notification(24).instance();
        assertThat(instance.name(), is(name(meter.name(), "server_1_or_2_1", "port_not_7002")));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_2"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertTrue(instance.isLevelInstance());
        assertThat(instance.measurables(), is(notAllSupportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(4L, 5L)));

        printlnSeparator();
        timeNanosProvider.increaseMs(25000L + EXPIRED_INSTANCES_REMOVAL_ADDITIONAL_DELAY_MS - 1L);
        assertThat(listener.notificationCount(), is(3 + 6 + 1 + 4 + 7 + 2 + 2));

        printlnSeparator();
        timeNanosProvider.increaseMs(1L);
        assertThat(listener.notificationCount(), is(3 + 6 + 1 + 4 + 7 + 2 + 2 + 6));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1,server=server_1_1,port=7001], total = false, level = false
        assertThat(listener.notification(25).type(), is(INSTANCE_REMOVED));
        instance = listener.notification(25).instance();
        assertThat(instance.name(), is(meter.name()));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("7001"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(1L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1,server=server_1_1,port=7002], total = false, level = false
        assertThat(listener.notification(26).type(), is(INSTANCE_REMOVED));
        instance = listener.notification(26).instance();
        assertThat(instance.name(), is(meter.name()));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("7002"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(2L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1,server=server_1_1], total = false, level = true
        assertThat(listener.notification(27).type(), is(INSTANCE_REMOVED));
        instance = listener.notification(27).instance();
        assertThat(instance.name(), is(meter.name()));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"), SERVER.value("server_1_1"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertTrue(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(1L, 2L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1,server=server_1_2,port=7001], total = false, level = false
        assertThat(listener.notification(28).type(), is(INSTANCE_REMOVED));
        instance = listener.notification(28).instance();
        assertThat(instance.name(), is(meter.name()));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("7001"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(3L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1,server=server_1_2], total = false, level = true
        assertThat(listener.notification(29).type(), is(INSTANCE_REMOVED));
        instance = listener.notification(29).instance();
        assertThat(instance.name(), is(meter.name()));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"), SERVER.value("server_1_2"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertTrue(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(3L)));

        // ActiveHealthChecker.healthCheck, label values = [subsystem=sub,service=service_1], total = false, level = true
        assertThat(listener.notification(30).type(), is(INSTANCE_REMOVED));
        instance = listener.notification(30).instance();
        assertThat(instance.name(), is(meter.name()));
        assertThat(instance.labelValues(), is(List.of(SUBSYSTEM.value("sub"), SERVICE.value("service_1"))));
        assertFalse(instance.isTotalInstance());
        assertFalse(instance.isLabeledMetricTotalInstance());
        assertTrue(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, List.of(1L, 2L, 3L)));

        printlnSeparator();
        meter.metricRemoved();
        assertThat(listener.notificationCount(), is(3 + 6 + 1 + 4 + 7 + 2 + 2 + 6 + 9));

        for (int i = 31; i < 40; ++i) {
            assertThat(listener.notification(i).type(), is(INSTANCE_REMOVED));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidLabelValues() {
        Type meter = meterMaker.makeMeter(
            name("meter"),
            configBuilderMaker.makeConfigBuilder().labels(SERVICE),
            timeMsProvider,
            executor);

        meter.metricAdded();
        TestMetricListener listener = new TestMetricListener();
        meter.addListener(listener);
        assertThat(listener.notificationCount(), is(1));
        assertThat(listener.notification(0).type(), is(INSTANCE_ADDED));

        MetricInstance instance = listener.instances().iterator().next();
        assertThat(instance.name(), is(meter.name()));
        assertFalse(instance.hasLabelValues());
        assertTrue(instance.isTotalInstance());
        assertTrue(instance.isLabeledMetricTotalInstance());
        assertFalse(instance.isLevelInstance());
        assertThat(instance.measurables(), is(supportedMeasurables));
        assertTrue(instanceVerifier.verifyInstance(instance, emptyList()));

        meter.update(1L, forLabelValues(SERVICE.value("service")));
        meter.update(2L, forLabelValues(SERVICE.value("service")));
        assertThat(listener.notificationCount(), is(2));
        assertThat(listener.notification(1).type(), is(INSTANCE_ADDED));

        assertTrue(instanceVerifier.verifyInstance(listener.notification(0).instance(), List.of(1L, 2L)));
        assertTrue(instanceVerifier.verifyInstance(listener.notification(1).instance(), List.of(1L, 2L)));

        assertThat(iterToSet(meter.iterator()), is(Set.of(
            listener.notification(0).instance(),
            listener.notification(1).instance())));

        meter.update(3L, forLabelValues(SERVICE.value("service"), SERVER.value("server")));
    }

    private void printlnSeparator() {
        System.out.println("--- " + timeMsProvider.timeMs() + " ---");
    }
}