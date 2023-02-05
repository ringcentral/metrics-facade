package com.ringcentral.platform.metrics.meter;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.*;
import com.ringcentral.platform.metrics.configs.MeterSliceConfig.LevelInstanceNameProvider;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.SliceConfigBuilder;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.names.MetricName;

import java.time.Duration;
import java.util.*;

public class TestMeterSliceConfigBuilder extends SliceConfigBuilder<
    Measurable,
    BaseMeterInstanceConfig,
    BaseMeterSliceConfig,
    BaseMeterConfig,
    TestMeterAllSliceConfigBuilder,
    TestMeterSliceConfigBuilder,
    TestMeterConfigBuilder> {

    public TestMeterSliceConfigBuilder(TestMeterConfigBuilder builder, MetricName name) {
        super(builder, name, Measurable.class);
    }

    @Override
    public BaseMeterSliceConfig buildImpl(
        boolean enabled,
        MetricName name,
        LabelValuesPredicate predicate,
        List<Label> labels,
        Integer maxLabeledInstances,
        Duration labeledInstanceExpirationTime,
        Set<Measurable> measurables,
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
