package com.ringcentral.platform.metrics.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.*;
import com.ringcentral.platform.metrics.configs.MeterSliceConfig.LevelInstanceNameProvider;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.SliceConfigBuilder;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.measurables.NothingMeasurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.time.Duration;
import java.util.*;

public class BaseMeterSliceConfigBuilder extends SliceConfigBuilder<
    NothingMeasurable,
    BaseMeterInstanceConfig,
    BaseMeterSliceConfig,
    BaseMeterConfig,
    BaseMeterAllSliceConfigBuilder,
    BaseMeterSliceConfigBuilder,
    BaseMeterConfigBuilder> {

    public BaseMeterSliceConfigBuilder(BaseMeterConfigBuilder builder, MetricName name) {
        super(builder, name, NothingMeasurable.class);
    }

    @Override
    public BaseMeterSliceConfig buildImpl(
        boolean enabled,
        MetricName name,
        LabelValuesPredicate predicate,
        List<Label> labels,
        Integer maxLabeledInstances,
        Duration labeledInstanceExpirationTime,
        Set<NothingMeasurable> measurables,
        boolean totalEnabled,
        BaseMeterInstanceConfig totalInstanceConfig,
        boolean levelsEnabled,
        LevelInstanceNameProvider levelInstanceNameProvider,
        Map<Label, BaseMeterInstanceConfig> levelInstanceConfigs,
        BaseMeterInstanceConfig defaultLevelInstanceConfig,
        boolean onlyConfiguredLevelsEnabled,
        MetricContext context) {

        return new BaseMeterSliceConfig(
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
