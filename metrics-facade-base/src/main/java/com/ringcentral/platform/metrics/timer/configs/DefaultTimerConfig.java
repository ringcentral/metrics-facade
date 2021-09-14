package com.ringcentral.platform.metrics.timer.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMeterConfig;
import com.ringcentral.platform.metrics.dimensions.*;

import java.util.*;

public class DefaultTimerConfig extends AbstractMeterConfig<TimerInstanceConfig, TimerSliceConfig> implements TimerConfig {

    public DefaultTimerConfig(
        boolean enabled,
        String description,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricDimensionValuesPredicate exclusionPredicate,
        TimerSliceConfig allSliceConfig,
        Set<TimerSliceConfig> sliceConfigs,
        MetricContext context) {

        super(
            enabled,
            description,
            prefixDimensionValues,
            dimensions,
            exclusionPredicate,
            allSliceConfig,
            sliceConfigs,
            context);
    }
}
