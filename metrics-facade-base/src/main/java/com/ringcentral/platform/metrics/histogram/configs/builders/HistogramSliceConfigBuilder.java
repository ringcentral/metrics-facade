package com.ringcentral.platform.metrics.histogram.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.MeterSliceConfig.LevelInstanceNameProvider;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.SliceConfigBuilder;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.histogram.HistogramMeasurable;
import com.ringcentral.platform.metrics.histogram.configs.*;
import com.ringcentral.platform.metrics.names.MetricName;

import java.time.Duration;
import java.util.*;

public class HistogramSliceConfigBuilder extends SliceConfigBuilder<
    HistogramMeasurable,
    HistogramInstanceConfig,
    HistogramSliceConfig,
    HistogramConfig,
    HistogramAllSliceConfigBuilder,
    HistogramSliceConfigBuilder,
    HistogramConfigBuilder> {

    public HistogramSliceConfigBuilder(HistogramConfigBuilder builder, MetricName name) {
        super(builder, name, HistogramMeasurable.class);
    }

    @Override
    public HistogramSliceConfig buildImpl(
        boolean enabled,
        MetricName name,
        MetricDimensionValuesPredicate predicate,
        List<MetricDimension> dimensions,
        Integer maxDimensionalInstances,
        Duration dimensionalInstanceExpirationTime,
        Set<HistogramMeasurable> measurables,
        boolean totalEnabled,
        HistogramInstanceConfig totalInstanceConfig,
        boolean levelsEnabled,
        LevelInstanceNameProvider levelInstanceNameProvider,
        Map<MetricDimension, HistogramInstanceConfig> levelInstanceConfigs,
        HistogramInstanceConfig defaultLevelInstanceConfig,
        boolean onlyConfiguredLevelsEnabled,
        MetricContext context) {

        return new DefaultHistogramSliceConfig(
            enabled,
            name,
            predicate,
            dimensions,
            maxDimensionalInstances,
            dimensionalInstanceExpirationTime,
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
