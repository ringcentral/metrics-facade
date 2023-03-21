package com.ringcentral.platform.metrics.micrometer;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.stub.StubMetricRegistry;
import com.ringcentral.platform.metrics.test.time.*;
import com.ringcentral.platform.metrics.var.doubleVar.*;
import com.ringcentral.platform.metrics.var.longVar.*;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.*;
import org.junit.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.ringcentral.platform.metrics.MetricModBuilder.modifying;
import static com.ringcentral.platform.metrics.TestMetricListener.NotificationType.*;
import static com.ringcentral.platform.metrics.TestMetricRegistryListener.NotificationType.*;
import static com.ringcentral.platform.metrics.configs.builders.BaseMeterConfigBuilder.withMeter;
import static com.ringcentral.platform.metrics.names.MetricName.*;
import static com.ringcentral.platform.metrics.names.MetricNameMask.allMetrics;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class MfMeterRegistryTest {

    static final Label SERVICE = new Label("1_service");
    static final Label SERVER = new Label("2_server");
    static final Label STATISTIC = new Label("statistic");

    TestTimeNanosProvider timeNanosProvider = new TestTimeNanosProvider();
    TestScheduledExecutorService executor = new TestScheduledExecutorService(timeNanosProvider);

    MetricRegistry mfRegistry = new StubMetricRegistry(executor);
    TestMetricRegistryListener mfRegistryListener = new TestMetricRegistryListener();
    MfMeterRegistry registry = new MfMeterRegistry(mfRegistry, Clock.SYSTEM);

    @Before
    public void before() {
        mfRegistry.preConfigure(allMetrics(), modifying().meter(withMeter().allSlice().noLevels()));
        mfRegistry.addListener(mfRegistryListener);
    }

    @Test
    public void gauge_nonLabeled() {
        assertThat(mfRegistryListener.notificationCount(), is(0));
        AtomicLong valueSupplier = new AtomicLong();
        Gauge gauge = Gauge.builder("gauge.nonLabeled", valueSupplier::incrementAndGet).register(registry);
        assertThat(gauge.value(), is(1.0));
        assertThat(gauge.value(), is(2.0));

        assertThat(mfRegistryListener.notificationCount(), is(1));
        TestMetricRegistryListener.Notification mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        DoubleVar mfDoubleVar = mfRegistryNotification.metric();
        assertThat(mfDoubleVar.name(), is(name("gauge", "nonLabeled")));

        TestMetricListener mfDoubleVarListener = mfRegistryListener.listenerForMetric(withName("gauge", "nonLabeled"));
        assertThat(mfDoubleVarListener.notificationCount(), is(1));
        assertThat(mfDoubleVarListener.notification(0).type(), is(INSTANCE_ADDED));
        DoubleVarInstance instance = (DoubleVarInstance)mfDoubleVarListener.notification(0).instance();
        assertTrue(instance.isTotalInstance());
        assertFalse(instance.isNonDecreasing());

        registry.remove(gauge);

        assertThat(mfDoubleVarListener.notificationCount(), is(2));
        assertThat(mfDoubleVarListener.notification(1).type(), is(INSTANCE_REMOVED));
        assertSame(mfDoubleVarListener.notification(1).instance(), instance);

        assertThat(mfRegistryListener.notificationCount(), is(2));
        assertThat(mfRegistryListener.notification(1).type(), is(METRIC_REMOVED));
        assertThat(mfRegistryListener.notification(1).metric(), is(mfDoubleVar));
    }

    @Test
    public void gauge_labeled() {
        assertThat(mfRegistryListener.notificationCount(), is(0));
        AtomicLong valueSupplier_1 = new AtomicLong();

        // add
        Gauge gauge_1 = Gauge.builder("gauge.labeled", valueSupplier_1::incrementAndGet)
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(registry);

        assertThat(gauge_1.value(), is(1.0));
        assertThat(gauge_1.value(), is(2.0));

        assertThat(mfRegistryListener.notificationCount(), is(1));
        TestMetricRegistryListener.Notification mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        DoubleVar mfDoubleVar = mfRegistryNotification.metric();
        assertThat(mfDoubleVar.name(), is(name("gauge", "labeled")));

        TestMetricListener mfDoubleVarListener = mfRegistryListener.listenerForMetric(withName("gauge", "labeled"));

        assertThat(mfDoubleVarListener.notificationCount(), is(1));
        assertThat(mfDoubleVarListener.notification(0).type(), is(INSTANCE_ADDED));
        DoubleVarInstance instance_1 = (DoubleVarInstance)mfDoubleVarListener.notification(0).instance();
        assertThat(instance_1.labelValues(), is(List.of(SERVICE.value("service_1"), SERVER.value("server_1_1"))));
        assertFalse(instance_1.isTotalInstance());
        assertFalse(instance_1.isNonDecreasing());

        AtomicLong valueSupplier_2 = new AtomicLong();

        Gauge gauge_2 = Gauge.builder("gauge.labeled", valueSupplier_2::incrementAndGet)
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(registry);

        assertThat(gauge_2.value(), is(1.0));
        assertThat(gauge_2.value(), is(2.0));

        assertThat(mfRegistryListener.notificationCount(), is(1));

        assertThat(mfDoubleVarListener.notificationCount(), is(2));
        assertThat(mfDoubleVarListener.notification(1).type(), is(INSTANCE_ADDED));
        DoubleVarInstance instance_2 = (DoubleVarInstance)mfDoubleVarListener.notification(1).instance();
        assertThat(instance_2.labelValues(), is(List.of(SERVICE.value("service_2"), SERVER.value("server_2_1"))));
        assertFalse(instance_2.isTotalInstance());
        assertFalse(instance_2.isNonDecreasing());

        // remove
        registry.remove(gauge_1);

        assertThat(mfDoubleVarListener.notificationCount(), is(3));
        assertThat(mfDoubleVarListener.notification(2).type(), is(INSTANCE_REMOVED));
        assertSame(mfDoubleVarListener.notification(2).instance(), instance_1);

        registry.remove(gauge_2);

        assertThat(mfDoubleVarListener.notificationCount(), is(4));
        assertThat(mfDoubleVarListener.notification(3).type(), is(INSTANCE_REMOVED));
        assertSame(mfDoubleVarListener.notification(3).instance(), instance_2);

        assertThat(mfRegistryListener.notificationCount(), is(1));
    }

    @Test
    public void counter_nonLabeled() {
        assertThat(mfRegistryListener.notificationCount(), is(0));
        Counter counter = Counter.builder("counter.nonLabeled").register(registry);
        assertThat(counter.count(), is(0.0));
        counter.increment();
        assertThat(counter.count(), is(1.0));
        counter.increment();
        assertThat(counter.count(), is(2.0));

        assertThat(mfRegistryListener.notificationCount(), is(1));
        TestMetricRegistryListener.Notification mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        Rate mfRate = mfRegistryNotification.metric();
        assertThat(mfRate.name(), is(name("counter", "nonLabeled")));

        TestMetricListener mfRateListener = mfRegistryListener.listenerForMetric(withName("counter", "nonLabeled"));

        assertThat(mfRateListener.notificationCount(), is(1));
        assertThat(mfRateListener.notification(0).type(), is(INSTANCE_ADDED));
        MetricInstance instance = mfRateListener.notification(0).instance();
        assertTrue(instance.isTotalInstance());

        registry.remove(counter);

        assertThat(mfRateListener.notificationCount(), is(2));
        assertThat(mfRateListener.notification(1).type(), is(INSTANCE_REMOVED));
        assertSame(mfRateListener.notification(1).instance(), instance);

        assertThat(mfRegistryListener.notificationCount(), is(2));
        assertThat(mfRegistryListener.notification(1).type(), is(METRIC_REMOVED));
        assertThat(mfRegistryListener.notification(1).metric(), is(mfRate));
    }

    @Test
    public void counter_labeled() {
        assertThat(mfRegistryListener.notificationCount(), is(0));

        // add
        Counter counter_1 = Counter.builder("counter.labeled")
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(registry);

        assertThat(counter_1.count(), is(0.0));
        counter_1.increment();
        assertThat(counter_1.count(), is(1.0));
        counter_1.increment();
        assertThat(counter_1.count(), is(2.0));

        assertThat(mfRegistryListener.notificationCount(), is(1));
        TestMetricRegistryListener.Notification mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        Rate mfRate = mfRegistryNotification.metric();
        assertThat(mfRate.name(), is(name("counter", "labeled")));

        TestMetricListener mfRateListener = mfRegistryListener.listenerForMetric(withName("counter", "labeled"));

        assertThat(mfRateListener.notificationCount(), is(2));

        assertThat(mfRateListener.notification(0).type(), is(INSTANCE_ADDED));
        MetricInstance totalInstance = mfRateListener.notification(0).instance();
        assertTrue(totalInstance.isTotalInstance());

        assertThat(mfRateListener.notification(1).type(), is(INSTANCE_ADDED));
        MetricInstance instance_1 = mfRateListener.notification(1).instance();
        assertThat(instance_1.labelValues(), is(List.of(SERVICE.value("service_1"), SERVER.value("server_1_1"))));
        assertFalse(instance_1.isTotalInstance());

        Counter counter_2 = Counter.builder("counter.labeled")
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(registry);

        assertThat(counter_2.count(), is(0.0));
        counter_2.increment();
        assertThat(counter_2.count(), is(1.0));
        counter_2.increment();
        assertThat(counter_2.count(), is(2.0));

        assertThat(mfRegistryListener.notificationCount(), is(1));

        assertThat(mfRateListener.notificationCount(), is(3));
        assertThat(mfRateListener.notification(2).type(), is(INSTANCE_ADDED));
        MetricInstance instance_2 = mfRateListener.notification(2).instance();
        assertThat(instance_2.labelValues(), is(List.of(SERVICE.value("service_2"), SERVER.value("server_2_1"))));
        assertFalse(instance_2.isTotalInstance());

        // remove
        registry.remove(counter_1);

        assertThat(mfRateListener.notificationCount(), is(4));
        assertThat(mfRateListener.notification(3).type(), is(INSTANCE_REMOVED));
        assertSame(mfRateListener.notification(3).instance(), instance_1);

        registry.remove(counter_2);

        assertThat(mfRateListener.notificationCount(), is(5));
        assertThat(mfRateListener.notification(4).type(), is(INSTANCE_REMOVED));
        assertSame(mfRateListener.notification(4).instance(), instance_2);

        assertThat(mfRegistryListener.notificationCount(), is(1));
    }

    @Test
    public void functionCounter_nonLabeled() {
        assertThat(mfRegistryListener.notificationCount(), is(0));
        AtomicLong valueSupplier = new AtomicLong();

        FunctionCounter funCounter = FunctionCounter
            .builder("funCounter.nonLabeled", this, a -> (double)valueSupplier.incrementAndGet())
            .register(registry);

        assertThat(funCounter.count(), is(1.0));
        assertThat(funCounter.count(), is(2.0));

        assertThat(mfRegistryListener.notificationCount(), is(1));
        TestMetricRegistryListener.Notification mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        LongVar mfLongVar = mfRegistryNotification.metric();
        assertThat(mfLongVar.name(), is(name("funCounter", "nonLabeled")));

        TestMetricListener mfLongVarListener = mfRegistryListener.listenerForMetric(withName("funCounter", "nonLabeled"));

        assertThat(mfLongVarListener.notificationCount(), is(1));
        assertThat(mfLongVarListener.notification(0).type(), is(INSTANCE_ADDED));
        LongVarInstance instance = (LongVarInstance)mfLongVarListener.notification(0).instance();
        assertTrue(instance.isTotalInstance());
        assertTrue(instance.isNonDecreasing());

        registry.remove(funCounter);

        assertThat(mfLongVarListener.notificationCount(), is(2));
        assertThat(mfLongVarListener.notification(1).type(), is(INSTANCE_REMOVED));
        assertSame(mfLongVarListener.notification(1).instance(), instance);

        assertThat(mfRegistryListener.notificationCount(), is(2));
        assertThat(mfRegistryListener.notification(1).type(), is(METRIC_REMOVED));
        assertThat(mfRegistryListener.notification(1).metric(), is(mfLongVar));
    }

    @Test
    public void funCounter_labeled() {
        assertThat(mfRegistryListener.notificationCount(), is(0));
        AtomicLong valueSupplier_1 = new AtomicLong();

        // add
        FunctionCounter funCounter_1 = FunctionCounter
            .builder("funCounter.labeled", this, a -> (double)valueSupplier_1.incrementAndGet())
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(registry);

        assertThat(funCounter_1.count(), is(1.0));
        assertThat(funCounter_1.count(), is(2.0));

        assertThat(mfRegistryListener.notificationCount(), is(1));
        TestMetricRegistryListener.Notification mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        LongVar mfLongVar = mfRegistryNotification.metric();
        assertThat(mfLongVar.name(), is(name("funCounter", "labeled")));

        TestMetricListener mfLongVarListener = mfRegistryListener.listenerForMetric(withName("funCounter", "labeled"));

        assertThat(mfLongVarListener.notificationCount(), is(1));
        assertThat(mfLongVarListener.notification(0).type(), is(INSTANCE_ADDED));
        LongVarInstance instance_1 = (LongVarInstance)mfLongVarListener.notification(0).instance();
        assertThat(instance_1.labelValues(), is(List.of(SERVICE.value("service_1"), SERVER.value("server_1_1"))));
        assertFalse(instance_1.isTotalInstance());
        assertTrue(instance_1.isNonDecreasing());

        AtomicLong valueSupplier_2 = new AtomicLong();

        FunctionCounter funCounter_2 = FunctionCounter
            .builder("funCounter.labeled", this, a -> (double)valueSupplier_2.incrementAndGet())
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(registry);

        assertThat(funCounter_2.count(), is(1.0));
        assertThat(funCounter_2.count(), is(2.0));

        assertThat(mfRegistryListener.notificationCount(), is(1));

        assertThat(mfLongVarListener.notificationCount(), is(2));
        assertThat(mfLongVarListener.notification(1).type(), is(INSTANCE_ADDED));
        LongVarInstance instance_2 = (LongVarInstance)mfLongVarListener.notification(1).instance();
        assertThat(instance_2.labelValues(), is(List.of(SERVICE.value("service_2"), SERVER.value("server_2_1"))));
        assertFalse(instance_2.isTotalInstance());
        assertTrue(instance_2.isNonDecreasing());

        // remove
        registry.remove(funCounter_1);

        assertThat(mfLongVarListener.notificationCount(), is(3));
        assertThat(mfLongVarListener.notification(2).type(), is(INSTANCE_REMOVED));
        assertSame(mfLongVarListener.notification(2).instance(), instance_1);

        registry.remove(funCounter_2);

        assertThat(mfLongVarListener.notificationCount(), is(4));
        assertThat(mfLongVarListener.notification(3).type(), is(INSTANCE_REMOVED));
        assertSame(mfLongVarListener.notification(3).instance(), instance_2);

        assertThat(mfRegistryListener.notificationCount(), is(1));
    }

    @Test
    public void distributionSummary_nonLabeled() {
        assertThat(mfRegistryListener.notificationCount(), is(0));

        DistributionSummary distributionSummary = DistributionSummary
            .builder("distributionSummary.nonLabeled")
            .register(registry);

        assertThat(distributionSummary.count(), is(0L));
        distributionSummary.record(1.0);
        assertThat(distributionSummary.count(), is(1L));
        distributionSummary.record(2.0);
        assertThat(distributionSummary.count(), is(3L));

        assertThat(mfRegistryListener.notificationCount(), is(1));
        TestMetricRegistryListener.Notification mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        Histogram mfHistogram = mfRegistryNotification.metric();
        assertThat(mfHistogram.name(), is(name("distributionSummary", "nonLabeled")));

        TestMetricListener mfHistogramListener = mfRegistryListener.listenerForMetric(withName("distributionSummary", "nonLabeled"));

        assertThat(mfHistogramListener.notificationCount(), is(1));
        assertThat(mfHistogramListener.notification(0).type(), is(INSTANCE_ADDED));
        MetricInstance instance = mfHistogramListener.notification(0).instance();
        assertTrue(instance.isTotalInstance());

        registry.remove(distributionSummary);

        assertThat(mfHistogramListener.notificationCount(), is(2));
        assertThat(mfHistogramListener.notification(1).type(), is(INSTANCE_REMOVED));
        assertSame(mfHistogramListener.notification(1).instance(), instance);

        assertThat(mfRegistryListener.notificationCount(), is(2));
        assertThat(mfRegistryListener.notification(1).type(), is(METRIC_REMOVED));
        assertThat(mfRegistryListener.notification(1).metric(), is(mfHistogram));
    }

    @Test
    public void distributionSummary_labeled() {
        assertThat(mfRegistryListener.notificationCount(), is(0));

        // add
        DistributionSummary distributionSummary_1 = DistributionSummary
            .builder("distributionSummary.labeled")
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(registry);

        assertThat(distributionSummary_1.count(), is(0L));
        distributionSummary_1.record(1.0);
        assertThat(distributionSummary_1.count(), is(1L));
        distributionSummary_1.record(2.0);
        assertThat(distributionSummary_1.count(), is(3L));

        assertThat(mfRegistryListener.notificationCount(), is(1));
        TestMetricRegistryListener.Notification mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        Histogram mfHistogram = mfRegistryNotification.metric();
        assertThat(mfHistogram.name(), is(name("distributionSummary", "labeled")));

        TestMetricListener mfHistogramListener = mfRegistryListener.listenerForMetric(withName("distributionSummary", "labeled"));

        assertThat(mfHistogramListener.notificationCount(), is(2));

        assertThat(mfHistogramListener.notification(0).type(), is(INSTANCE_ADDED));
        MetricInstance totalInstance = mfHistogramListener.notification(0).instance();
        assertTrue(totalInstance.isTotalInstance());

        assertThat(mfHistogramListener.notification(1).type(), is(INSTANCE_ADDED));
        MetricInstance instance_1 = mfHistogramListener.notification(1).instance();
        assertThat(instance_1.labelValues(), is(List.of(SERVICE.value("service_1"), SERVER.value("server_1_1"))));
        assertFalse(instance_1.isTotalInstance());

        DistributionSummary distributionSummary_2 = DistributionSummary
            .builder("distributionSummary.labeled")
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(registry);

        assertThat(distributionSummary_2.count(), is(0L));
        distributionSummary_2.record(1.0);
        assertThat(distributionSummary_2.count(), is(1L));
        distributionSummary_2.record(2.0);
        assertThat(distributionSummary_2.count(), is(3L));

        assertThat(mfRegistryListener.notificationCount(), is(1));
        mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        assertSame(mfRegistryNotification.metric(), mfHistogram);

        assertThat(mfHistogramListener.notificationCount(), is(3));
        assertThat(mfHistogramListener.notification(2).type(), is(INSTANCE_ADDED));
        MetricInstance instance_2 = mfHistogramListener.notification(2).instance();
        assertThat(instance_2.labelValues(), is(List.of(SERVICE.value("service_2"), SERVER.value("server_2_1"))));
        assertFalse(instance_2.isTotalInstance());

        // remove
        registry.remove(distributionSummary_1);

        assertThat(mfHistogramListener.notificationCount(), is(4));
        assertThat(mfHistogramListener.notification(3).type(), is(INSTANCE_REMOVED));
        assertSame(mfHistogramListener.notification(3).instance(), instance_1);

        registry.remove(distributionSummary_2);

        assertThat(mfHistogramListener.notificationCount(), is(5));
        assertThat(mfHistogramListener.notification(4).type(), is(INSTANCE_REMOVED));
        assertSame(mfHistogramListener.notification(4).instance(), instance_2);

        assertThat(mfRegistryListener.notificationCount(), is(1));
    }

    @Test
    public void timer_nonLabeled() {
        assertThat(mfRegistryListener.notificationCount(), is(0));

        Timer timer = Timer
            .builder("timer.nonLabeled")
            .register(registry);

        assertThat(timer.count(), is(0L));
        timer.record(1L, MILLISECONDS);
        assertThat(timer.count(), is(1L));
        timer.record(2L, MILLISECONDS);
        assertThat(timer.count(), is(3L));

        assertThat(mfRegistryListener.notificationCount(), is(1));
        TestMetricRegistryListener.Notification mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        com.ringcentral.platform.metrics.timer.Timer mfTimer = mfRegistryNotification.metric();
        assertThat(mfTimer.name(), is(name("timer", "nonLabeled")));

        TestMetricListener mfTimerListener = mfRegistryListener.listenerForMetric(withName("timer", "nonLabeled"));

        assertThat(mfTimerListener.notificationCount(), is(1));
        assertThat(mfTimerListener.notification(0).type(), is(INSTANCE_ADDED));
        MetricInstance instance = mfTimerListener.notification(0).instance();
        assertTrue(instance.isTotalInstance());

        registry.remove(timer);

        assertThat(mfTimerListener.notificationCount(), is(2));
        assertThat(mfTimerListener.notification(1).type(), is(INSTANCE_REMOVED));
        assertSame(mfTimerListener.notification(1).instance(), instance);

        assertThat(mfRegistryListener.notificationCount(), is(2));
        assertThat(mfRegistryListener.notification(1).type(), is(METRIC_REMOVED));
        assertThat(mfRegistryListener.notification(1).metric(), is(mfTimer));
    }

    @Test
    public void timer_labeled() {
        assertThat(mfRegistryListener.notificationCount(), is(0));

        // add
        Timer timer_1 = Timer
            .builder("timer.labeled")
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(registry);

        assertThat(timer_1.count(), is(0L));
        timer_1.record(1L, MILLISECONDS);
        assertThat(timer_1.count(), is(1L));
        timer_1.record(2L, MILLISECONDS);
        assertThat(timer_1.count(), is(3L));

        assertThat(mfRegistryListener.notificationCount(), is(1));
        TestMetricRegistryListener.Notification mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        com.ringcentral.platform.metrics.timer.Timer mfTimer = mfRegistryNotification.metric();
        assertThat(mfTimer.name(), is(name("timer", "labeled")));

        TestMetricListener mfTimerListener = mfRegistryListener.listenerForMetric(withName("timer", "labeled"));

        assertThat(mfTimerListener.notificationCount(), is(2));

        assertThat(mfTimerListener.notification(0).type(), is(INSTANCE_ADDED));
        MetricInstance totalInstance = mfTimerListener.notification(0).instance();
        assertTrue(totalInstance.isTotalInstance());

        assertThat(mfTimerListener.notification(1).type(), is(INSTANCE_ADDED));
        MetricInstance instance_1 = mfTimerListener.notification(1).instance();
        assertThat(instance_1.labelValues(), is(List.of(SERVICE.value("service_1"), SERVER.value("server_1_1"))));
        assertFalse(instance_1.isTotalInstance());

        Timer timer_2 = Timer
            .builder("timer.labeled")
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(registry);

        assertThat(timer_2.count(), is(0L));
        timer_2.record(1L, MILLISECONDS);
        assertThat(timer_2.count(), is(1L));
        timer_2.record(2L, MILLISECONDS);
        assertThat(timer_2.count(), is(3L));

        assertThat(mfRegistryListener.notificationCount(), is(1));
        mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        assertSame(mfRegistryNotification.metric(), mfTimer);

        assertThat(mfTimerListener.notificationCount(), is(3));
        assertThat(mfTimerListener.notification(2).type(), is(INSTANCE_ADDED));
        MetricInstance instance_2 = mfTimerListener.notification(2).instance();
        assertThat(instance_2.labelValues(), is(List.of(SERVICE.value("service_2"), SERVER.value("server_2_1"))));
        assertFalse(instance_2.isTotalInstance());

        // remove
        registry.remove(timer_1);

        assertThat(mfTimerListener.notificationCount(), is(4));
        assertThat(mfTimerListener.notification(3).type(), is(INSTANCE_REMOVED));
        assertSame(mfTimerListener.notification(3).instance(), instance_1);

        registry.remove(timer_2);

        assertThat(mfTimerListener.notificationCount(), is(5));
        assertThat(mfTimerListener.notification(4).type(), is(INSTANCE_REMOVED));
        assertSame(mfTimerListener.notification(4).instance(), instance_2);

        assertThat(mfRegistryListener.notificationCount(), is(1));
    }

    @Test
    public void functionTimer_nonLabeled() {
        assertThat(mfRegistryListener.notificationCount(), is(0));
        AtomicLong countSupplier = new AtomicLong();
        AtomicLong totalTimeSupplier = new AtomicLong();

        FunctionTimer funTimer = FunctionTimer
            .builder(
                "funTimer.nonLabeled",
                this,
                a -> countSupplier.incrementAndGet(),
                a -> (double)totalTimeSupplier.incrementAndGet(),
                MILLISECONDS)
            .register(registry);

        assertThat(funTimer.count(), is(1.0));
        assertThat(funTimer.count(), is(2.0));
        assertThat(funTimer.totalTime(MILLISECONDS), is(1.0));
        assertThat(funTimer.totalTime(MILLISECONDS), is(2.0));

        assertThat(mfRegistryListener.notificationCount(), is(2));
        TestMetricRegistryListener.Notification mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        LongVar countVar = mfRegistryNotification.metric();
        assertThat(countVar.name(), is(name("funTimer", "nonLabeled", "count")));

        TestMetricListener countVarListener = mfRegistryListener.listenerForMetric(withName("funTimer", "nonLabeled", "count"));

        assertThat(countVarListener.notificationCount(), is(1));
        assertThat(countVarListener.notification(0).type(), is(INSTANCE_ADDED));
        LongVarInstance countVarInstance = (LongVarInstance)countVarListener.notification(0).instance();
        assertTrue(countVarInstance.isTotalInstance());
        assertTrue(countVarInstance.isNonDecreasing());

        mfRegistryNotification = mfRegistryListener.notification(1);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        DoubleVar totalTimeVar = mfRegistryNotification.metric();
        assertThat(totalTimeVar.name(), is(name("funTimer", "nonLabeled", "totalTime")));

        TestMetricListener totalTimeVarListener = mfRegistryListener.listenerForMetric(withName("funTimer", "nonLabeled", "totalTime"));

        assertThat(totalTimeVarListener.notificationCount(), is(1));
        assertThat(totalTimeVarListener.notification(0).type(), is(INSTANCE_ADDED));
        DoubleVarInstance totalTimeVarInstance = (DoubleVarInstance)totalTimeVarListener.notification(0).instance();
        assertTrue(totalTimeVarInstance.isTotalInstance());
        assertTrue(totalTimeVarInstance.isNonDecreasing());

        registry.remove(funTimer);

        assertThat(countVarListener.notificationCount(), is(2));
        assertThat(countVarListener.notification(1).type(), is(INSTANCE_REMOVED));
        assertSame(countVarListener.notification(1).instance(), countVarInstance);

        assertThat(totalTimeVarListener.notificationCount(), is(2));
        assertThat(totalTimeVarListener.notification(1).type(), is(INSTANCE_REMOVED));
        assertSame(totalTimeVarListener.notification(1).instance(), totalTimeVarInstance);

        assertThat(mfRegistryListener.notificationCount(), is(4));
        assertThat(mfRegistryListener.notification(2).type(), is(METRIC_REMOVED));
        assertThat(mfRegistryListener.notification(2).metric(), is(countVar));
        assertThat(mfRegistryListener.notification(3).type(), is(METRIC_REMOVED));
        assertThat(mfRegistryListener.notification(3).metric(), is(totalTimeVar));
    }

    @Test
    public void funTimer_labeled() {
        assertThat(mfRegistryListener.notificationCount(), is(0));
        AtomicLong countSupplier_1 = new AtomicLong();
        AtomicLong totalTimeSupplier_1 = new AtomicLong();

        // add
        FunctionTimer funTimer_1 = FunctionTimer
            .builder(
                "funTimer.labeled",
                this,
                a -> countSupplier_1.incrementAndGet(),
                a -> (double)totalTimeSupplier_1.incrementAndGet(),
                MILLISECONDS)
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(registry);

        assertThat(funTimer_1.count(), is(1.0));
        assertThat(funTimer_1.count(), is(2.0));
        assertThat(funTimer_1.totalTime(MILLISECONDS), is(1.0));
        assertThat(funTimer_1.totalTime(MILLISECONDS), is(2.0));

        assertThat(mfRegistryListener.notificationCount(), is(2));
        TestMetricRegistryListener.Notification mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        LongVar countVar = mfRegistryNotification.metric();
        assertThat(countVar.name(), is(name("funTimer", "labeled", "count")));

        TestMetricListener countVarListener = mfRegistryListener.listenerForMetric(withName("funTimer", "labeled", "count"));

        assertThat(countVarListener.notificationCount(), is(1));
        assertThat(countVarListener.notification(0).type(), is(INSTANCE_ADDED));
        LongVarInstance countVarInstance_1 = (LongVarInstance)countVarListener.notification(0).instance();
        assertThat(countVarInstance_1.labelValues(), is(List.of(SERVICE.value("service_1"), SERVER.value("server_1_1"))));
        assertFalse(countVarInstance_1.isTotalInstance());
        assertTrue(countVarInstance_1.isNonDecreasing());

        TestMetricListener totalTimeVarListener = mfRegistryListener.listenerForMetric(withName("funTimer", "labeled", "totalTime"));

        assertThat(totalTimeVarListener.notificationCount(), is(1));
        assertThat(totalTimeVarListener.notification(0).type(), is(INSTANCE_ADDED));
        DoubleVarInstance totalTimeVarInstance_1 = (DoubleVarInstance)totalTimeVarListener.notification(0).instance();
        assertThat(totalTimeVarInstance_1.labelValues(), is(List.of(SERVICE.value("service_1"), SERVER.value("server_1_1"))));
        assertFalse(totalTimeVarInstance_1.isTotalInstance());
        assertTrue(totalTimeVarInstance_1.isNonDecreasing());

        AtomicLong countSupplier_2 = new AtomicLong();
        AtomicLong totalTimeSupplier_2 = new AtomicLong();

        FunctionTimer funTimer_2 = FunctionTimer
            .builder(
                "funTimer.labeled",
                this,
                a -> countSupplier_2.incrementAndGet(),
                a -> (double)totalTimeSupplier_2.incrementAndGet(),
                MILLISECONDS)
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(registry);

        assertThat(funTimer_2.count(), is(1.0));
        assertThat(funTimer_2.count(), is(2.0));
        assertThat(funTimer_2.totalTime(MILLISECONDS), is(1.0));
        assertThat(funTimer_2.totalTime(MILLISECONDS), is(2.0));

        assertThat(mfRegistryListener.notificationCount(), is(2));

        assertThat(countVarListener.notificationCount(), is(2));
        assertThat(countVarListener.notification(1).type(), is(INSTANCE_ADDED));
        LongVarInstance countVarInstance_2 = (LongVarInstance)countVarListener.notification(1).instance();
        assertThat(countVarInstance_2.labelValues(), is(List.of(SERVICE.value("service_2"), SERVER.value("server_2_1"))));
        assertFalse(countVarInstance_2.isTotalInstance());
        assertTrue(countVarInstance_2.isNonDecreasing());

        assertThat(totalTimeVarListener.notificationCount(), is(2));
        assertThat(totalTimeVarListener.notification(1).type(), is(INSTANCE_ADDED));
        DoubleVarInstance totalTimeVarInstance_2 = (DoubleVarInstance)totalTimeVarListener.notification(1).instance();
        assertThat(totalTimeVarInstance_2.labelValues(), is(List.of(SERVICE.value("service_2"), SERVER.value("server_2_1"))));
        assertFalse(totalTimeVarInstance_2.isTotalInstance());
        assertTrue(totalTimeVarInstance_2.isNonDecreasing());

        // remove
        registry.remove(funTimer_1);

        assertThat(countVarListener.notificationCount(), is(3));
        assertThat(countVarListener.notification(2).type(), is(INSTANCE_REMOVED));
        assertSame(countVarListener.notification(2).instance(), countVarInstance_1);

        assertThat(totalTimeVarListener.notificationCount(), is(3));
        assertThat(totalTimeVarListener.notification(2).type(), is(INSTANCE_REMOVED));
        assertSame(totalTimeVarListener.notification(2).instance(), totalTimeVarInstance_1);

        registry.remove(funTimer_2);

        assertThat(countVarListener.notificationCount(), is(4));
        assertThat(countVarListener.notification(3).type(), is(INSTANCE_REMOVED));
        assertSame(countVarListener.notification(3).instance(), countVarInstance_2);

        assertThat(totalTimeVarListener.notificationCount(), is(4));
        assertThat(totalTimeVarListener.notification(3).type(), is(INSTANCE_REMOVED));
        assertSame(totalTimeVarListener.notification(3).instance(), totalTimeVarInstance_2);

        assertThat(mfRegistryListener.notificationCount(), is(2));
    }

    @Test
    public void longTaskTimer_nonLabeled() {
        assertThat(mfRegistryListener.notificationCount(), is(0));

        LongTaskTimer longTaskTimer = LongTaskTimer
            .builder("longTaskTimer.nonLabeled")
            .register(registry);

        assertThat(longTaskTimer.activeTasks(), is(0));
        longTaskTimer.start();
        assertThat(longTaskTimer.activeTasks(), is(1));

        assertThat(mfRegistryListener.notificationCount(), is(3));
        TestMetricRegistryListener.Notification mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        LongVar activeTasksVar = mfRegistryNotification.metric();
        assertThat(activeTasksVar.name(), is(name("longTaskTimer", "nonLabeled", "activeTasks")));

        TestMetricListener activeTasksVarListener = mfRegistryListener.listenerForMetric(withName("longTaskTimer", "nonLabeled", "activeTasks"));

        assertThat(activeTasksVarListener.notificationCount(), is(1));
        assertThat(activeTasksVarListener.notification(0).type(), is(INSTANCE_ADDED));
        LongVarInstance countVarInstance = (LongVarInstance)activeTasksVarListener.notification(0).instance();
        assertTrue(countVarInstance.isTotalInstance());
        assertFalse(countVarInstance.isNonDecreasing());

        mfRegistryNotification = mfRegistryListener.notification(1);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        DoubleVar durationVar = mfRegistryNotification.metric();
        assertThat(durationVar.name(), is(name("longTaskTimer", "nonLabeled", "duration")));

        TestMetricListener durationVarListener = mfRegistryListener.listenerForMetric(withName("longTaskTimer", "nonLabeled", "duration"));

        assertThat(durationVarListener.notificationCount(), is(1));
        assertThat(durationVarListener.notification(0).type(), is(INSTANCE_ADDED));
        DoubleVarInstance durationVarInstance = (DoubleVarInstance)durationVarListener.notification(0).instance();
        assertTrue(durationVarInstance.isTotalInstance());
        assertFalse(durationVarInstance.isNonDecreasing());

        mfRegistryNotification = mfRegistryListener.notification(2);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        DoubleVar maxVar = mfRegistryNotification.metric();
        assertThat(maxVar.name(), is(name("longTaskTimer", "nonLabeled", "max")));

        TestMetricListener maxVarListener = mfRegistryListener.listenerForMetric(withName("longTaskTimer", "nonLabeled", "max"));

        assertThat(maxVarListener.notificationCount(), is(1));
        assertThat(maxVarListener.notification(0).type(), is(INSTANCE_ADDED));
        DoubleVarInstance maxVarInstance = (DoubleVarInstance)maxVarListener.notification(0).instance();
        assertTrue(maxVarInstance.isTotalInstance());
        assertFalse(maxVarInstance.isNonDecreasing());

        registry.remove(longTaskTimer);

        assertThat(activeTasksVarListener.notificationCount(), is(2));
        assertThat(activeTasksVarListener.notification(1).type(), is(INSTANCE_REMOVED));
        assertSame(activeTasksVarListener.notification(1).instance(), countVarInstance);

        assertThat(durationVarListener.notificationCount(), is(2));
        assertThat(durationVarListener.notification(1).type(), is(INSTANCE_REMOVED));
        assertSame(durationVarListener.notification(1).instance(), durationVarInstance);

        assertThat(maxVarListener.notificationCount(), is(2));
        assertThat(maxVarListener.notification(1).type(), is(INSTANCE_REMOVED));
        assertSame(maxVarListener.notification(1).instance(), maxVarInstance);

        assertThat(mfRegistryListener.notificationCount(), is(6));
        assertThat(mfRegistryListener.notification(3).type(), is(METRIC_REMOVED));
        assertThat(mfRegistryListener.notification(3).metric(), is(activeTasksVar));
        assertThat(mfRegistryListener.notification(4).type(), is(METRIC_REMOVED));
        assertThat(mfRegistryListener.notification(4).metric(), is(durationVar));
        assertThat(mfRegistryListener.notification(5).type(), is(METRIC_REMOVED));
        assertThat(mfRegistryListener.notification(5).metric(), is(maxVar));
    }

    @Test
    public void longTaskTimer_labeled() {
        assertThat(mfRegistryListener.notificationCount(), is(0));

        // add
        LongTaskTimer longTaskTimer_1 = LongTaskTimer
            .builder("longTaskTimer.labeled")
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(registry);

        assertThat(longTaskTimer_1.activeTasks(), is(0));
        longTaskTimer_1.start();
        assertThat(longTaskTimer_1.activeTasks(), is(1));

        assertThat(mfRegistryListener.notificationCount(), is(3));
        TestMetricRegistryListener.Notification mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        LongVar countVar = mfRegistryNotification.metric();
        assertThat(countVar.name(), is(name("longTaskTimer", "labeled", "activeTasks")));

        TestMetricListener activeTasksVarListener = mfRegistryListener.listenerForMetric(withName("longTaskTimer", "labeled", "activeTasks"));

        assertThat(activeTasksVarListener.notificationCount(), is(1));
        assertThat(activeTasksVarListener.notification(0).type(), is(INSTANCE_ADDED));
        LongVarInstance activeTasksVarInstance_1 = (LongVarInstance)activeTasksVarListener.notification(0).instance();
        assertThat(activeTasksVarInstance_1.labelValues(), is(List.of(SERVICE.value("service_1"), SERVER.value("server_1_1"))));
        assertFalse(activeTasksVarInstance_1.isTotalInstance());
        assertFalse(activeTasksVarInstance_1.isNonDecreasing());

        TestMetricListener durationVarListener = mfRegistryListener.listenerForMetric(withName("longTaskTimer", "labeled", "duration"));

        assertThat(durationVarListener.notificationCount(), is(1));
        assertThat(durationVarListener.notification(0).type(), is(INSTANCE_ADDED));
        DoubleVarInstance durationVarInstance_1 = (DoubleVarInstance)durationVarListener.notification(0).instance();
        assertThat(durationVarInstance_1.labelValues(), is(List.of(SERVICE.value("service_1"), SERVER.value("server_1_1"))));
        assertFalse(durationVarInstance_1.isTotalInstance());
        assertFalse(durationVarInstance_1.isNonDecreasing());

        TestMetricListener maxVarListener = mfRegistryListener.listenerForMetric(withName("longTaskTimer", "labeled", "max"));

        assertThat(maxVarListener.notificationCount(), is(1));
        assertThat(maxVarListener.notification(0).type(), is(INSTANCE_ADDED));
        DoubleVarInstance maxVarInstance_1 = (DoubleVarInstance)maxVarListener.notification(0).instance();
        assertThat(maxVarInstance_1.labelValues(), is(List.of(SERVICE.value("service_1"), SERVER.value("server_1_1"))));
        assertFalse(maxVarInstance_1.isTotalInstance());
        assertFalse(maxVarInstance_1.isNonDecreasing());

        LongTaskTimer longTaskTimer_2 = LongTaskTimer
            .builder("longTaskTimer.labeled")
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(registry);

        assertThat(longTaskTimer_2.activeTasks(), is(0));
        longTaskTimer_2.start();
        assertThat(longTaskTimer_2.activeTasks(), is(1));

        assertThat(mfRegistryListener.notificationCount(), is(3));

        assertThat(activeTasksVarListener.notificationCount(), is(2));
        assertThat(activeTasksVarListener.notification(1).type(), is(INSTANCE_ADDED));
        LongVarInstance activeTasksVarInstance_2 = (LongVarInstance)activeTasksVarListener.notification(1).instance();
        assertThat(activeTasksVarInstance_2.labelValues(), is(List.of(SERVICE.value("service_2"), SERVER.value("server_2_1"))));
        assertFalse(activeTasksVarInstance_2.isTotalInstance());
        assertFalse(activeTasksVarInstance_2.isNonDecreasing());

        assertThat(durationVarListener.notificationCount(), is(2));
        assertThat(durationVarListener.notification(1).type(), is(INSTANCE_ADDED));
        DoubleVarInstance durationVarInstance_2 = (DoubleVarInstance)durationVarListener.notification(1).instance();
        assertThat(durationVarInstance_2.labelValues(), is(List.of(SERVICE.value("service_2"), SERVER.value("server_2_1"))));
        assertFalse(durationVarInstance_2.isTotalInstance());
        assertFalse(durationVarInstance_2.isNonDecreasing());

        assertThat(maxVarListener.notificationCount(), is(2));
        assertThat(maxVarListener.notification(1).type(), is(INSTANCE_ADDED));
        DoubleVarInstance maxVarInstance_2 = (DoubleVarInstance)durationVarListener.notification(1).instance();
        assertThat(maxVarInstance_2.labelValues(), is(List.of(SERVICE.value("service_2"), SERVER.value("server_2_1"))));
        assertFalse(maxVarInstance_2.isTotalInstance());
        assertFalse(maxVarInstance_2.isNonDecreasing());

        // remove
        registry.remove(longTaskTimer_1);

        assertThat(activeTasksVarListener.notificationCount(), is(3));
        assertThat(activeTasksVarListener.notification(2).type(), is(INSTANCE_REMOVED));
        assertSame(activeTasksVarListener.notification(2).instance(), activeTasksVarInstance_1);

        assertThat(durationVarListener.notificationCount(), is(3));
        assertThat(durationVarListener.notification(2).type(), is(INSTANCE_REMOVED));
        assertSame(durationVarListener.notification(2).instance(), durationVarInstance_1);

        assertThat(maxVarListener.notificationCount(), is(3));
        assertThat(maxVarListener.notification(2).type(), is(INSTANCE_REMOVED));
        assertSame(maxVarListener.notification(2).instance(), maxVarInstance_1);

        registry.remove(longTaskTimer_2);

        assertThat(activeTasksVarListener.notificationCount(), is(4));
        assertThat(activeTasksVarListener.notification(3).type(), is(INSTANCE_REMOVED));
        assertSame(activeTasksVarListener.notification(3).instance(), activeTasksVarInstance_2);

        assertThat(durationVarListener.notificationCount(), is(4));
        assertThat(durationVarListener.notification(3).type(), is(INSTANCE_REMOVED));
        assertSame(durationVarListener.notification(3).instance(), maxVarInstance_2);

        assertThat(durationVarListener.notificationCount(), is(4));
        assertThat(durationVarListener.notification(3).type(), is(INSTANCE_REMOVED));
        assertSame(durationVarListener.notification(3).instance(), maxVarInstance_2);

        assertThat(mfRegistryListener.notificationCount(), is(3));
    }

    @Test
    public void meter_labeled() {
        assertThat(mfRegistryListener.notificationCount(), is(0));

        AtomicLong durationSupplier_1 = new AtomicLong();
        AtomicLong maxSupplier_1 = new AtomicLong();

        // add
        Meter meter_1 = Meter
            .builder(
                "meter.labeled",
                Meter.Type.OTHER,
                List.of(
                    new Measurement(() -> (double)durationSupplier_1.incrementAndGet(), Statistic.DURATION),
                    new Measurement(() -> (double)maxSupplier_1.incrementAndGet(), Statistic.MAX)))
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(registry);

        assertThat(mfRegistryListener.notificationCount(), is(1));
        TestMetricRegistryListener.Notification mfRegistryNotification = mfRegistryListener.notification(0);
        assertThat(mfRegistryNotification.type(), is(METRIC_ADDED));
        DoubleVar mfVar = mfRegistryNotification.metric();
        assertThat(mfVar.name(), is(name("meter", "labeled")));

        TestMetricListener mfVarListener = mfRegistryListener.listenerForMetric(withName("meter", "labeled"));
        assertThat(mfVarListener.notificationCount(), is(2));

        assertThat(mfVarListener.notification(0).type(), is(INSTANCE_ADDED));
        DoubleVarInstance mfVarInstance_1 = (DoubleVarInstance)mfVarListener.notification(0).instance();

        assertThat(
            mfVarInstance_1.labelValues(),
            is(List.of(
                SERVICE.value("service_1"),
                SERVER.value("server_1_1"),
                STATISTIC.value(Statistic.DURATION.getTagValueRepresentation()))));

        assertFalse(mfVarInstance_1.isTotalInstance());
        assertFalse(mfVarInstance_1.isNonDecreasing());

        assertThat(mfVarListener.notification(1).type(), is(INSTANCE_ADDED));
        DoubleVarInstance mfVarInstance_2 = (DoubleVarInstance)mfVarListener.notification(1).instance();

        assertThat(
            mfVarInstance_2.labelValues(),
            is(List.of(
                SERVICE.value("service_1"),
                SERVER.value("server_1_1"),
                STATISTIC.value(Statistic.MAX.getTagValueRepresentation()))));

        assertFalse(mfVarInstance_2.isTotalInstance());
        assertFalse(mfVarInstance_2.isNonDecreasing());

        AtomicLong durationSupplier_2 = new AtomicLong();
        AtomicLong maxSupplier_2 = new AtomicLong();

        Meter meter_2 = Meter
            .builder(
                "meter.labeled",
                Meter.Type.OTHER,
                List.of(
                    new Measurement(() -> (double) durationSupplier_2.incrementAndGet(), Statistic.DURATION),
                    new Measurement(() -> (double) maxSupplier_2.incrementAndGet(), Statistic.MAX)))
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(registry);

        assertThat(mfRegistryListener.notificationCount(), is(1));

        assertThat(mfVarListener.notificationCount(), is(4));

        assertThat(mfVarListener.notification(2).type(), is(INSTANCE_ADDED));
        DoubleVarInstance mfVarInstance_3 = (DoubleVarInstance)mfVarListener.notification(2).instance();

        assertThat(
            mfVarInstance_3.labelValues(),
            is(List.of(
                SERVICE.value("service_2"),
                SERVER.value("server_2_1"),
                STATISTIC.value(Statistic.DURATION.getTagValueRepresentation()))));

        assertFalse(mfVarInstance_3.isTotalInstance());
        assertFalse(mfVarInstance_3.isNonDecreasing());

        assertThat(mfVarListener.notification(3).type(), is(INSTANCE_ADDED));
        DoubleVarInstance mfVarInstance_4 = (DoubleVarInstance)mfVarListener.notification(3).instance();

        assertThat(
            mfVarInstance_4.labelValues(),
            is(List.of(
                SERVICE.value("service_2"),
                SERVER.value("server_2_1"),
                STATISTIC.value(Statistic.MAX.getTagValueRepresentation()))));

        assertFalse(mfVarInstance_4.isTotalInstance());
        assertFalse(mfVarInstance_4.isNonDecreasing());

        // remove
        registry.remove(meter_1);

        assertThat(mfVarListener.notificationCount(), is(6));
        assertThat(mfVarListener.notification(4).type(), is(INSTANCE_REMOVED));
        assertSame(mfVarListener.notification(4).instance(), mfVarInstance_1);

        assertThat(mfVarListener.notification(5).type(), is(INSTANCE_REMOVED));
        assertSame(mfVarListener.notification(5).instance(), mfVarInstance_2);

        registry.remove(meter_2);

        assertThat(mfVarListener.notificationCount(), is(8));
        assertThat(mfVarListener.notification(6).type(), is(INSTANCE_REMOVED));
        assertSame(mfVarListener.notification(6).instance(), mfVarInstance_3);

        assertThat(mfVarListener.notification(7).type(), is(INSTANCE_REMOVED));
        assertSame(mfVarListener.notification(7).instance(), mfVarInstance_4);

        assertThat(mfRegistryListener.notificationCount(), is(1));
    }
}