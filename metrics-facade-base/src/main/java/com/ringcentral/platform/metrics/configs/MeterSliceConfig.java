package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.time.Duration;
import java.util.*;

public interface MeterSliceConfig<IC extends MeterInstanceConfig> {

    interface LevelInstanceNameProvider {
        MetricName nameForLevelInstance(List<LabelValue> values);
    }

    boolean isEnabled();
    MetricName name();

    default boolean hasPredicate() {
        return predicate() != null;
    }

    LabelValuesPredicate predicate();

    default boolean hasLabels() {
        return labels() != null && !labels().isEmpty();
    }

    List<Label> labels();

    default boolean hasEffectiveMaxLabeledInstances() {
        return hasMaxLabeledInstances() && maxLabeledInstances() < Integer.MAX_VALUE;
    }

    default boolean hasMaxLabeledInstances() {
        return maxLabeledInstances() != null;
    }

    Integer maxLabeledInstances();

    default boolean isLabeledInstanceExpirationEnabled() {
        Duration t = labeledInstanceExpirationTime();
        return t != null && !t.isZero() && !t.isNegative();
    }

    Duration labeledInstanceExpirationTime();

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

    default boolean hasLevelInstanceConfigFor(Label label) {
        return hasLevelInstanceConfigs() && levelInstanceConfigs().containsKey(label);
    }

    default boolean hasLevelInstanceConfigs() {
        return levelInstanceConfigs() != null && !levelInstanceConfigs().isEmpty();
    }

    Map<Label, IC> levelInstanceConfigs();

    default boolean hasDefaultLevelInstanceConfig() {
        return defaultLevelInstanceConfig() != null;
    }

    IC defaultLevelInstanceConfig();
    boolean areOnlyConfiguredLevelsEnabled();

    MetricContext context();
}
