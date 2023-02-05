package com.ringcentral.platform.metrics.histogram.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.MeterSliceConfig.LevelInstanceNameProvider;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.AllSliceConfigBuilder;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.histogram.HistogramMeasurable;
import com.ringcentral.platform.metrics.histogram.configs.*;
import com.ringcentral.platform.metrics.names.MetricName;

import java.time.Duration;
import java.util.*;

public class HistogramAllSliceConfigBuilder extends AllSliceConfigBuilder<
    HistogramMeasurable,
    HistogramInstanceConfig,
    HistogramSliceConfig,
    HistogramConfig,
    HistogramAllSliceConfigBuilder,
    HistogramSliceConfigBuilder,
    HistogramConfigBuilder> {

    public HistogramAllSliceConfigBuilder(HistogramConfigBuilder builder, MetricName name) {
        super(builder, name, HistogramMeasurable.class);
    }

    @Override
    public HistogramSliceConfig buildImpl(
        boolean enabled,
        MetricName name,
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

        return new DefaultHistogramSliceConfig(
            enabled,
            name,
            null,
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
