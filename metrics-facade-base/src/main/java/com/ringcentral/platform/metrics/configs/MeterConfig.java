package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.labels.*;

import java.util.*;

public interface MeterConfig<IC extends MeterInstanceConfig, SC extends MeterSliceConfig<IC>> extends MetricConfig {
    default boolean hasLabels() {
        return labels() != null && !labels().isEmpty();
    }

    List<Label> labels();

    default boolean hasExclusionPredicate() {
        return exclusionPredicate() != null;
    }

    LabelValuesPredicate exclusionPredicate();
    SC allSliceConfig();

    default boolean hasSliceConfigs() {
        return sliceConfigs() != null && !sliceConfigs().isEmpty();
    }

    Set<SC> sliceConfigs();
}
