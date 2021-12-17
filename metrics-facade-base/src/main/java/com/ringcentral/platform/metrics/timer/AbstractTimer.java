package com.ringcentral.platform.metrics.timer;

import com.ringcentral.platform.metrics.AbstractMeter;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.timer.configs.*;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;

public abstract class AbstractTimer<MI> extends AbstractMeter<
    MI,
    TimerInstanceConfig,
    TimerSliceConfig,
    TimerConfig> implements Timer {

    public static final Set<TimerMeasurable> DEFAULT_TIMER_MEASURABLES = Set.of(
        Counter.COUNT,

        Rate.MEAN_RATE,
        Rate.ONE_MINUTE_RATE,
        Rate.FIVE_MINUTES_RATE,
        Rate.FIFTEEN_MINUTES_RATE,
        Rate.RATE_UNIT,

        Histogram.MIN,
        Histogram.MAX,
        Histogram.MEAN,
        Histogram.PERCENTILE_50,
        Histogram.PERCENTILE_90,
        Histogram.PERCENTILE_99,

        Timer.DURATION_UNIT);

    protected AbstractTimer(
        MetricName name,
        TimerConfig config,
        MeasurableValueProvidersProvider<MI, TimerInstanceConfig, TimerSliceConfig, TimerConfig> measurableValueProvidersProvider,
        MeterImplMaker<MI, TimerInstanceConfig, TimerSliceConfig, TimerConfig> meterImplMaker,
        MeterImplUpdater<MI> meterImplUpdater,
        InstanceMaker<MI> instanceMaker,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            measurableValueProvidersProvider,
            meterImplMaker,
            meterImplUpdater,
            instanceMaker,
            timeMsProvider,
            executor);
    }

    @Override
    public void update(long duration, MetricDimensionValues dimensionValues) {
        checkArgument(duration >= 0L, "duration < 0");
        super.update(duration, dimensionValues);
    }
}
