package com.ringcentral.platform.metrics.rate.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMeterConfig;
import com.ringcentral.platform.metrics.dimensions.*;

import java.util.*;

public class DefaultRateConfig extends AbstractMeterConfig<RateInstanceConfig, RateSliceConfig> implements RateConfig {

    public DefaultRateConfig(
        boolean enabled,
        String description,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricDimensionValuesPredicate exclusionPredicate,
        RateSliceConfig allSliceConfig,
        Set<RateSliceConfig> sliceConfigs,
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
