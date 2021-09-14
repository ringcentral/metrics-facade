package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.time.Duration;
import java.util.*;

public interface MeterSliceConfig<IC extends MeterInstanceConfig> {

    interface LevelInstanceNameProvider {
        MetricName nameForLevelInstance(List<MetricDimensionValue> values);
    }

    boolean isEnabled();
    MetricName name();

    default boolean hasPredicate() {
        return predicate() != null;
    }

    MetricDimensionValuesPredicate predicate();

    default boolean hasDimensions() {
        return dimensions() != null && !dimensions().isEmpty();
    }

    List<MetricDimension> dimensions();

    default boolean hasEffectiveMaxDimensionalInstances() {
        return hasMaxDimensionalInstances() && maxDimensionalInstances() < Integer.MAX_VALUE;
    }

    default boolean hasMaxDimensionalInstances() {
        return maxDimensionalInstances() != null;
    }

    Integer maxDimensionalInstances();

    default boolean isDimensionalInstanceExpirationEnabled() {
        Duration t = dimensionalInstanceExpirationTime();
        return t != null && !t.isZero() && !t.isNegative();
    }

    Duration dimensionalInstanceExpirationTime();

    default boolean hasMeasurables() {
        return measurables() != null && !measurables().isEmpty();
    }

    Set<? extends Measurable> measurables();

    boolean isTotalEnabled();
    IC totalInstanceConfig();
    boolean areLevelsEnabled();

    default boolean hasLevelInstanceNameProvider() {
        return levelInstanceNameProvider() != null;
    }

    LevelInstanceNameProvider levelInstanceNameProvider();

    default boolean hasLevelInstanceConfigFor(MetricDimension dimension) {
        return hasLevelInstanceConfigs() && levelInstanceConfigs().containsKey(dimension);
    }

    default boolean hasLevelInstanceConfigs() {
        return levelInstanceConfigs() != null && !levelInstanceConfigs().isEmpty();
    }

    Map<MetricDimension, IC> levelInstanceConfigs();

    default boolean hasDefaultLevelInstanceConfig() {
        return defaultLevelInstanceConfig() != null;
    }

    IC defaultLevelInstanceConfig();
    boolean areOnlyConfiguredLevelsEnabled();

    MetricContext context();
}
