package com.ringcentral.platform.metrics.rate.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMeterConfig;
import com.ringcentral.platform.metrics.labels.*;

import java.util.*;

public class DefaultRateConfig extends AbstractMeterConfig<RateInstanceConfig, RateSliceConfig> implements RateConfig {

    public DefaultRateConfig(
        boolean enabled,
        String description,
        LabelValues prefixLabelValues,
        List<Label> labels,
        LabelValuesPredicate exclusionPredicate,
        RateSliceConfig allSliceConfig,
        Set<RateSliceConfig> sliceConfigs,
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
