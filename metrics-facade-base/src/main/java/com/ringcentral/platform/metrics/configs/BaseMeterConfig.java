package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.*;

import java.util.*;

public class BaseMeterConfig extends AbstractMeterConfig<BaseMeterInstanceConfig, BaseMeterSliceConfig> {

    public BaseMeterConfig(
        boolean enabled,
        String description,
        LabelValues prefixLabelValues,
        List<Label> labels,
        LabelValuesPredicate exclusionPredicate,
        BaseMeterSliceConfig allSliceConfig,
        Set<BaseMeterSliceConfig> sliceConfigs,
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
