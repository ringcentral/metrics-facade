package com.ringcentral.platform.metrics.counter.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMeterConfig;
import com.ringcentral.platform.metrics.labels.*;

import java.util.*;

public class DefaultCounterConfig extends AbstractMeterConfig<CounterInstanceConfig, CounterSliceConfig> implements CounterConfig {

    public DefaultCounterConfig(
        boolean enabled,
        String description,
        LabelValues prefixLabelValues,
        List<Label> labels,
        LabelValuesPredicate exclusionPredicate,
        CounterSliceConfig allSliceConfig,
        Set<CounterSliceConfig> sliceConfigs,
        MetricContext context) {

        super(
            enabled,
            description,
            prefixLabelValues,
            labels,
            exclusionPredicate,
            allSliceConfig,
            sliceConfigs,
            context);
    }
}
