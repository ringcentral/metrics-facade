package com.ringcentral.platform.metrics.histogram;

import com.ringcentral.platform.metrics.AbstractMeter;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.histogram.configs.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.counter.Counter.*;

public abstract class AbstractHistogram<MI> extends AbstractMeter<
    MI,
    HistogramInstanceConfig,
    HistogramSliceConfig,
    HistogramConfig> implements Histogram {

    public static final Set<HistogramMeasurable> DEFAULT_HISTOGRAM_MEASURABLES = Set.of(
        COUNT,
        MIN,
        MAX,
        MEAN,
        PERCENTILE_50,
        PERCENTILE_90,
        PERCENTILE_99);

    protected AbstractHistogram(
        MetricName name,
        HistogramConfig config,
        MeasurableValueProvidersProvider<MI> measurableValueProvidersProvider,
        MeterImplMaker<MI, HistogramInstanceConfig, HistogramSliceConfig, HistogramConfig> meterImplMaker,
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
    public void update(long value, MetricDimensionValues dimensionValues) {
        super.update(value, dimensionValues);
    }
}
