package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.time.Duration;
import java.util.*;

public class BaseMeterSliceConfig extends AbstractMeterSliceConfig<BaseMeterInstanceConfig> {

    public BaseMeterSliceConfig(
        boolean enabled,
        MetricName name,
        LabelValuesPredicate predicate,
        List<Label> labels,
        Integer maxLabeledInstances,
        Duration labeledInstanceExpirationTime,
        Set<? extends Measurable> measurables,
        boolean totalEnabled,
        BaseMeterInstanceConfig totalInstanceConfig,
        boolean levelsEnabled,
        LevelInstanceNameProvider levelInstanceNameProvider,
        Map<Label, BaseMeterInstanceConfig> levelInstanceConfigs,
        BaseMeterInstanceConfig defaultLevelInstanceConfig,
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
