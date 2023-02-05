package com.ringcentral.platform.metrics.histogram.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMeterConfig;
import com.ringcentral.platform.metrics.labels.*;

import java.util.*;

public class DefaultHistogramConfig extends AbstractMeterConfig<HistogramInstanceConfig, HistogramSliceConfig> implements HistogramConfig {

    public DefaultHistogramConfig(
        boolean enabled,
        String description,
        LabelValues prefixLabelValues,
        List<Label> labels,
        LabelValuesPredicate exclusionPredicate,
        HistogramSliceConfig allSliceConfig,
        Set<HistogramSliceConfig> sliceConfigs,
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
