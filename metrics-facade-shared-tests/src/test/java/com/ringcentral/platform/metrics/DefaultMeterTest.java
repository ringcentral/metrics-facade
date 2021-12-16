package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.AbstractMeter.*;
import com.ringcentral.platform.metrics.configs.*;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.meter.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.MEAN;
import static com.ringcentral.platform.metrics.rate.Rate.ONE_MINUTE_RATE;
import static com.ringcentral.platform.metrics.timer.Timer.DURATION_UNIT;
import static java.util.stream.Collectors.toMap;

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
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<TestMeterImpl>> measurableValueProviders,
            TestMeterImpl meterImpl) {

            super(
                name,
                dimensionValues,
                totalInstance,
                dimensionalTotalInstance,
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
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<TestMeterImpl>> measurableValueProviders,
            TestMeterImpl meterImpl,
            long creationTimeMs) {

            super(
                name,
                dimensionValues,
                totalInstance,
                dimensionalTotalInstance,
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
            ScheduledExecutorService executor) {

            super(
                name,
                config,
                measurables -> (measurables.isEmpty() ? SUPPORTED_MEASURABLES : measurables).stream()
                    .collect(toMap(m -> m, m -> meterImpl -> 1L)),
                (instanceConfig, sliceConfig, meterConfig, measurables) -> new TestMeterImpl(),
                TestMeterImpl::update,
                new InstanceMaker<>() {

                    @Override
                    public AbstractMeterInstance<TestMeterImpl> makeInstance(
                        MetricName name,
                        List<MetricDimensionValue> dimensionValues,
                        boolean totalInstance,
                        boolean dimensionalTotalInstance,
                        boolean levelInstance,
                        Map<Measurable, MeasurableValueProvider<TestMeterImpl>> measurableValueProviders,
                        TestMeterImpl meterImpl) {

                        return new TestMeterInstance(
                            name,
                            dimensionValues,
                            totalInstance,
                            dimensionalTotalInstance,
                            levelInstance,
                            measurableValueProviders,
                            meterImpl);
                    }

                    @Override
                    public AbstractExpirableMeterInstance<TestMeterImpl> makeExpirableInstance(
                        MetricName name,
                        List<MetricDimensionValue> dimensionValues,
                        boolean totalInstance,
                        boolean dimensionalTotalInstance,
                        boolean levelInstance,
                        Map<Measurable, MeasurableValueProvider<TestMeterImpl>> measurableValueProviders,
                        TestMeterImpl meterImpl,
                        long creationTimeMs) {

                        return new TestExpirableMeterInstance(
                            name,
                            dimensionValues,
                            totalInstance,
                            dimensionalTotalInstance,
                            levelInstance,
                            measurableValueProviders,
                            meterImpl,
                            creationTimeMs);
                    }
                },
                timeMsProvider,
                executor);
        }
    }

    public DefaultMeterTest() {
        super(
            SUPPORTED_MEASURABLES,
            TestMeterConfigBuilder::new,
            TestMeterInstanceConfigBuilder::new,
            (name, builder, timeMsProvider, executor) -> new TestMeter(name, builder.build(), timeMsProvider, executor),
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
