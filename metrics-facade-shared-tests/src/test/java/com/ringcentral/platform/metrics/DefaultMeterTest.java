package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.AbstractMeter.AbstractExpirableMeterInstance;
import com.ringcentral.platform.metrics.AbstractMeter.AbstractMeterInstance;
import com.ringcentral.platform.metrics.AbstractMeter.MeasurableValueProvider;
import com.ringcentral.platform.metrics.configs.BaseMeterConfig;
import com.ringcentral.platform.metrics.configs.BaseMeterInstanceConfig;
import com.ringcentral.platform.metrics.configs.BaseMeterSliceConfig;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.measurables.AbstractMeasurableValues;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.meter.TestMeterConfigBuilder;
import com.ringcentral.platform.metrics.meter.TestMeterInstanceConfigBuilder;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.MEAN;
import static com.ringcentral.platform.metrics.rate.Rate.ONE_MINUTE_RATE;
import static com.ringcentral.platform.metrics.timer.Timer.DURATION_UNIT;
import static java.util.stream.Collectors.toMap;
import static org.mockito.Mockito.mock;

public class DefaultMeterTest extends AbstractMeterTest<
    Measurable,
    BaseMeterInstanceConfig,
    TestMeterConfigBuilder,
    DefaultMeterTest.TestMeter> {

    public static final Set<Measurable> SUPPORTED_MEASURABLES = Set.of(
        COUNT,
        ONE_MINUTE_RATE,
        MEAN,
        DURATION_UNIT);

    public static class TestMeterImpl {

        final List<Long> values = new ArrayList<>();

        public void update(Long value) {
            values.add(value);
        }

        public List<Long> values() {
            return values;
        }
    }

    public static class TestMeterInstance extends AbstractMeterInstance<TestMeterImpl> {

        final TestMeterImpl meterImpl;

        public TestMeterInstance(
            MetricName name,
            List<LabelValue> labelValues,
            boolean totalInstance,
            boolean labeledMetricTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<TestMeterImpl>> measurableValueProviders,
            TestMeterImpl meterImpl) {

            super(
                name,
                labelValues,
                totalInstance,
                labeledMetricTotalInstance,
                levelInstance,
                () -> new AbstractMeasurableValues(measurableValueProviders.keySet()) {

                    @Override
                    @SuppressWarnings("unchecked")
                    protected <V> V valueOfImpl(Measurable measurable) {
                        return (V)measurableValueProviders.get(measurable).valueFor(meterImpl);
                    }
                },
                measurableValueProviders,
                meterImpl);

            this.meterImpl = meterImpl;
        }

        public TestMeterImpl meterImpl() {
            return meterImpl;
        }
    }

    public static class TestExpirableMeterInstance extends AbstractExpirableMeterInstance<TestMeterImpl> {

        final TestMeterImpl meterImpl;

        public TestExpirableMeterInstance(
            MetricName name,
            List<LabelValue> labelValues,
            boolean totalInstance,
            boolean labeledMetricTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<TestMeterImpl>> measurableValueProviders,
            TestMeterImpl meterImpl,
            long creationTimeMs) {

            super(
                name,
                labelValues,
                totalInstance,
                labeledMetricTotalInstance,
                levelInstance,
                () -> new AbstractMeasurableValues(measurableValueProviders.keySet()) {

                    @Override
                    @SuppressWarnings("unchecked")
                    protected <V> V valueOfImpl(Measurable measurable) {
                        return (V)measurableValueProviders.get(measurable).valueFor(meterImpl);
                    }
                },
                measurableValueProviders,
                meterImpl,
                creationTimeMs);

            this.meterImpl = meterImpl;
        }

        public TestMeterImpl meterImpl() {
            return meterImpl;
        }
    }

    public static class TestMeter extends AbstractMeter<
        TestMeterImpl,
        BaseMeterInstanceConfig,
        BaseMeterSliceConfig,
        BaseMeterConfig> {

        public TestMeter(
            MetricName name,
            BaseMeterConfig config,
            TimeMsProvider timeMsProvider,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            super(
                name,
                config,
                (ic, sc, c, measurables) -> (measurables.isEmpty() ? SUPPORTED_MEASURABLES : measurables).stream()
                    .collect(toMap(m -> m, m -> meterImpl -> 1L)),
                (ic, sc, c, m, e, r) -> new TestMeterImpl(),
                TestMeterImpl::update,
                new InstanceMaker<>() {

                    @Override
                    public AbstractMeterInstance<TestMeterImpl> makeInstance(
                        MetricName name,
                        List<LabelValue> labelValues,
                        boolean totalInstance,
                        boolean labeledMetricTotalInstance,
                        boolean levelInstance,
                        Map<Measurable, MeasurableValueProvider<TestMeterImpl>> measurableValueProviders,
                        TestMeterImpl meterImpl) {

                        return new TestMeterInstance(
                            name,
                            labelValues,
                            totalInstance,
                            labeledMetricTotalInstance,
                            levelInstance,
                            measurableValueProviders,
                            meterImpl);
                    }

                    @Override
                    public AbstractExpirableMeterInstance<TestMeterImpl> makeExpirableInstance(
                        MetricName name,
                        List<LabelValue> labelValues,
                        boolean totalInstance,
                        boolean labeledMetricTotalInstance,
                        boolean levelInstance,
                        Map<Measurable, MeasurableValueProvider<TestMeterImpl>> measurableValueProviders,
                        TestMeterImpl meterImpl,
                        long creationTimeMs) {

                        return new TestExpirableMeterInstance(
                            name,
                            labelValues,
                            totalInstance,
                            labeledMetricTotalInstance,
                            levelInstance,
                            measurableValueProviders,
                            meterImpl,
                            creationTimeMs);
                    }
                },
                timeMsProvider,
                executor,
                registry);
        }
    }

    public DefaultMeterTest() {
        super(
            SUPPORTED_MEASURABLES,
            TestMeterConfigBuilder::new,
            TestMeterInstanceConfigBuilder::new,
            (name, builder, timeMsProvider, executor) ->
                new TestMeter(name, builder.build(), timeMsProvider, executor, mock(MetricRegistry.class)),
            (instance, expectedUpdateValues) -> {
                if (instance instanceof TestMeterInstance) {
                    return ((TestMeterInstance)instance).meterImpl().values().equals(expectedUpdateValues);
                } else if (instance instanceof TestExpirableMeterInstance) {
                    return ((TestExpirableMeterInstance)instance).meterImpl().values().equals(expectedUpdateValues);
                } else {
                    return false;
                }
            });
    }
}
