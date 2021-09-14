package com.ringcentral.platform.metrics.counter.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMeterConfig;
import com.ringcentral.platform.metrics.dimensions.*;

import java.util.*;

public class DefaultCounterConfig extends AbstractMeterConfig<CounterInstanceConfig, CounterSliceConfig> implements CounterConfig {

    public DefaultCounterConfig(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricDimensionValuesPredicate exclusionPredicate,
        CounterSliceConfig allSliceConfig,
        Set<CounterSliceConfig> sliceConfigs,
        MetricContext context) {

        super(
            enabled,
            prefixDimensionValues,
            dimensions,
            exclusionPredicate,
            allSliceConfig,
            sliceConfigs,
            context);
    }
}
