package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.dimensions.*;

import java.util.*;

public interface MeterConfig<IC extends MeterInstanceConfig, SC extends MeterSliceConfig<IC>> extends MetricConfig {
    default boolean hasDimensions() {
        return dimensions() != null && !dimensions().isEmpty();
    }

    List<MetricDimension> dimensions();

    default boolean hasExclusionPredicate() {
        return exclusionPredicate() != null;
    }

    MetricDimensionValuesPredicate exclusionPredicate();
    SC allSliceConfig();

    default boolean hasSliceConfigs() {
        return sliceConfigs() != null && !sliceConfigs().isEmpty();
    }

    Set<SC> sliceConfigs();
}
