package com.ringcentral.platform.metrics.rate;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import com.ringcentral.platform.metrics.AbstractMeter;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.configs.*;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;
import static com.ringcentral.platform.metrics.counter.Counter.*;
import static com.ringcentral.platform.metrics.utils.Preconditions.*;

public abstract class AbstractRate<MI> extends AbstractMeter<
    MI,
    RateInstanceConfig,
    RateSliceConfig,
    RateConfig> implements Rate {

    public static final Set<RateMeasurable> DEFAULT_RATE_MEASURABLES = Set.of(
        COUNT,
        MEAN_RATE,
        ONE_MINUTE_RATE,
        FIVE_MINUTES_RATE,
        FIFTEEN_MINUTES_RATE,
        RATE_UNIT);

    protected AbstractRate(
        MetricName name,
        RateConfig config,
        MeasurableValueProvidersProvider<MI> measurableValueProvidersProvider,
        MeterImplMaker<MI, RateInstanceConfig, RateSliceConfig, RateConfig> meterImplMaker,
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
    public void mark(long count, MetricDimensionValues dimensionValues) {
        checkArgument(count > 0L, "count <= 0");
        super.update(count, dimensionValues);
    }
}
