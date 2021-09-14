package com.ringcentral.platform.metrics.histogram.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMeterConfig;
import com.ringcentral.platform.metrics.dimensions.*;

import java.util.*;

public class DefaultHistogramConfig extends AbstractMeterConfig<HistogramInstanceConfig, HistogramSliceConfig> implements HistogramConfig {

    public DefaultHistogramConfig(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricDimensionValuesPredicate exclusionPredicate,
        HistogramSliceConfig allSliceConfig,
        Set<HistogramSliceConfig> sliceConfigs,
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
