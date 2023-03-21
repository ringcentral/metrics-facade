package com.ringcentral.platform.metrics.configs.builders;

import com.ringcentral.platform.metrics.configs.BaseMeterConfig;
import com.ringcentral.platform.metrics.configs.BaseMeterInstanceConfig;
import com.ringcentral.platform.metrics.configs.BaseMeterSliceConfig;
import com.ringcentral.platform.metrics.configs.MeterSliceConfig.LevelInstanceNameProvider;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.InstanceConfigBuilder;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelValuesPredicate;
import com.ringcentral.platform.metrics.measurables.NothingMeasurable;
import com.ringcentral.platform.metrics.names.MetricName;
import org.junit.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.ringcentral.platform.metrics.configs.builders.BaseMeterConfigBuilder.meter;
import static com.ringcentral.platform.metrics.configs.builders.BaseMeterConfigBuilder.withMeter;
import static com.ringcentral.platform.metrics.configs.builders.BaseMeterInstanceConfigBuilder.meterInstance;
import static com.ringcentral.platform.metrics.labels.AllLabelValuesPredicate.labelValuesMatchingAll;
import static com.ringcentral.platform.metrics.labels.AnyLabelValuesPredicate.labelValuesMatchingAny;
import static com.ringcentral.platform.metrics.labels.LabelValues.labelValues;
import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class BaseMeterConfigBuilderTest {

    static final LabelValuesPredicate SLICE_PREDICATE_1 = mock(LabelValuesPredicate.class);
    static final LabelValuesPredicate SLICE_PREDICATE_2 = mock(LabelValuesPredicate.class);

    static final Label LABEL_1 = new Label("label_1");
    static final Label LABEL_2 = new Label("label_2");
    static final Label LABEL_3 = new Label("label_3");
    static final Label LABEL_4 = new Label("label_4");
    static final Label LABEL_5 = new Label("label_5");
    static final Label LABEL_6 = new Label("label_6");
    static final Label LABEL_7 = new Label("label_7");
    static final Label LABEL_8 = new Label("label_8");
    static final Label LABEL_9 = new Label("label_9");
    static final Label LABEL_10 = new Label("label_10");

    static final LabelValuesPredicate EXCLUSION_PREDICATE_1 = labelValuesMatchingAll(LABEL_1.mask("*1*"));
    static final LabelValuesPredicate EXCLUSION_PREDICATE_2 = labelValuesMatchingAny(LABEL_2.mask("*1*"));

    static final NothingMeasurable MEASURABLE_1 = mock(NothingMeasurable.class);
    static final NothingMeasurable MEASURABLE_2 = mock(NothingMeasurable.class);
    static final NothingMeasurable MEASURABLE_3 = mock(NothingMeasurable.class);
    static final NothingMeasurable MEASURABLE_4 = mock(NothingMeasurable.class);
    static final NothingMeasurable MEASURABLE_5 = mock(NothingMeasurable.class);
    static final NothingMeasurable MEASURABLE_6 = mock(NothingMeasurable.class);
    static final NothingMeasurable MEASURABLE_7 = mock(NothingMeasurable.class);
    static final NothingMeasurable MEASURABLE_8 = mock(NothingMeasurable.class);

    static final LevelInstanceNameProvider LEVEL_INSTANCE_NAME_PROVIDER_1 = mock(LevelInstanceNameProvider.class);
    static final LevelInstanceNameProvider LEVEL_INSTANCE_NAME_PROVIDER_2 = mock(LevelInstanceNameProvider.class);
    static final LevelInstanceNameProvider LEVEL_INSTANCE_NAME_PROVIDER_3 = mock(LevelInstanceNameProvider.class);

    static final InstanceConfigBuilder<NothingMeasurable, BaseMeterInstanceConfig, BaseMeterInstanceConfigBuilder>
        LEVEL_INSTANCE_CONFIG_BUILDER_4 = meterInstance().name(name("levelInstanceName_4")).measurables(MEASURABLE_1).put("k_4", "v_4");

    static final InstanceConfigBuilder<NothingMeasurable, BaseMeterInstanceConfig, BaseMeterInstanceConfigBuilder>
        LEVEL_INSTANCE_CONFIG_BUILDER_5 = meterInstance().name(name("levelInstanceName_5")).measurables(MEASURABLE_1).put("k_5", "v_5");

    static final InstanceConfigBuilder<NothingMeasurable, BaseMeterInstanceConfig, BaseMeterInstanceConfigBuilder>
        LEVEL_INSTANCE_CONFIG_BUILDER_6 = meterInstance().name(name("levelInstanceName_6")).measurables(MEASURABLE_1).put("k_6", "v_6");

    static final InstanceConfigBuilder<NothingMeasurable, BaseMeterInstanceConfig, BaseMeterInstanceConfigBuilder>
        LEVEL_INSTANCE_CONFIG_BUILDER_7 = meterInstance().name(name("levelInstanceName_7")).measurables(MEASURABLE_1).put("k_7", "v_7");

    @Test
    public void minConfig() {
        BaseMeterConfig config = new BaseMeterConfigBuilder().build();
        assertTrue(config.isEnabled());
        assertFalse(config.hasDescription());
        assertNull(config.description());
        assertTrue(config.prefixLabelValues().isEmpty());
        assertTrue(config.context().isEmpty());
        assertFalse(config.hasExclusionPredicate());
        assertNull(config.exclusionPredicate());
        assertTrue(config.labels().isEmpty());

        BaseMeterSliceConfig allSliceConfig = config.allSliceConfig();
        assertTrue(allSliceConfig.isEnabled());
        assertTrue(allSliceConfig.name().isEmpty());
        assertFalse(allSliceConfig.hasPredicate());
        assertNull(allSliceConfig.predicate());
        assertFalse(allSliceConfig.hasLabels());
        assertTrue(allSliceConfig.labels().isEmpty());
        assertFalse(allSliceConfig.hasMaxLabeledInstances());
        assertNull(allSliceConfig.maxLabeledInstances());
        assertFalse(allSliceConfig.isLabeledInstanceExpirationEnabled());
        assertNull(allSliceConfig.labeledInstanceExpirationTime());
        assertFalse(allSliceConfig.hasMeasurables());
        assertTrue(allSliceConfig.measurables().isEmpty());
        assertTrue(allSliceConfig.isTotalEnabled());
        assertNull(allSliceConfig.totalInstanceConfig());
        assertTrue(allSliceConfig.areLevelsEnabled());
        assertFalse(allSliceConfig.hasLevelInstanceNameProvider());
        assertNull(allSliceConfig.levelInstanceNameProvider());
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigs());
        assertTrue(allSliceConfig.levelInstanceConfigs().isEmpty());
        assertFalse(allSliceConfig.hasDefaultLevelInstanceConfig());
        assertNull(allSliceConfig.defaultLevelInstanceConfig());
        assertFalse(allSliceConfig.areOnlyConfiguredLevelsEnabled());

        assertFalse(config.hasSliceConfigs());
    }

    @Test
    public void maxConfig() {
        BaseMeterConfig config = new BaseMeterConfigBuilder()
            .disable()
            .description("description")
            .prefix(labelValues(LABEL_1.value("v_1"), LABEL_2.value("v_2")))
            .put("k_1", "v_1").put("k_2", "v_2")
            .exclude(EXCLUSION_PREDICATE_1)
            .labels(LABEL_3, LABEL_4, LABEL_5, LABEL_6, LABEL_7, LABEL_8)
            .maxLabeledInstancesPerSlice(10)
            .expireLabeledInstanceAfter(11, SECONDS)
            .measurables(MEASURABLE_1)
            // AllSlice
            .allSlice(withName("all"))
                .disable()
                .labels(LABEL_4, LABEL_5, LABEL_6, LABEL_7)
                .maxLabeledInstances(12)
                .expireLabeledInstanceAfter(13, SECONDS)
                .measurables(MEASURABLE_2)
                .disableTotal()
                .total(meterInstance()
                    .name(name("total"))
                    .measurables(MEASURABLE_3)
                    .put("k_3", "v_3"))
                .noLevels()
                .levels(
                    LEVEL_INSTANCE_NAME_PROVIDER_1,
                    Map.of(
                        LABEL_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        LABEL_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        LABEL_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true)
            // Slice 1
            .slice("slice_1", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_1)
                .labels(LABEL_4, LABEL_5, LABEL_6)
                .maxLabeledInstances(14)
                .expireLabeledInstanceAfter(15, SECONDS)
                .measurables(MEASURABLE_4)
                .disableTotal()
                .total(meterInstance()
                    .name(name("total"))
                    .measurables(MEASURABLE_5)
                    .put("k_4", "v_4"))
                .enableLevels()
                .levels(
                    LEVEL_INSTANCE_NAME_PROVIDER_2,
                    Map.of(
                        LABEL_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        LABEL_5, LEVEL_INSTANCE_CONFIG_BUILDER_5),
                    LEVEL_INSTANCE_CONFIG_BUILDER_6,
                    true)
            // Slice 2
            .slice("slice_2", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_2)
                .labels(LABEL_5, LABEL_6, LABEL_7)
                .maxLabeledInstances(16)
                .expireLabeledInstanceAfter(17, SECONDS)
                .measurables(MEASURABLE_6)
                .disableTotal()
                .total(meterInstance()
                    .name(name("total"))
                    .measurables(MEASURABLE_7)
                    .put("k_5", "v_5"))
                .enableLevels()
                .levels(
                    LEVEL_INSTANCE_NAME_PROVIDER_3,
                    Map.of(
                        LABEL_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        LABEL_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true)
            .builder().build();

        assertFalse(config.isEnabled());
        assertTrue(config.hasDescription());
        assertThat(config.description(), is("description"));
        assertThat(config.prefixLabelValues(), is(labelValues(LABEL_1.value("v_1"), LABEL_2.value("v_2"))));
        assertThat(config.context().get("k_1"), is("v_1"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertTrue(config.hasExclusionPredicate());
        assertThat(config.exclusionPredicate(), is(EXCLUSION_PREDICATE_1));
        assertThat(config.labels(), is(List.of(LABEL_3, LABEL_4, LABEL_5, LABEL_6, LABEL_7, LABEL_8)));

        // AllSlice
        BaseMeterSliceConfig allSliceConfig = config.allSliceConfig();
        assertFalse(allSliceConfig.isEnabled());
        assertThat(allSliceConfig.name(), is(name("all")));
        assertFalse(allSliceConfig.hasPredicate());
        assertNull(allSliceConfig.predicate());
        assertTrue(allSliceConfig.hasLabels());
        assertThat(allSliceConfig.labels(), is(List.of(LABEL_4, LABEL_5, LABEL_6, LABEL_7)));
        assertTrue(allSliceConfig.hasMaxLabeledInstances());
        assertThat(allSliceConfig.maxLabeledInstances(), is(12));
        assertTrue(allSliceConfig.isLabeledInstanceExpirationEnabled());
        assertThat(allSliceConfig.labeledInstanceExpirationTime(), is(Duration.ofSeconds(13)));
        assertTrue(allSliceConfig.hasMeasurables());
        assertThat(allSliceConfig.measurables(), is(Set.of(MEASURABLE_2)));

        assertFalse(allSliceConfig.isTotalEnabled());
        assertThat(allSliceConfig.totalInstanceConfig().name(), is(name("total")));
        assertThat(allSliceConfig.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_3)));
        assertThat(allSliceConfig.totalInstanceConfig().context().get("k_3"), is("v_3"));

        assertFalse(allSliceConfig.areLevelsEnabled());
        assertTrue(allSliceConfig.hasLevelInstanceNameProvider());
        assertThat(allSliceConfig.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_3));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(LABEL_4));
        assertThat(allSliceConfig.levelInstanceConfigs().get(LABEL_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(allSliceConfig.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(LABEL_6));
        assertThat(allSliceConfig.levelInstanceConfigs().get(LABEL_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(allSliceConfig.hasLevelInstanceConfigs());
        assertThat(allSliceConfig.levelInstanceConfigs().size(), is(3));
        assertTrue(allSliceConfig.hasDefaultLevelInstanceConfig());
        assertThat(allSliceConfig.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(allSliceConfig.areOnlyConfiguredLevelsEnabled());

        // Slices
        assertTrue(config.hasSliceConfigs());
        assertThat(config.sliceConfigs().size(), is(2));

        // Slice 1
        BaseMeterSliceConfig slice_1_config = config.sliceConfigs().stream()
            .filter(sc -> sc.name().equals(MetricName.of("slice_1", "suffix")))
            .findFirst().orElse(null);

        assertNotNull(slice_1_config);
        assertFalse(slice_1_config.isEnabled());
        assertThat(slice_1_config.name(), is(name("slice_1", "suffix")));
        assertTrue(slice_1_config.hasPredicate());
        assertThat(slice_1_config.predicate(), is(SLICE_PREDICATE_1));
        assertTrue(slice_1_config.hasLabels());
        assertThat(slice_1_config.labels(), is(List.of(LABEL_4, LABEL_5, LABEL_6)));
        assertTrue(slice_1_config.hasMaxLabeledInstances());
        assertThat(slice_1_config.maxLabeledInstances(), is(14));
        assertTrue(slice_1_config.isLabeledInstanceExpirationEnabled());
        assertThat(slice_1_config.labeledInstanceExpirationTime(), is(Duration.ofSeconds(15)));
        assertTrue(slice_1_config.hasMeasurables());
        assertThat(slice_1_config.measurables(), is(Set.of(MEASURABLE_4)));

        assertFalse(slice_1_config.isTotalEnabled());
        assertThat(slice_1_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_1_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_5)));
        assertThat(slice_1_config.totalInstanceConfig().context().get("k_4"), is("v_4"));

        assertTrue(slice_1_config.areLevelsEnabled());
        assertTrue(slice_1_config.hasLevelInstanceNameProvider());
        assertThat(slice_1_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_3));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(LABEL_4));
        assertThat(slice_1_config.levelInstanceConfigs().get(LABEL_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(slice_1_config.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_6));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(slice_1_config.hasLevelInstanceConfigs());
        assertThat(slice_1_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_1_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_1_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertTrue(slice_1_config.areOnlyConfiguredLevelsEnabled());

        // Slice 2
        BaseMeterSliceConfig slice_2_config = config.sliceConfigs().stream()
            .filter(sc -> sc.name().equals(MetricName.of("slice_2", "suffix")))
            .findFirst().orElse(null);

        assertNotNull(slice_2_config);
        assertFalse(slice_2_config.isEnabled());
        assertThat(slice_2_config.name(), is(name("slice_2", "suffix")));
        assertTrue(slice_2_config.hasPredicate());
        assertThat(slice_2_config.predicate(), is(SLICE_PREDICATE_2));
        assertTrue(slice_2_config.hasLabels());
        assertThat(slice_2_config.labels(), is(List.of(LABEL_5, LABEL_6, LABEL_7)));
        assertTrue(slice_2_config.hasMaxLabeledInstances());
        assertThat(slice_2_config.maxLabeledInstances(), is(16));
        assertTrue(slice_2_config.isLabeledInstanceExpirationEnabled());
        assertThat(slice_2_config.labeledInstanceExpirationTime(), is(Duration.ofSeconds(17)));
        assertTrue(slice_2_config.hasMeasurables());
        assertThat(slice_2_config.measurables(), is(Set.of(MEASURABLE_6)));

        assertFalse(slice_2_config.isTotalEnabled());
        assertThat(slice_2_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_2_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_7)));
        assertThat(slice_2_config.totalInstanceConfig().context().get("k_5"), is("v_5"));

        assertTrue(slice_2_config.areLevelsEnabled());
        assertTrue(slice_2_config.hasLevelInstanceNameProvider());
        assertThat(slice_2_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_4));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(slice_2_config.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(LABEL_6));
        assertThat(slice_2_config.levelInstanceConfigs().get(LABEL_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(slice_2_config.hasLevelInstanceConfigs());
        assertThat(slice_2_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_2_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_2_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(slice_2_config.areOnlyConfiguredLevelsEnabled());
    }

    @Test
    public void rebase() {
        BaseMeterConfigBuilder builder = meter()
            .labels(
                LABEL_3, LABEL_4, LABEL_5, LABEL_6,
                LABEL_7, LABEL_8, LABEL_9, LABEL_10);

        builder.rebase(withMeter()
            .disable()
            .description("description_1")
            .prefix(labelValues(LABEL_1.value("v_1"), LABEL_2.value("v_2")))
            .put("k_1", "v_1").put("k_2", "v_2")
            .exclude(EXCLUSION_PREDICATE_1)
            .labels(LABEL_3, LABEL_4, LABEL_5, LABEL_6, LABEL_7, LABEL_8)
            .maxLabeledInstancesPerSlice(10)
            .expireLabeledInstanceAfter(11, SECONDS)
            .measurables(MEASURABLE_1)
            // AllSlice
            .allSlice(withName("all"))
                .disable()
                .labels(LABEL_4, LABEL_5, LABEL_6, LABEL_7)
                .maxLabeledInstances(12)
                .expireLabeledInstanceAfter(13, SECONDS)
                .measurables(MEASURABLE_2)
                .disableTotal()
                .total(meterInstance()
                    .name(name("total"))
                    .measurables(MEASURABLE_3)
                    .put("k_3", "v_3"))
                .noLevels()
                .levels(
                    LEVEL_INSTANCE_NAME_PROVIDER_1,
                    Map.of(
                        LABEL_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        LABEL_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        LABEL_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true)
            // Slice 1
            .slice("slice_1", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_1)
                .labels(LABEL_4, LABEL_5, LABEL_6)
                .maxLabeledInstances(14)
                .expireLabeledInstanceAfter(15, SECONDS)
                .measurables(MEASURABLE_4)
                .disableTotal()
                .total(meterInstance()
                    .name(name("total"))
                    .measurables(MEASURABLE_5)
                    .put("k_4", "v_4"))
                .enableLevels()
                .levels(
                    LEVEL_INSTANCE_NAME_PROVIDER_2,
                    Map.of(
                        LABEL_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        LABEL_5, LEVEL_INSTANCE_CONFIG_BUILDER_5),
                    LEVEL_INSTANCE_CONFIG_BUILDER_6,
                    true)
            // Slice 2
            .slice("slice_2", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_2)
                .labels(LABEL_5, LABEL_6, LABEL_7)
                .maxLabeledInstances(16)
                .expireLabeledInstanceAfter(17, SECONDS)
                .measurables(MEASURABLE_6)
                .disableTotal()
                .total(meterInstance()
                    .name(name("total"))
                    .measurables(MEASURABLE_7)
                    .put("k_5", "v_5"))
                .enableLevels()
                .levels(
                    LEVEL_INSTANCE_NAME_PROVIDER_3,
                    Map.of(
                        LABEL_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        LABEL_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true).builder());

        BaseMeterConfig config = builder.build();

        assertFalse(config.isEnabled());
        assertThat(config.description(), is("description_1"));
        assertThat(config.prefixLabelValues(), is(labelValues(LABEL_1.value("v_1"), LABEL_2.value("v_2"))));
        assertThat(config.context().get("k_1"), is("v_1"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertTrue(config.hasExclusionPredicate());
        assertThat(config.exclusionPredicate(), is(EXCLUSION_PREDICATE_1));

        assertThat(config.labels(), is(List.of(
            LABEL_3, LABEL_4, LABEL_5, LABEL_6,
            LABEL_7, LABEL_8, LABEL_9, LABEL_10)));

        // AllSlice
        BaseMeterSliceConfig allSliceConfig = config.allSliceConfig();
        assertFalse(allSliceConfig.isEnabled());
        assertThat(allSliceConfig.name(), is(name("all")));
        assertFalse(allSliceConfig.hasPredicate());
        assertNull(allSliceConfig.predicate());
        assertTrue(allSliceConfig.hasLabels());
        assertThat(allSliceConfig.labels(), is(List.of(LABEL_4, LABEL_5, LABEL_6, LABEL_7)));
        assertTrue(allSliceConfig.hasMaxLabeledInstances());
        assertThat(allSliceConfig.maxLabeledInstances(), is(12));
        assertTrue(allSliceConfig.isLabeledInstanceExpirationEnabled());
        assertThat(allSliceConfig.labeledInstanceExpirationTime(), is(Duration.ofSeconds(13)));
        assertTrue(allSliceConfig.hasMeasurables());
        assertThat(allSliceConfig.measurables(), is(Set.of(MEASURABLE_2)));

        assertFalse(allSliceConfig.isTotalEnabled());
        assertThat(allSliceConfig.totalInstanceConfig().name(), is(name("total")));
        assertThat(allSliceConfig.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_3)));
        assertThat(allSliceConfig.totalInstanceConfig().context().get("k_3"), is("v_3"));

        assertFalse(allSliceConfig.areLevelsEnabled());
        assertTrue(allSliceConfig.hasLevelInstanceNameProvider());
        assertThat(allSliceConfig.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_3));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(LABEL_4));
        assertThat(allSliceConfig.levelInstanceConfigs().get(LABEL_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(allSliceConfig.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(LABEL_6));
        assertThat(allSliceConfig.levelInstanceConfigs().get(LABEL_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(allSliceConfig.hasLevelInstanceConfigs());
        assertThat(allSliceConfig.levelInstanceConfigs().size(), is(3));
        assertTrue(allSliceConfig.hasDefaultLevelInstanceConfig());
        assertThat(allSliceConfig.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(allSliceConfig.areOnlyConfiguredLevelsEnabled());

        // Slices
        assertTrue(config.hasSliceConfigs());
        assertThat(config.sliceConfigs().size(), is(2));

        // Slice 1
        BaseMeterSliceConfig slice_1_config = config.sliceConfigs().stream()
            .filter(sc -> sc.name().equals(MetricName.of("slice_1", "suffix")))
            .findFirst().orElse(null);

        assertNotNull(slice_1_config);
        assertFalse(slice_1_config.isEnabled());
        assertThat(slice_1_config.name(), is(name("slice_1", "suffix")));
        assertTrue(slice_1_config.hasPredicate());
        assertThat(slice_1_config.predicate(), is(SLICE_PREDICATE_1));
        assertTrue(slice_1_config.hasLabels());
        assertThat(slice_1_config.labels(), is(List.of(LABEL_4, LABEL_5, LABEL_6)));
        assertTrue(slice_1_config.hasMaxLabeledInstances());
        assertThat(slice_1_config.maxLabeledInstances(), is(14));
        assertTrue(slice_1_config.isLabeledInstanceExpirationEnabled());
        assertThat(slice_1_config.labeledInstanceExpirationTime(), is(Duration.ofSeconds(15)));
        assertTrue(slice_1_config.hasMeasurables());
        assertThat(slice_1_config.measurables(), is(Set.of(MEASURABLE_4)));

        assertFalse(slice_1_config.isTotalEnabled());
        assertThat(slice_1_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_1_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_5)));
        assertThat(slice_1_config.totalInstanceConfig().context().get("k_4"), is("v_4"));

        assertTrue(slice_1_config.areLevelsEnabled());
        assertTrue(slice_1_config.hasLevelInstanceNameProvider());
        assertThat(slice_1_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_3));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(LABEL_4));
        assertThat(slice_1_config.levelInstanceConfigs().get(LABEL_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(slice_1_config.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_6));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(slice_1_config.hasLevelInstanceConfigs());
        assertThat(slice_1_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_1_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_1_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertTrue(slice_1_config.areOnlyConfiguredLevelsEnabled());

        // Slice 2
        BaseMeterSliceConfig slice_2_config = config.sliceConfigs().stream()
            .filter(sc -> sc.name().equals(MetricName.of("slice_2", "suffix")))
            .findFirst().orElse(null);

        assertNotNull(slice_2_config);
        assertFalse(slice_2_config.isEnabled());
        assertThat(slice_2_config.name(), is(name("slice_2", "suffix")));
        assertTrue(slice_2_config.hasPredicate());
        assertThat(slice_2_config.predicate(), is(SLICE_PREDICATE_2));
        assertTrue(slice_2_config.hasLabels());
        assertThat(slice_2_config.labels(), is(List.of(LABEL_5, LABEL_6, LABEL_7)));
        assertTrue(slice_2_config.hasMaxLabeledInstances());
        assertThat(slice_2_config.maxLabeledInstances(), is(16));
        assertTrue(slice_2_config.isLabeledInstanceExpirationEnabled());
        assertThat(slice_2_config.labeledInstanceExpirationTime(), is(Duration.ofSeconds(17)));
        assertTrue(slice_2_config.hasMeasurables());
        assertThat(slice_2_config.measurables(), is(Set.of(MEASURABLE_6)));

        assertFalse(slice_2_config.isTotalEnabled());
        assertThat(slice_2_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_2_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_7)));
        assertThat(slice_2_config.totalInstanceConfig().context().get("k_5"), is("v_5"));

        assertTrue(slice_2_config.areLevelsEnabled());
        assertTrue(slice_2_config.hasLevelInstanceNameProvider());
        assertThat(slice_2_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_4));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(slice_2_config.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(LABEL_6));
        assertThat(slice_2_config.levelInstanceConfigs().get(LABEL_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(slice_2_config.hasLevelInstanceConfigs());
        assertThat(slice_2_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_2_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_2_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(slice_2_config.areOnlyConfiguredLevelsEnabled());

        builder.rebase(withMeter()
            .disable()
            .description("description_2")
            .prefix(labelValues(LABEL_1.value("v_1_2"), LABEL_2.value("v_2")))
            .put("k_1", "v_1_2").put("k_2", "v_2")
            .exclude(EXCLUSION_PREDICATE_2)
            .labels(LABEL_3, LABEL_4, LABEL_5, LABEL_6, LABEL_7, LABEL_8)
            .maxLabeledInstancesPerSlice(110)
            .expireLabeledInstanceAfter(111, SECONDS)
            .measurables(MEASURABLE_1, MEASURABLE_8)
            // AllSlice
            .allSlice(withName("all_2"))
                .disable()
                .labels(LABEL_4, LABEL_5, LABEL_6, LABEL_7)
                .maxLabeledInstances(112)
                .expireLabeledInstanceAfter(113, SECONDS)
                .measurables(MEASURABLE_2, MEASURABLE_8)
                .disableTotal()
                .total(meterInstance()
                    .name(name("total_2"))
                    .measurables(MEASURABLE_3, MEASURABLE_8)
                    .put("k_3", "v_3_2"))
                .noLevels()
                .levels(
                    LEVEL_INSTANCE_NAME_PROVIDER_1,
                    Map.of(
                        LABEL_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        LABEL_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        LABEL_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true)
            // Slice 1
            .slice("slice_1", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_1)
                .labels(LABEL_4, LABEL_5, LABEL_6)
                .maxLabeledInstances(114)
                .expireLabeledInstanceAfter(115, SECONDS)
                .measurables(MEASURABLE_4, MEASURABLE_8)
                .disableTotal()
                .total(meterInstance()
                    .name(name("total_2"))
                    .measurables(MEASURABLE_5, MEASURABLE_8)
                    .put("k_4", "v_4_2"))
                .enableLevels()
                .levels(
                    LEVEL_INSTANCE_NAME_PROVIDER_2,
                    Map.of(
                        LABEL_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        LABEL_5, LEVEL_INSTANCE_CONFIG_BUILDER_5),
                    LEVEL_INSTANCE_CONFIG_BUILDER_6,
                    true)
            // Slice 3
            .slice("slice_3", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_2)
                .labels(LABEL_5, LABEL_6, LABEL_7)
                .maxLabeledInstances(116)
                .expireLabeledInstanceAfter(117, SECONDS)
                .measurables(MEASURABLE_6, MEASURABLE_8)
                .disableTotal()
                .total(meterInstance()
                    .name(name("total_2"))
                    .measurables(MEASURABLE_7, MEASURABLE_8)
                    .put("k_5", "v_5_2"))
                .enableLevels()
                .levels(
                    LEVEL_INSTANCE_NAME_PROVIDER_3,
                    Map.of(
                        LABEL_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        LABEL_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true).builder());

        config = builder.build();

        assertFalse(config.isEnabled());
        assertThat(config.description(), is("description_1"));
        assertThat(config.prefixLabelValues(), is(labelValues(LABEL_1.value("v_1"), LABEL_2.value("v_2"))));
        assertThat(config.context().get("k_1"), is("v_1"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertTrue(config.hasExclusionPredicate());
        assertThat(config.exclusionPredicate(), is(EXCLUSION_PREDICATE_1));

        assertThat(config.labels(), is(List.of(
            LABEL_3, LABEL_4, LABEL_5, LABEL_6,
            LABEL_7, LABEL_8, LABEL_9, LABEL_10)));

        // AllSlice
        allSliceConfig = config.allSliceConfig();
        assertFalse(allSliceConfig.isEnabled());
        assertThat(allSliceConfig.name(), is(name("all")));
        assertFalse(allSliceConfig.hasPredicate());
        assertNull(allSliceConfig.predicate());
        assertTrue(allSliceConfig.hasLabels());
        assertThat(allSliceConfig.labels(), is(List.of(LABEL_4, LABEL_5, LABEL_6, LABEL_7)));
        assertTrue(allSliceConfig.hasMaxLabeledInstances());
        assertThat(allSliceConfig.maxLabeledInstances(), is(12));
        assertTrue(allSliceConfig.isLabeledInstanceExpirationEnabled());
        assertThat(allSliceConfig.labeledInstanceExpirationTime(), is(Duration.ofSeconds(13)));
        assertTrue(allSliceConfig.hasMeasurables());
        assertThat(allSliceConfig.measurables(), is(Set.of(MEASURABLE_2)));

        assertFalse(allSliceConfig.isTotalEnabled());
        assertThat(allSliceConfig.totalInstanceConfig().name(), is(name("total")));
        assertThat(allSliceConfig.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_3)));
        assertThat(allSliceConfig.totalInstanceConfig().context().get("k_3"), is("v_3"));

        assertFalse(allSliceConfig.areLevelsEnabled());
        assertTrue(allSliceConfig.hasLevelInstanceNameProvider());
        assertThat(allSliceConfig.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_3));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(LABEL_4));
        assertThat(allSliceConfig.levelInstanceConfigs().get(LABEL_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(allSliceConfig.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(LABEL_6));
        assertThat(allSliceConfig.levelInstanceConfigs().get(LABEL_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(allSliceConfig.hasLevelInstanceConfigs());
        assertThat(allSliceConfig.levelInstanceConfigs().size(), is(3));
        assertTrue(allSliceConfig.hasDefaultLevelInstanceConfig());
        assertThat(allSliceConfig.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(allSliceConfig.areOnlyConfiguredLevelsEnabled());

        // Slices
        assertTrue(config.hasSliceConfigs());
        assertThat(config.sliceConfigs().size(), is(3));

        // Slice 1
        slice_1_config = config.sliceConfigs().stream()
            .filter(sc -> sc.name().equals(MetricName.of("slice_1", "suffix")))
            .findFirst().orElse(null);

        assertNotNull(slice_1_config);
        assertFalse(slice_1_config.isEnabled());
        assertThat(slice_1_config.name(), is(name("slice_1", "suffix")));
        assertTrue(slice_1_config.hasPredicate());
        assertThat(slice_1_config.predicate(), is(SLICE_PREDICATE_1));
        assertTrue(slice_1_config.hasLabels());
        assertThat(slice_1_config.labels(), is(List.of(LABEL_4, LABEL_5, LABEL_6)));
        assertTrue(slice_1_config.hasMaxLabeledInstances());
        assertThat(slice_1_config.maxLabeledInstances(), is(14));
        assertTrue(slice_1_config.isLabeledInstanceExpirationEnabled());
        assertThat(slice_1_config.labeledInstanceExpirationTime(), is(Duration.ofSeconds(15)));
        assertTrue(slice_1_config.hasMeasurables());
        assertThat(slice_1_config.measurables(), is(Set.of(MEASURABLE_4)));

        assertFalse(slice_1_config.isTotalEnabled());
        assertThat(slice_1_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_1_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_5)));
        assertThat(slice_1_config.totalInstanceConfig().context().get("k_4"), is("v_4"));

        assertTrue(slice_1_config.areLevelsEnabled());
        assertTrue(slice_1_config.hasLevelInstanceNameProvider());
        assertThat(slice_1_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_3));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(LABEL_4));
        assertThat(slice_1_config.levelInstanceConfigs().get(LABEL_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(slice_1_config.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_6));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(slice_1_config.hasLevelInstanceConfigs());
        assertThat(slice_1_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_1_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_1_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertTrue(slice_1_config.areOnlyConfiguredLevelsEnabled());

        // Slice 2
        slice_2_config = config.sliceConfigs().stream()
            .filter(sc -> sc.name().equals(MetricName.of("slice_2", "suffix")))
            .findFirst().orElse(null);

        assertNotNull(slice_2_config);
        assertFalse(slice_2_config.isEnabled());
        assertThat(slice_2_config.name(), is(name("slice_2", "suffix")));
        assertTrue(slice_2_config.hasPredicate());
        assertThat(slice_2_config.predicate(), is(SLICE_PREDICATE_2));
        assertTrue(slice_2_config.hasLabels());
        assertThat(slice_2_config.labels(), is(List.of(LABEL_5, LABEL_6, LABEL_7)));
        assertTrue(slice_2_config.hasMaxLabeledInstances());
        assertThat(slice_2_config.maxLabeledInstances(), is(16));
        assertTrue(slice_2_config.isLabeledInstanceExpirationEnabled());
        assertThat(slice_2_config.labeledInstanceExpirationTime(), is(Duration.ofSeconds(17)));
        assertTrue(slice_2_config.hasMeasurables());
        assertThat(slice_2_config.measurables(), is(Set.of(MEASURABLE_6)));

        assertFalse(slice_2_config.isTotalEnabled());
        assertThat(slice_2_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_2_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_7)));
        assertThat(slice_2_config.totalInstanceConfig().context().get("k_5"), is("v_5"));

        assertTrue(slice_2_config.areLevelsEnabled());
        assertTrue(slice_2_config.hasLevelInstanceNameProvider());
        assertThat(slice_2_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_4));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(slice_2_config.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(LABEL_6));
        assertThat(slice_2_config.levelInstanceConfigs().get(LABEL_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(slice_2_config.hasLevelInstanceConfigs());
        assertThat(slice_2_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_2_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_2_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(slice_2_config.areOnlyConfiguredLevelsEnabled());

        // Slice 3
        BaseMeterSliceConfig slice_3_config = config.sliceConfigs().stream()
            .filter(sc -> sc.name().equals(MetricName.of("slice_3", "suffix")))
            .findFirst().orElse(null);

        assertNotNull(slice_3_config);
        assertFalse(slice_3_config.isEnabled());
        assertThat(slice_3_config.name(), is(name("slice_3", "suffix")));
        assertTrue(slice_3_config.hasPredicate());
        assertThat(slice_3_config.predicate(), is(SLICE_PREDICATE_2));
        assertTrue(slice_3_config.hasLabels());
        assertThat(slice_3_config.labels(), is(List.of(LABEL_5, LABEL_6, LABEL_7)));
        assertTrue(slice_3_config.hasMaxLabeledInstances());
        assertThat(slice_3_config.maxLabeledInstances(), is(116));
        assertTrue(slice_3_config.isLabeledInstanceExpirationEnabled());
        assertThat(slice_3_config.labeledInstanceExpirationTime(), is(Duration.ofSeconds(117)));
        assertTrue(slice_3_config.hasMeasurables());
        assertThat(slice_3_config.measurables(), is(Set.of(MEASURABLE_6, MEASURABLE_8)));

        assertFalse(slice_3_config.isTotalEnabled());
        assertThat(slice_3_config.totalInstanceConfig().name(), is(name("total_2")));
        assertThat(slice_3_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_7, MEASURABLE_8)));
        assertThat(slice_3_config.totalInstanceConfig().context().get("k_5"), is("v_5_2"));

        assertTrue(slice_3_config.areLevelsEnabled());
        assertTrue(slice_3_config.hasLevelInstanceNameProvider());
        assertThat(slice_3_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_3));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(LABEL_3));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(LABEL_4));
        assertTrue(slice_3_config.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(slice_3_config.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(slice_3_config.hasLevelInstanceConfigFor(LABEL_6));
        assertThat(slice_3_config.levelInstanceConfigs().get(LABEL_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(slice_3_config.hasLevelInstanceConfigs());
        assertThat(slice_3_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_3_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_3_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(slice_3_config.areOnlyConfiguredLevelsEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rebase_labelsNotUnique() {
        BaseMeterConfigBuilder builder = meter().labels(LABEL_1, LABEL_2);
        builder.rebase(withMeter().prefix(labelValues(LABEL_2.value("v_2"))));
    }

    @Test
    public void mod() {
        BaseMeterConfigBuilder builder = meter()
            .labels(
                LABEL_3, LABEL_4, LABEL_5, LABEL_6,
                LABEL_7, LABEL_8, LABEL_9, LABEL_10);

        builder.modify(withMeter()
            .disable()
            .description("description_1")
            .prefix(labelValues(LABEL_1.value("v_1"), LABEL_2.value("v_2")))
            .put("k_1", "v_1").put("k_2", "v_2")
            .exclude(EXCLUSION_PREDICATE_1)
            .labels(LABEL_3, LABEL_4, LABEL_5, LABEL_6, LABEL_7, LABEL_8)
            .maxLabeledInstancesPerSlice(10)
            .expireLabeledInstanceAfter(11, SECONDS)
            .measurables(MEASURABLE_1)
            // AllSlice
            .allSlice(withName("all"))
                .disable()
                .labels(LABEL_4, LABEL_5, LABEL_6, LABEL_7)
                .maxLabeledInstances(12)
                .expireLabeledInstanceAfter(13, SECONDS)
                .measurables(MEASURABLE_2)
                .disableTotal()
                .total(meterInstance()
                    .name(name("total"))
                    .measurables(MEASURABLE_3)
                    .put("k_3", "v_3"))
                .noLevels()
                .levels(
                    LEVEL_INSTANCE_NAME_PROVIDER_1,
                    Map.of(
                        LABEL_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        LABEL_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        LABEL_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true)
            // Slice 1
            .slice("slice_1", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_1)
                .labels(LABEL_4, LABEL_5, LABEL_6)
                .maxLabeledInstances(14)
                .expireLabeledInstanceAfter(15, SECONDS)
                .measurables(MEASURABLE_4)
                .disableTotal()
                .total(meterInstance()
                    .name(name("total"))
                    .measurables(MEASURABLE_5)
                    .put("k_4", "v_4"))
                .enableLevels()
                .levels(
                    LEVEL_INSTANCE_NAME_PROVIDER_2,
                    Map.of(
                        LABEL_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        LABEL_5, LEVEL_INSTANCE_CONFIG_BUILDER_5),
                    LEVEL_INSTANCE_CONFIG_BUILDER_6,
                    true)
            // Slice 2
            .slice("slice_2", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_2)
                .labels(LABEL_5, LABEL_6, LABEL_7)
                .maxLabeledInstances(16)
                .expireLabeledInstanceAfter(17, SECONDS)
                .measurables(MEASURABLE_6)
                .disableTotal()
                .total(meterInstance()
                    .name(name("total"))
                    .measurables(MEASURABLE_7)
                    .put("k_5", "v_5"))
                .enableLevels()
                .levels(
                    LEVEL_INSTANCE_NAME_PROVIDER_3,
                    Map.of(
                        LABEL_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        LABEL_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true).builder());

        BaseMeterConfig config = builder.build();

        assertFalse(config.isEnabled());
        assertThat(config.description(), is("description_1"));
        assertThat(config.prefixLabelValues(), is(labelValues(LABEL_1.value("v_1"), LABEL_2.value("v_2"))));
        assertThat(config.context().get("k_1"), is("v_1"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertTrue(config.hasExclusionPredicate());
        assertThat(config.exclusionPredicate(), is(EXCLUSION_PREDICATE_1));

        assertThat(config.labels(), is(List.of(
            LABEL_3, LABEL_4, LABEL_5, LABEL_6,
            LABEL_7, LABEL_8, LABEL_9, LABEL_10)));

        // AllSlice
        BaseMeterSliceConfig allSliceConfig = config.allSliceConfig();
        assertFalse(allSliceConfig.isEnabled());
        assertThat(allSliceConfig.name(), is(name("all")));
        assertFalse(allSliceConfig.hasPredicate());
        assertNull(allSliceConfig.predicate());
        assertTrue(allSliceConfig.hasLabels());
        assertThat(allSliceConfig.labels(), is(List.of(LABEL_4, LABEL_5, LABEL_6, LABEL_7)));
        assertTrue(allSliceConfig.hasMaxLabeledInstances());
        assertThat(allSliceConfig.maxLabeledInstances(), is(12));
        assertTrue(allSliceConfig.isLabeledInstanceExpirationEnabled());
        assertThat(allSliceConfig.labeledInstanceExpirationTime(), is(Duration.ofSeconds(13)));
        assertTrue(allSliceConfig.hasMeasurables());
        assertThat(allSliceConfig.measurables(), is(Set.of(MEASURABLE_2)));

        assertFalse(allSliceConfig.isTotalEnabled());
        assertThat(allSliceConfig.totalInstanceConfig().name(), is(name("total")));
        assertThat(allSliceConfig.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_3)));
        assertThat(allSliceConfig.totalInstanceConfig().context().get("k_3"), is("v_3"));

        assertFalse(allSliceConfig.areLevelsEnabled());
        assertTrue(allSliceConfig.hasLevelInstanceNameProvider());
        assertThat(allSliceConfig.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_3));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(LABEL_4));
        assertThat(allSliceConfig.levelInstanceConfigs().get(LABEL_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(allSliceConfig.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(LABEL_6));
        assertThat(allSliceConfig.levelInstanceConfigs().get(LABEL_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(allSliceConfig.hasLevelInstanceConfigs());
        assertThat(allSliceConfig.levelInstanceConfigs().size(), is(3));
        assertTrue(allSliceConfig.hasDefaultLevelInstanceConfig());
        assertThat(allSliceConfig.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(allSliceConfig.areOnlyConfiguredLevelsEnabled());

        // Slices
        assertTrue(config.hasSliceConfigs());
        assertThat(config.sliceConfigs().size(), is(2));

        // Slice 1
        BaseMeterSliceConfig slice_1_config = config.sliceConfigs().stream()
            .filter(sc -> sc.name().equals(MetricName.of("slice_1", "suffix")))
            .findFirst().orElse(null);

        assertNotNull(slice_1_config);
        assertFalse(slice_1_config.isEnabled());
        assertThat(slice_1_config.name(), is(name("slice_1", "suffix")));
        assertTrue(slice_1_config.hasPredicate());
        assertThat(slice_1_config.predicate(), is(SLICE_PREDICATE_1));
        assertTrue(slice_1_config.hasLabels());
        assertThat(slice_1_config.labels(), is(List.of(LABEL_4, LABEL_5, LABEL_6)));
        assertTrue(slice_1_config.hasMaxLabeledInstances());
        assertThat(slice_1_config.maxLabeledInstances(), is(14));
        assertTrue(slice_1_config.isLabeledInstanceExpirationEnabled());
        assertThat(slice_1_config.labeledInstanceExpirationTime(), is(Duration.ofSeconds(15)));
        assertTrue(slice_1_config.hasMeasurables());
        assertThat(slice_1_config.measurables(), is(Set.of(MEASURABLE_4)));

        assertFalse(slice_1_config.isTotalEnabled());
        assertThat(slice_1_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_1_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_5)));
        assertThat(slice_1_config.totalInstanceConfig().context().get("k_4"), is("v_4"));

        assertTrue(slice_1_config.areLevelsEnabled());
        assertTrue(slice_1_config.hasLevelInstanceNameProvider());
        assertThat(slice_1_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_3));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(LABEL_4));
        assertThat(slice_1_config.levelInstanceConfigs().get(LABEL_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(slice_1_config.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_6));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(slice_1_config.hasLevelInstanceConfigs());
        assertThat(slice_1_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_1_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_1_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertTrue(slice_1_config.areOnlyConfiguredLevelsEnabled());

        // Slice 2
        BaseMeterSliceConfig slice_2_config = config.sliceConfigs().stream()
            .filter(sc -> sc.name().equals(MetricName.of("slice_2", "suffix")))
            .findFirst().orElse(null);

        assertNotNull(slice_2_config);
        assertFalse(slice_2_config.isEnabled());
        assertThat(slice_2_config.name(), is(name("slice_2", "suffix")));
        assertTrue(slice_2_config.hasPredicate());
        assertThat(slice_2_config.predicate(), is(SLICE_PREDICATE_2));
        assertTrue(slice_2_config.hasLabels());
        assertThat(slice_2_config.labels(), is(List.of(LABEL_5, LABEL_6, LABEL_7)));
        assertTrue(slice_2_config.hasMaxLabeledInstances());
        assertThat(slice_2_config.maxLabeledInstances(), is(16));
        assertTrue(slice_2_config.isLabeledInstanceExpirationEnabled());
        assertThat(slice_2_config.labeledInstanceExpirationTime(), is(Duration.ofSeconds(17)));
        assertTrue(slice_2_config.hasMeasurables());
        assertThat(slice_2_config.measurables(), is(Set.of(MEASURABLE_6)));

        assertFalse(slice_2_config.isTotalEnabled());
        assertThat(slice_2_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_2_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_7)));
        assertThat(slice_2_config.totalInstanceConfig().context().get("k_5"), is("v_5"));

        assertTrue(slice_2_config.areLevelsEnabled());
        assertTrue(slice_2_config.hasLevelInstanceNameProvider());
        assertThat(slice_2_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_4));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(slice_2_config.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(LABEL_6));
        assertThat(slice_2_config.levelInstanceConfigs().get(LABEL_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(slice_2_config.hasLevelInstanceConfigs());
        assertThat(slice_2_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_2_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_2_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(slice_2_config.areOnlyConfiguredLevelsEnabled());

        builder.modify(withMeter()
            .disable()
            .description("description_2")
            .prefix(labelValues(LABEL_1.value("v_1_2"), LABEL_2.value("v_2")))
            .put("k_1", "v_1_2").put("k_2", "v_2")
            .exclude(EXCLUSION_PREDICATE_2)
            .labels(LABEL_3, LABEL_4, LABEL_5, LABEL_6, LABEL_7, LABEL_8)
            .maxLabeledInstancesPerSlice(110)
            .expireLabeledInstanceAfter(111, SECONDS)
            .measurables(MEASURABLE_1, MEASURABLE_8)
            // AllSlice
            .allSlice(withName("all_2"))
                .disable()
                .labels(LABEL_4, LABEL_5, LABEL_6, LABEL_7)
                .maxLabeledInstances(112)
                .expireLabeledInstanceAfter(113, SECONDS)
                .measurables(MEASURABLE_2, MEASURABLE_8)
                .disableTotal()
                .total(meterInstance()
                    .name(name("total_2"))
                    .measurables(MEASURABLE_3, MEASURABLE_8)
                    .put("k_3", "v_3_2"))
                .noLevels()
                .levels(
                    LEVEL_INSTANCE_NAME_PROVIDER_1,
                    Map.of(
                        LABEL_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        LABEL_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        LABEL_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true)
            // Slice 1
            .slice("slice_1", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_1)
                .labels(LABEL_4, LABEL_5, LABEL_6)
                .maxLabeledInstances(114)
                .expireLabeledInstanceAfter(115, SECONDS)
                .measurables(MEASURABLE_4, MEASURABLE_8)
                .disableTotal()
                .total(meterInstance()
                    .name(name("total_2"))
                    .measurables(MEASURABLE_5, MEASURABLE_8)
                    .put("k_4", "v_4_2"))
                .enableLevels()
                .levels(
                    LEVEL_INSTANCE_NAME_PROVIDER_2,
                    Map.of(
                        LABEL_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        LABEL_5, LEVEL_INSTANCE_CONFIG_BUILDER_5),
                    LEVEL_INSTANCE_CONFIG_BUILDER_6,
                    true)
            // Slice 3
            .slice("slice_3", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_2)
                .labels(LABEL_5, LABEL_6, LABEL_7)
                .maxLabeledInstances(116)
                .expireLabeledInstanceAfter(117, SECONDS)
                .measurables(MEASURABLE_6, MEASURABLE_8)
                .disableTotal()
                .total(meterInstance()
                    .name(name("total_2"))
                    .measurables(MEASURABLE_7, MEASURABLE_8)
                    .put("k_5", "v_5_2"))
                .enableLevels()
                .levels(
                    LEVEL_INSTANCE_NAME_PROVIDER_3,
                    Map.of(
                        LABEL_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        LABEL_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true).builder());

        config = builder.build();

        assertFalse(config.isEnabled());
        assertThat(config.description(), is("description_2"));
        assertThat(config.prefixLabelValues(), is(labelValues(LABEL_1.value("v_1_2"), LABEL_2.value("v_2"))));
        assertThat(config.context().get("k_1"), is("v_1_2"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertTrue(config.hasExclusionPredicate());
        assertThat(config.exclusionPredicate(), is(EXCLUSION_PREDICATE_2));

        assertThat(config.labels(), is(List.of(
            LABEL_3, LABEL_4, LABEL_5, LABEL_6,
            LABEL_7, LABEL_8, LABEL_9, LABEL_10)));

        // AllSlice
        allSliceConfig = config.allSliceConfig();
        assertFalse(allSliceConfig.isEnabled());
        assertThat(allSliceConfig.name(), is(name("all_2")));
        assertFalse(allSliceConfig.hasPredicate());
        assertNull(allSliceConfig.predicate());
        assertTrue(allSliceConfig.hasLabels());
        assertThat(allSliceConfig.labels(), is(List.of(LABEL_4, LABEL_5, LABEL_6, LABEL_7)));
        assertTrue(allSliceConfig.hasMaxLabeledInstances());
        assertThat(allSliceConfig.maxLabeledInstances(), is(112));
        assertTrue(allSliceConfig.isLabeledInstanceExpirationEnabled());
        assertThat(allSliceConfig.labeledInstanceExpirationTime(), is(Duration.ofSeconds(113)));
        assertTrue(allSliceConfig.hasMeasurables());
        assertThat(allSliceConfig.measurables(), is(Set.of(MEASURABLE_2, MEASURABLE_8)));

        assertFalse(allSliceConfig.isTotalEnabled());
        assertThat(allSliceConfig.totalInstanceConfig().name(), is(name("total_2")));
        assertThat(allSliceConfig.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_3, MEASURABLE_8)));
        assertThat(allSliceConfig.totalInstanceConfig().context().get("k_3"), is("v_3_2"));

        assertFalse(allSliceConfig.areLevelsEnabled());
        assertTrue(allSliceConfig.hasLevelInstanceNameProvider());
        assertThat(allSliceConfig.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_3));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(LABEL_4));
        assertThat(allSliceConfig.levelInstanceConfigs().get(LABEL_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(allSliceConfig.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(LABEL_6));
        assertThat(allSliceConfig.levelInstanceConfigs().get(LABEL_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(allSliceConfig.hasLevelInstanceConfigs());
        assertThat(allSliceConfig.levelInstanceConfigs().size(), is(3));
        assertTrue(allSliceConfig.hasDefaultLevelInstanceConfig());
        assertThat(allSliceConfig.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(allSliceConfig.areOnlyConfiguredLevelsEnabled());

        // Slices
        assertTrue(config.hasSliceConfigs());
        assertThat(config.sliceConfigs().size(), is(3));

        // Slice 1
        slice_1_config = config.sliceConfigs().stream()
            .filter(sc -> sc.name().equals(MetricName.of("slice_1", "suffix")))
            .findFirst().orElse(null);

        assertNotNull(slice_1_config);
        assertFalse(slice_1_config.isEnabled());
        assertThat(slice_1_config.name(), is(name("slice_1", "suffix")));
        assertTrue(slice_1_config.hasPredicate());
        assertThat(slice_1_config.predicate(), is(SLICE_PREDICATE_1));
        assertTrue(slice_1_config.hasLabels());
        assertThat(slice_1_config.labels(), is(List.of(LABEL_4, LABEL_5, LABEL_6)));
        assertTrue(slice_1_config.hasMaxLabeledInstances());
        assertThat(slice_1_config.maxLabeledInstances(), is(114));
        assertTrue(slice_1_config.isLabeledInstanceExpirationEnabled());
        assertThat(slice_1_config.labeledInstanceExpirationTime(), is(Duration.ofSeconds(115)));
        assertTrue(slice_1_config.hasMeasurables());
        assertThat(slice_1_config.measurables(), is(Set.of(MEASURABLE_4, MEASURABLE_8)));

        assertFalse(slice_1_config.isTotalEnabled());
        assertThat(slice_1_config.totalInstanceConfig().name(), is(name("total_2")));
        assertThat(slice_1_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_5, MEASURABLE_8)));
        assertThat(slice_1_config.totalInstanceConfig().context().get("k_4"), is("v_4_2"));

        assertTrue(slice_1_config.areLevelsEnabled());
        assertTrue(slice_1_config.hasLevelInstanceNameProvider());
        assertThat(slice_1_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_3));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(LABEL_4));
        assertThat(slice_1_config.levelInstanceConfigs().get(LABEL_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(slice_1_config.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_6));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(slice_1_config.hasLevelInstanceConfigs());
        assertThat(slice_1_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_1_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_1_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertTrue(slice_1_config.areOnlyConfiguredLevelsEnabled());

        // Slice 2
        slice_2_config = config.sliceConfigs().stream()
            .filter(sc -> sc.name().equals(MetricName.of("slice_2", "suffix")))
            .findFirst().orElse(null);

        assertNotNull(slice_2_config);
        assertFalse(slice_2_config.isEnabled());
        assertThat(slice_2_config.name(), is(name("slice_2", "suffix")));
        assertTrue(slice_2_config.hasPredicate());
        assertThat(slice_2_config.predicate(), is(SLICE_PREDICATE_2));
        assertTrue(slice_2_config.hasLabels());
        assertThat(slice_2_config.labels(), is(List.of(LABEL_5, LABEL_6, LABEL_7)));
        assertTrue(slice_2_config.hasMaxLabeledInstances());
        assertThat(slice_2_config.maxLabeledInstances(), is(16));
        assertTrue(slice_2_config.isLabeledInstanceExpirationEnabled());
        assertThat(slice_2_config.labeledInstanceExpirationTime(), is(Duration.ofSeconds(17)));
        assertTrue(slice_2_config.hasMeasurables());
        assertThat(slice_2_config.measurables(), is(Set.of(MEASURABLE_6)));

        assertFalse(slice_2_config.isTotalEnabled());
        assertThat(slice_2_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_2_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_7)));
        assertThat(slice_2_config.totalInstanceConfig().context().get("k_5"), is("v_5"));

        assertTrue(slice_2_config.areLevelsEnabled());
        assertTrue(slice_2_config.hasLevelInstanceNameProvider());
        assertThat(slice_2_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_4));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(slice_2_config.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(LABEL_6));
        assertThat(slice_2_config.levelInstanceConfigs().get(LABEL_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(slice_2_config.hasLevelInstanceConfigs());
        assertThat(slice_2_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_2_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_2_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(slice_2_config.areOnlyConfiguredLevelsEnabled());

        // Slice 3
        BaseMeterSliceConfig slice_3_config = config.sliceConfigs().stream()
            .filter(sc -> sc.name().equals(MetricName.of("slice_3", "suffix")))
            .findFirst().orElse(null);

        assertNotNull(slice_3_config);
        assertFalse(slice_3_config.isEnabled());
        assertThat(slice_3_config.name(), is(name("slice_3", "suffix")));
        assertTrue(slice_3_config.hasPredicate());
        assertThat(slice_3_config.predicate(), is(SLICE_PREDICATE_2));
        assertTrue(slice_3_config.hasLabels());
        assertThat(slice_3_config.labels(), is(List.of(LABEL_5, LABEL_6, LABEL_7)));
        assertTrue(slice_3_config.hasMaxLabeledInstances());
        assertThat(slice_3_config.maxLabeledInstances(), is(116));
        assertTrue(slice_3_config.isLabeledInstanceExpirationEnabled());
        assertThat(slice_3_config.labeledInstanceExpirationTime(), is(Duration.ofSeconds(117)));
        assertTrue(slice_3_config.hasMeasurables());
        assertThat(slice_3_config.measurables(), is(Set.of(MEASURABLE_6, MEASURABLE_8)));

        assertFalse(slice_3_config.isTotalEnabled());
        assertThat(slice_3_config.totalInstanceConfig().name(), is(name("total_2")));
        assertThat(slice_3_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_7, MEASURABLE_8)));
        assertThat(slice_3_config.totalInstanceConfig().context().get("k_5"), is("v_5_2"));

        assertTrue(slice_3_config.areLevelsEnabled());
        assertTrue(slice_3_config.hasLevelInstanceNameProvider());
        assertThat(slice_3_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_3));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(LABEL_1));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(LABEL_2));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(LABEL_3));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(LABEL_4));
        assertTrue(slice_3_config.hasLevelInstanceConfigFor(LABEL_5));
        assertThat(slice_3_config.levelInstanceConfigs().get(LABEL_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(slice_3_config.hasLevelInstanceConfigFor(LABEL_6));
        assertThat(slice_3_config.levelInstanceConfigs().get(LABEL_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(LABEL_7));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(LABEL_8));
        assertTrue(slice_3_config.hasLevelInstanceConfigs());
        assertThat(slice_3_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_3_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_3_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(slice_3_config.areOnlyConfiguredLevelsEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mod_labelsNotUnique() {
        BaseMeterConfigBuilder builder = meter().labels(LABEL_1, LABEL_2);
        builder.modify(withMeter().prefix(labelValues(LABEL_2.value("v_2"))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void labels_labelsNotUnique() {
        meter()
            .prefix(labelValues(LABEL_1.value("v_1")))
            .labels(LABEL_1, LABEL_2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void prefixLabelValues_labelsNotUnique() {
        meter()
            .labels(LABEL_1, LABEL_2)
            .prefix(labelValues(LABEL_2.value("v_1")));
    }
}