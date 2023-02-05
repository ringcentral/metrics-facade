package com.ringcentral.platform.metrics.histogram.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMeterSliceConfig;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.histogram.HistogramMeasurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.time.Duration;
import java.util.*;

public class DefaultHistogramSliceConfig extends AbstractMeterSliceConfig<HistogramInstanceConfig> implements HistogramSliceConfig {

    public DefaultHistogramSliceConfig(
        boolean enabled,
        MetricName name,
        LabelValuesPredicate predicate,
        List<Label> labels,
        Integer maxLabeledInstances,
        Duration labeledInstanceExpirationTime,
        Set<HistogramMeasurable> measurables,
        boolean totalEnabled,
        HistogramInstanceConfig totalInstanceConfig,
        boolean levelsEnabled,
        LevelInstanceNameProvider levelInstanceNameProvider,
        Map<Label, HistogramInstanceConfig> levelInstanceConfigs,
        HistogramInstanceConfig defaultLevelInstanceConfig,
        boolean onlyConfiguredLevelsEnabled,
        MetricContext context) {

        super(
            enabled,
            name,
            predicate,
            labels,
            maxLabeledInstances,
            labeledInstanceExpirationTime,
            measurables,
            totalEnabled,
            totalInstanceConfig,
            levelsEnabled,
            levelInstanceNameProvider,
            levelInstanceConfigs,
            defaultLevelInstanceConfig,
            onlyConfiguredLevelsEnabled,
            context);
    }
}
