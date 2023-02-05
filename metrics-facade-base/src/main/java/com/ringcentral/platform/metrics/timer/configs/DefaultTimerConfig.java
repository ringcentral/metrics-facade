package com.ringcentral.platform.metrics.timer.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMeterConfig;
import com.ringcentral.platform.metrics.labels.*;

import java.util.*;

public class DefaultTimerConfig extends AbstractMeterConfig<TimerInstanceConfig, TimerSliceConfig> implements TimerConfig {

    public DefaultTimerConfig(
        boolean enabled,
        String description,
        LabelValues prefixLabelValues,
        List<Label> labels,
        LabelValuesPredicate exclusionPredicate,
        TimerSliceConfig allSliceConfig,
        Set<TimerSliceConfig> sliceConfigs,
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
