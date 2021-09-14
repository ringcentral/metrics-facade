package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;

import java.util.*;

public class BaseMeterConfig extends AbstractMeterConfig<BaseMeterInstanceConfig, BaseMeterSliceConfig> {

    public BaseMeterConfig(
        boolean enabled,
        String description,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricDimensionValuesPredicate exclusionPredicate,
        BaseMeterSliceConfig allSliceConfig,
        Set<BaseMeterSliceConfig> sliceConfigs,
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
