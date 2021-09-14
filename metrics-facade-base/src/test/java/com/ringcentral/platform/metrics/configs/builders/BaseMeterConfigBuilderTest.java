package com.ringcentral.platform.metrics.configs.builders;

import com.ringcentral.platform.metrics.configs.*;
import com.ringcentral.platform.metrics.configs.MeterSliceConfig.LevelInstanceNameProvider;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.InstanceConfigBuilder;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.measurables.NothingMeasurable;
import com.ringcentral.platform.metrics.names.MetricName;
import org.junit.Test;

import java.time.Duration;
import java.util.*;

import static com.ringcentral.platform.metrics.configs.builders.BaseMeterConfigBuilder.*;
import static com.ringcentral.platform.metrics.configs.builders.BaseMeterInstanceConfigBuilder.meterInstance;
import static com.ringcentral.platform.metrics.dimensions.AllMetricDimensionValuesPredicate.dimensionValuesMatchingAll;
import static com.ringcentral.platform.metrics.dimensions.AnyMetricDimensionValuesPredicate.dimensionValuesMatchingAny;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.dimensionValues;
import static com.ringcentral.platform.metrics.names.MetricName.*;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class BaseMeterConfigBuilderTest {

    static final MetricDimensionValuesPredicate SLICE_PREDICATE_1 = mock(MetricDimensionValuesPredicate.class);
    static final MetricDimensionValuesPredicate SLICE_PREDICATE_2 = mock(MetricDimensionValuesPredicate.class);

    static final MetricDimension DIMENSION_1 = new MetricDimension("dimension_1");
    static final MetricDimension DIMENSION_2 = new MetricDimension("dimension_2");
    static final MetricDimension DIMENSION_3 = new MetricDimension("dimension_3");
    static final MetricDimension DIMENSION_4 = new MetricDimension("dimension_4");
    static final MetricDimension DIMENSION_5 = new MetricDimension("dimension_5");
    static final MetricDimension DIMENSION_6 = new MetricDimension("dimension_6");
    static final MetricDimension DIMENSION_7 = new MetricDimension("dimension_7");
    static final MetricDimension DIMENSION_8 = new MetricDimension("dimension_8");
    static final MetricDimension DIMENSION_9 = new MetricDimension("dimension_9");
    static final MetricDimension DIMENSION_10 = new MetricDimension("dimension_10");

    static final MetricDimensionValuesPredicate EXCLUSION_PREDICATE_1 = dimensionValuesMatchingAll(DIMENSION_1.mask("*1*"));
    static final MetricDimensionValuesPredicate EXCLUSION_PREDICATE_2 = dimensionValuesMatchingAny(DIMENSION_2.mask("*1*"));

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
        assertTrue(config.prefixDimensionValues().isEmpty());
        assertTrue(config.context().isEmpty());
        assertFalse(config.hasExclusionPredicate());
        assertNull(config.exclusionPredicate());
        assertTrue(config.dimensions().isEmpty());

        BaseMeterSliceConfig allSliceConfig = config.allSliceConfig();
        assertTrue(allSliceConfig.isEnabled());
        assertTrue(allSliceConfig.name().isEmpty());
        assertFalse(allSliceConfig.hasPredicate());
        assertNull(allSliceConfig.predicate());
        assertFalse(allSliceConfig.hasDimensions());
        assertTrue(allSliceConfig.dimensions().isEmpty());
        assertFalse(allSliceConfig.hasMaxDimensionalInstances());
        assertNull(allSliceConfig.maxDimensionalInstances());
        assertFalse(allSliceConfig.isDimensionalInstanceExpirationEnabled());
        assertNull(allSliceConfig.dimensionalInstanceExpirationTime());
        assertFalse(allSliceConfig.hasMeasurables());
        assertTrue(allSliceConfig.measurables().isEmpty());
        assertTrue(allSliceConfig.isTotalEnabled());
        assertNull(allSliceConfig.totalInstanceConfig());
        assertTrue(allSliceConfig.areLevelsEnabled());
        assertFalse(allSliceConfig.hasLevelInstanceNameProvider());
        assertNull(allSliceConfig.levelInstanceNameProvider());
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_1));
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
            .prefix(dimensionValues(DIMENSION_1.value("v_1"), DIMENSION_2.value("v_2")))
            .put("k_1", "v_1").put("k_2", "v_2")
            .exclude(EXCLUSION_PREDICATE_1)
            .dimensions(DIMENSION_3, DIMENSION_4, DIMENSION_5, DIMENSION_6, DIMENSION_7, DIMENSION_8)
            .maxDimensionalInstancesPerSlice(10)
            .expireDimensionalInstanceAfter(11, SECONDS)
            .measurables(MEASURABLE_1)
            // AllSlice
            .allSlice(withName("all"))
                .disable()
                .dimensions(DIMENSION_4, DIMENSION_5, DIMENSION_6, DIMENSION_7)
                .maxDimensionalInstances(12)
                .expireDimensionalInstanceAfter(13, SECONDS)
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
                        DIMENSION_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        DIMENSION_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        DIMENSION_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true)
            // Slice 1
            .slice("slice_1", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_1)
                .dimensions(DIMENSION_4, DIMENSION_5, DIMENSION_6)
                .maxDimensionalInstances(14)
                .expireDimensionalInstanceAfter(15, SECONDS)
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
                        DIMENSION_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        DIMENSION_5, LEVEL_INSTANCE_CONFIG_BUILDER_5),
                    LEVEL_INSTANCE_CONFIG_BUILDER_6,
                    true)
            // Slice 2
            .slice("slice_2", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_2)
                .dimensions(DIMENSION_5, DIMENSION_6, DIMENSION_7)
                .maxDimensionalInstances(16)
                .expireDimensionalInstanceAfter(17, SECONDS)
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
                        DIMENSION_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        DIMENSION_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true)
            .builder().build();

        assertFalse(config.isEnabled());
        assertTrue(config.hasDescription());
        assertThat(config.description(), is("description"));
        assertThat(config.prefixDimensionValues(), is(dimensionValues(DIMENSION_1.value("v_1"), DIMENSION_2.value("v_2"))));
        assertThat(config.context().get("k_1"), is("v_1"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertTrue(config.hasExclusionPredicate());
        assertThat(config.exclusionPredicate(), is(EXCLUSION_PREDICATE_1));
        assertThat(config.dimensions(), is(List.of(DIMENSION_3, DIMENSION_4, DIMENSION_5, DIMENSION_6, DIMENSION_7, DIMENSION_8)));

        // AllSlice
        BaseMeterSliceConfig allSliceConfig = config.allSliceConfig();
        assertFalse(allSliceConfig.isEnabled());
        assertThat(allSliceConfig.name(), is(name("all")));
        assertFalse(allSliceConfig.hasPredicate());
        assertNull(allSliceConfig.predicate());
        assertTrue(allSliceConfig.hasDimensions());
        assertThat(allSliceConfig.dimensions(), is(List.of(DIMENSION_4, DIMENSION_5, DIMENSION_6, DIMENSION_7)));
        assertTrue(allSliceConfig.hasMaxDimensionalInstances());
        assertThat(allSliceConfig.maxDimensionalInstances(), is(12));
        assertTrue(allSliceConfig.isDimensionalInstanceExpirationEnabled());
        assertThat(allSliceConfig.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(13)));
        assertTrue(allSliceConfig.hasMeasurables());
        assertThat(allSliceConfig.measurables(), is(Set.of(MEASURABLE_2)));

        assertFalse(allSliceConfig.isTotalEnabled());
        assertThat(allSliceConfig.totalInstanceConfig().name(), is(name("total")));
        assertThat(allSliceConfig.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_3)));
        assertThat(allSliceConfig.totalInstanceConfig().context().get("k_3"), is("v_3"));

        assertFalse(allSliceConfig.areLevelsEnabled());
        assertTrue(allSliceConfig.hasLevelInstanceNameProvider());
        assertThat(allSliceConfig.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_3));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_4));
        assertThat(allSliceConfig.levelInstanceConfigs().get(DIMENSION_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(allSliceConfig.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_6));
        assertThat(allSliceConfig.levelInstanceConfigs().get(DIMENSION_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_8));
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
        assertTrue(slice_1_config.hasDimensions());
        assertThat(slice_1_config.dimensions(), is(List.of(DIMENSION_4, DIMENSION_5, DIMENSION_6)));
        assertTrue(slice_1_config.hasMaxDimensionalInstances());
        assertThat(slice_1_config.maxDimensionalInstances(), is(14));
        assertTrue(slice_1_config.isDimensionalInstanceExpirationEnabled());
        assertThat(slice_1_config.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(15)));
        assertTrue(slice_1_config.hasMeasurables());
        assertThat(slice_1_config.measurables(), is(Set.of(MEASURABLE_4)));

        assertFalse(slice_1_config.isTotalEnabled());
        assertThat(slice_1_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_1_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_5)));
        assertThat(slice_1_config.totalInstanceConfig().context().get("k_4"), is("v_4"));

        assertTrue(slice_1_config.areLevelsEnabled());
        assertTrue(slice_1_config.hasLevelInstanceNameProvider());
        assertThat(slice_1_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_3));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_4));
        assertThat(slice_1_config.levelInstanceConfigs().get(DIMENSION_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(slice_1_config.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_6));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_8));
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
        assertTrue(slice_2_config.hasDimensions());
        assertThat(slice_2_config.dimensions(), is(List.of(DIMENSION_5, DIMENSION_6, DIMENSION_7)));
        assertTrue(slice_2_config.hasMaxDimensionalInstances());
        assertThat(slice_2_config.maxDimensionalInstances(), is(16));
        assertTrue(slice_2_config.isDimensionalInstanceExpirationEnabled());
        assertThat(slice_2_config.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(17)));
        assertTrue(slice_2_config.hasMeasurables());
        assertThat(slice_2_config.measurables(), is(Set.of(MEASURABLE_6)));

        assertFalse(slice_2_config.isTotalEnabled());
        assertThat(slice_2_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_2_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_7)));
        assertThat(slice_2_config.totalInstanceConfig().context().get("k_5"), is("v_5"));

        assertTrue(slice_2_config.areLevelsEnabled());
        assertTrue(slice_2_config.hasLevelInstanceNameProvider());
        assertThat(slice_2_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_4));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(slice_2_config.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_6));
        assertThat(slice_2_config.levelInstanceConfigs().get(DIMENSION_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_8));
        assertTrue(slice_2_config.hasLevelInstanceConfigs());
        assertThat(slice_2_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_2_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_2_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(slice_2_config.areOnlyConfiguredLevelsEnabled());
    }

    @Test
    public void rebase() {
        BaseMeterConfigBuilder builder = meter()
            .dimensions(
                DIMENSION_3, DIMENSION_4, DIMENSION_5, DIMENSION_6,
                DIMENSION_7, DIMENSION_8, DIMENSION_9, DIMENSION_10);

        builder.rebase(withMeter()
            .disable()
            .description("description_1")
            .prefix(dimensionValues(DIMENSION_1.value("v_1"), DIMENSION_2.value("v_2")))
            .put("k_1", "v_1").put("k_2", "v_2")
            .exclude(EXCLUSION_PREDICATE_1)
            .dimensions(DIMENSION_3, DIMENSION_4, DIMENSION_5, DIMENSION_6, DIMENSION_7, DIMENSION_8)
            .maxDimensionalInstancesPerSlice(10)
            .expireDimensionalInstanceAfter(11, SECONDS)
            .measurables(MEASURABLE_1)
            // AllSlice
            .allSlice(withName("all"))
                .disable()
                .dimensions(DIMENSION_4, DIMENSION_5, DIMENSION_6, DIMENSION_7)
                .maxDimensionalInstances(12)
                .expireDimensionalInstanceAfter(13, SECONDS)
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
                        DIMENSION_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        DIMENSION_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        DIMENSION_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true)
            // Slice 1
            .slice("slice_1", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_1)
                .dimensions(DIMENSION_4, DIMENSION_5, DIMENSION_6)
                .maxDimensionalInstances(14)
                .expireDimensionalInstanceAfter(15, SECONDS)
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
                        DIMENSION_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        DIMENSION_5, LEVEL_INSTANCE_CONFIG_BUILDER_5),
                    LEVEL_INSTANCE_CONFIG_BUILDER_6,
                    true)
            // Slice 2
            .slice("slice_2", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_2)
                .dimensions(DIMENSION_5, DIMENSION_6, DIMENSION_7)
                .maxDimensionalInstances(16)
                .expireDimensionalInstanceAfter(17, SECONDS)
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
                        DIMENSION_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        DIMENSION_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true).builder());

        BaseMeterConfig config = builder.build();

        assertFalse(config.isEnabled());
        assertThat(config.description(), is("description_1"));
        assertThat(config.prefixDimensionValues(), is(dimensionValues(DIMENSION_1.value("v_1"), DIMENSION_2.value("v_2"))));
        assertThat(config.context().get("k_1"), is("v_1"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertTrue(config.hasExclusionPredicate());
        assertThat(config.exclusionPredicate(), is(EXCLUSION_PREDICATE_1));

        assertThat(config.dimensions(), is(List.of(
            DIMENSION_3, DIMENSION_4, DIMENSION_5, DIMENSION_6,
            DIMENSION_7, DIMENSION_8, DIMENSION_9, DIMENSION_10)));

        // AllSlice
        BaseMeterSliceConfig allSliceConfig = config.allSliceConfig();
        assertFalse(allSliceConfig.isEnabled());
        assertThat(allSliceConfig.name(), is(name("all")));
        assertFalse(allSliceConfig.hasPredicate());
        assertNull(allSliceConfig.predicate());
        assertTrue(allSliceConfig.hasDimensions());
        assertThat(allSliceConfig.dimensions(), is(List.of(DIMENSION_4, DIMENSION_5, DIMENSION_6, DIMENSION_7)));
        assertTrue(allSliceConfig.hasMaxDimensionalInstances());
        assertThat(allSliceConfig.maxDimensionalInstances(), is(12));
        assertTrue(allSliceConfig.isDimensionalInstanceExpirationEnabled());
        assertThat(allSliceConfig.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(13)));
        assertTrue(allSliceConfig.hasMeasurables());
        assertThat(allSliceConfig.measurables(), is(Set.of(MEASURABLE_2)));

        assertFalse(allSliceConfig.isTotalEnabled());
        assertThat(allSliceConfig.totalInstanceConfig().name(), is(name("total")));
        assertThat(allSliceConfig.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_3)));
        assertThat(allSliceConfig.totalInstanceConfig().context().get("k_3"), is("v_3"));

        assertFalse(allSliceConfig.areLevelsEnabled());
        assertTrue(allSliceConfig.hasLevelInstanceNameProvider());
        assertThat(allSliceConfig.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_3));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_4));
        assertThat(allSliceConfig.levelInstanceConfigs().get(DIMENSION_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(allSliceConfig.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_6));
        assertThat(allSliceConfig.levelInstanceConfigs().get(DIMENSION_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_8));
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
        assertTrue(slice_1_config.hasDimensions());
        assertThat(slice_1_config.dimensions(), is(List.of(DIMENSION_4, DIMENSION_5, DIMENSION_6)));
        assertTrue(slice_1_config.hasMaxDimensionalInstances());
        assertThat(slice_1_config.maxDimensionalInstances(), is(14));
        assertTrue(slice_1_config.isDimensionalInstanceExpirationEnabled());
        assertThat(slice_1_config.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(15)));
        assertTrue(slice_1_config.hasMeasurables());
        assertThat(slice_1_config.measurables(), is(Set.of(MEASURABLE_4)));

        assertFalse(slice_1_config.isTotalEnabled());
        assertThat(slice_1_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_1_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_5)));
        assertThat(slice_1_config.totalInstanceConfig().context().get("k_4"), is("v_4"));

        assertTrue(slice_1_config.areLevelsEnabled());
        assertTrue(slice_1_config.hasLevelInstanceNameProvider());
        assertThat(slice_1_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_3));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_4));
        assertThat(slice_1_config.levelInstanceConfigs().get(DIMENSION_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(slice_1_config.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_6));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_8));
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
        assertTrue(slice_2_config.hasDimensions());
        assertThat(slice_2_config.dimensions(), is(List.of(DIMENSION_5, DIMENSION_6, DIMENSION_7)));
        assertTrue(slice_2_config.hasMaxDimensionalInstances());
        assertThat(slice_2_config.maxDimensionalInstances(), is(16));
        assertTrue(slice_2_config.isDimensionalInstanceExpirationEnabled());
        assertThat(slice_2_config.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(17)));
        assertTrue(slice_2_config.hasMeasurables());
        assertThat(slice_2_config.measurables(), is(Set.of(MEASURABLE_6)));

        assertFalse(slice_2_config.isTotalEnabled());
        assertThat(slice_2_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_2_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_7)));
        assertThat(slice_2_config.totalInstanceConfig().context().get("k_5"), is("v_5"));

        assertTrue(slice_2_config.areLevelsEnabled());
        assertTrue(slice_2_config.hasLevelInstanceNameProvider());
        assertThat(slice_2_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_4));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(slice_2_config.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_6));
        assertThat(slice_2_config.levelInstanceConfigs().get(DIMENSION_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_8));
        assertTrue(slice_2_config.hasLevelInstanceConfigs());
        assertThat(slice_2_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_2_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_2_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(slice_2_config.areOnlyConfiguredLevelsEnabled());

        builder.rebase(withMeter()
            .disable()
            .description("description_2")
            .prefix(dimensionValues(DIMENSION_1.value("v_1_2"), DIMENSION_2.value("v_2")))
            .put("k_1", "v_1_2").put("k_2", "v_2")
            .exclude(EXCLUSION_PREDICATE_2)
            .dimensions(DIMENSION_3, DIMENSION_4, DIMENSION_5, DIMENSION_6, DIMENSION_7, DIMENSION_8)
            .maxDimensionalInstancesPerSlice(110)
            .expireDimensionalInstanceAfter(111, SECONDS)
            .measurables(MEASURABLE_1, MEASURABLE_8)
            // AllSlice
            .allSlice(withName("all_2"))
                .disable()
                .dimensions(DIMENSION_4, DIMENSION_5, DIMENSION_6, DIMENSION_7)
                .maxDimensionalInstances(112)
                .expireDimensionalInstanceAfter(113, SECONDS)
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
                        DIMENSION_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        DIMENSION_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        DIMENSION_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true)
            // Slice 1
            .slice("slice_1", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_1)
                .dimensions(DIMENSION_4, DIMENSION_5, DIMENSION_6)
                .maxDimensionalInstances(114)
                .expireDimensionalInstanceAfter(115, SECONDS)
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
                        DIMENSION_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        DIMENSION_5, LEVEL_INSTANCE_CONFIG_BUILDER_5),
                    LEVEL_INSTANCE_CONFIG_BUILDER_6,
                    true)
            // Slice 3
            .slice("slice_3", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_2)
                .dimensions(DIMENSION_5, DIMENSION_6, DIMENSION_7)
                .maxDimensionalInstances(116)
                .expireDimensionalInstanceAfter(117, SECONDS)
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
                        DIMENSION_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        DIMENSION_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true).builder());

        config = builder.build();

        assertFalse(config.isEnabled());
        assertThat(config.description(), is("description_1"));
        assertThat(config.prefixDimensionValues(), is(dimensionValues(DIMENSION_1.value("v_1"), DIMENSION_2.value("v_2"))));
        assertThat(config.context().get("k_1"), is("v_1"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertTrue(config.hasExclusionPredicate());
        assertThat(config.exclusionPredicate(), is(EXCLUSION_PREDICATE_1));

        assertThat(config.dimensions(), is(List.of(
            DIMENSION_3, DIMENSION_4, DIMENSION_5, DIMENSION_6,
            DIMENSION_7, DIMENSION_8, DIMENSION_9, DIMENSION_10)));

        // AllSlice
        allSliceConfig = config.allSliceConfig();
        assertFalse(allSliceConfig.isEnabled());
        assertThat(allSliceConfig.name(), is(name("all")));
        assertFalse(allSliceConfig.hasPredicate());
        assertNull(allSliceConfig.predicate());
        assertTrue(allSliceConfig.hasDimensions());
        assertThat(allSliceConfig.dimensions(), is(List.of(DIMENSION_4, DIMENSION_5, DIMENSION_6, DIMENSION_7)));
        assertTrue(allSliceConfig.hasMaxDimensionalInstances());
        assertThat(allSliceConfig.maxDimensionalInstances(), is(12));
        assertTrue(allSliceConfig.isDimensionalInstanceExpirationEnabled());
        assertThat(allSliceConfig.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(13)));
        assertTrue(allSliceConfig.hasMeasurables());
        assertThat(allSliceConfig.measurables(), is(Set.of(MEASURABLE_2)));

        assertFalse(allSliceConfig.isTotalEnabled());
        assertThat(allSliceConfig.totalInstanceConfig().name(), is(name("total")));
        assertThat(allSliceConfig.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_3)));
        assertThat(allSliceConfig.totalInstanceConfig().context().get("k_3"), is("v_3"));

        assertFalse(allSliceConfig.areLevelsEnabled());
        assertTrue(allSliceConfig.hasLevelInstanceNameProvider());
        assertThat(allSliceConfig.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_3));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_4));
        assertThat(allSliceConfig.levelInstanceConfigs().get(DIMENSION_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(allSliceConfig.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_6));
        assertThat(allSliceConfig.levelInstanceConfigs().get(DIMENSION_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_8));
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
        assertTrue(slice_1_config.hasDimensions());
        assertThat(slice_1_config.dimensions(), is(List.of(DIMENSION_4, DIMENSION_5, DIMENSION_6)));
        assertTrue(slice_1_config.hasMaxDimensionalInstances());
        assertThat(slice_1_config.maxDimensionalInstances(), is(14));
        assertTrue(slice_1_config.isDimensionalInstanceExpirationEnabled());
        assertThat(slice_1_config.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(15)));
        assertTrue(slice_1_config.hasMeasurables());
        assertThat(slice_1_config.measurables(), is(Set.of(MEASURABLE_4)));

        assertFalse(slice_1_config.isTotalEnabled());
        assertThat(slice_1_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_1_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_5)));
        assertThat(slice_1_config.totalInstanceConfig().context().get("k_4"), is("v_4"));

        assertTrue(slice_1_config.areLevelsEnabled());
        assertTrue(slice_1_config.hasLevelInstanceNameProvider());
        assertThat(slice_1_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_3));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_4));
        assertThat(slice_1_config.levelInstanceConfigs().get(DIMENSION_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(slice_1_config.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_6));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_8));
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
        assertTrue(slice_2_config.hasDimensions());
        assertThat(slice_2_config.dimensions(), is(List.of(DIMENSION_5, DIMENSION_6, DIMENSION_7)));
        assertTrue(slice_2_config.hasMaxDimensionalInstances());
        assertThat(slice_2_config.maxDimensionalInstances(), is(16));
        assertTrue(slice_2_config.isDimensionalInstanceExpirationEnabled());
        assertThat(slice_2_config.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(17)));
        assertTrue(slice_2_config.hasMeasurables());
        assertThat(slice_2_config.measurables(), is(Set.of(MEASURABLE_6)));

        assertFalse(slice_2_config.isTotalEnabled());
        assertThat(slice_2_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_2_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_7)));
        assertThat(slice_2_config.totalInstanceConfig().context().get("k_5"), is("v_5"));

        assertTrue(slice_2_config.areLevelsEnabled());
        assertTrue(slice_2_config.hasLevelInstanceNameProvider());
        assertThat(slice_2_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_4));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(slice_2_config.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_6));
        assertThat(slice_2_config.levelInstanceConfigs().get(DIMENSION_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_8));
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
        assertTrue(slice_3_config.hasDimensions());
        assertThat(slice_3_config.dimensions(), is(List.of(DIMENSION_5, DIMENSION_6, DIMENSION_7)));
        assertTrue(slice_3_config.hasMaxDimensionalInstances());
        assertThat(slice_3_config.maxDimensionalInstances(), is(116));
        assertTrue(slice_3_config.isDimensionalInstanceExpirationEnabled());
        assertThat(slice_3_config.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(117)));
        assertTrue(slice_3_config.hasMeasurables());
        assertThat(slice_3_config.measurables(), is(Set.of(MEASURABLE_6, MEASURABLE_8)));

        assertFalse(slice_3_config.isTotalEnabled());
        assertThat(slice_3_config.totalInstanceConfig().name(), is(name("total_2")));
        assertThat(slice_3_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_7, MEASURABLE_8)));
        assertThat(slice_3_config.totalInstanceConfig().context().get("k_5"), is("v_5_2"));

        assertTrue(slice_3_config.areLevelsEnabled());
        assertTrue(slice_3_config.hasLevelInstanceNameProvider());
        assertThat(slice_3_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_3));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(DIMENSION_3));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(DIMENSION_4));
        assertTrue(slice_3_config.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(slice_3_config.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(slice_3_config.hasLevelInstanceConfigFor(DIMENSION_6));
        assertThat(slice_3_config.levelInstanceConfigs().get(DIMENSION_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(DIMENSION_8));
        assertTrue(slice_3_config.hasLevelInstanceConfigs());
        assertThat(slice_3_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_3_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_3_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(slice_3_config.areOnlyConfiguredLevelsEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rebase_dimensionsNotUnique() {
        BaseMeterConfigBuilder builder = meter().dimensions(DIMENSION_1, DIMENSION_2);
        builder.rebase(withMeter().prefix(dimensionValues(DIMENSION_2.value("v_2"))));
    }

    @Test
    public void mod() {
        BaseMeterConfigBuilder builder = meter()
            .dimensions(
                DIMENSION_3, DIMENSION_4, DIMENSION_5, DIMENSION_6,
                DIMENSION_7, DIMENSION_8, DIMENSION_9, DIMENSION_10);

        builder.modify(withMeter()
            .disable()
            .description("description_1")
            .prefix(dimensionValues(DIMENSION_1.value("v_1"), DIMENSION_2.value("v_2")))
            .put("k_1", "v_1").put("k_2", "v_2")
            .exclude(EXCLUSION_PREDICATE_1)
            .dimensions(DIMENSION_3, DIMENSION_4, DIMENSION_5, DIMENSION_6, DIMENSION_7, DIMENSION_8)
            .maxDimensionalInstancesPerSlice(10)
            .expireDimensionalInstanceAfter(11, SECONDS)
            .measurables(MEASURABLE_1)
            // AllSlice
            .allSlice(withName("all"))
                .disable()
                .dimensions(DIMENSION_4, DIMENSION_5, DIMENSION_6, DIMENSION_7)
                .maxDimensionalInstances(12)
                .expireDimensionalInstanceAfter(13, SECONDS)
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
                        DIMENSION_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        DIMENSION_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        DIMENSION_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true)
            // Slice 1
            .slice("slice_1", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_1)
                .dimensions(DIMENSION_4, DIMENSION_5, DIMENSION_6)
                .maxDimensionalInstances(14)
                .expireDimensionalInstanceAfter(15, SECONDS)
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
                        DIMENSION_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        DIMENSION_5, LEVEL_INSTANCE_CONFIG_BUILDER_5),
                    LEVEL_INSTANCE_CONFIG_BUILDER_6,
                    true)
            // Slice 2
            .slice("slice_2", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_2)
                .dimensions(DIMENSION_5, DIMENSION_6, DIMENSION_7)
                .maxDimensionalInstances(16)
                .expireDimensionalInstanceAfter(17, SECONDS)
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
                        DIMENSION_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        DIMENSION_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true).builder());

        BaseMeterConfig config = builder.build();

        assertFalse(config.isEnabled());
        assertThat(config.description(), is("description_1"));
        assertThat(config.prefixDimensionValues(), is(dimensionValues(DIMENSION_1.value("v_1"), DIMENSION_2.value("v_2"))));
        assertThat(config.context().get("k_1"), is("v_1"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertTrue(config.hasExclusionPredicate());
        assertThat(config.exclusionPredicate(), is(EXCLUSION_PREDICATE_1));

        assertThat(config.dimensions(), is(List.of(
            DIMENSION_3, DIMENSION_4, DIMENSION_5, DIMENSION_6,
            DIMENSION_7, DIMENSION_8, DIMENSION_9, DIMENSION_10)));

        // AllSlice
        BaseMeterSliceConfig allSliceConfig = config.allSliceConfig();
        assertFalse(allSliceConfig.isEnabled());
        assertThat(allSliceConfig.name(), is(name("all")));
        assertFalse(allSliceConfig.hasPredicate());
        assertNull(allSliceConfig.predicate());
        assertTrue(allSliceConfig.hasDimensions());
        assertThat(allSliceConfig.dimensions(), is(List.of(DIMENSION_4, DIMENSION_5, DIMENSION_6, DIMENSION_7)));
        assertTrue(allSliceConfig.hasMaxDimensionalInstances());
        assertThat(allSliceConfig.maxDimensionalInstances(), is(12));
        assertTrue(allSliceConfig.isDimensionalInstanceExpirationEnabled());
        assertThat(allSliceConfig.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(13)));
        assertTrue(allSliceConfig.hasMeasurables());
        assertThat(allSliceConfig.measurables(), is(Set.of(MEASURABLE_2)));

        assertFalse(allSliceConfig.isTotalEnabled());
        assertThat(allSliceConfig.totalInstanceConfig().name(), is(name("total")));
        assertThat(allSliceConfig.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_3)));
        assertThat(allSliceConfig.totalInstanceConfig().context().get("k_3"), is("v_3"));

        assertFalse(allSliceConfig.areLevelsEnabled());
        assertTrue(allSliceConfig.hasLevelInstanceNameProvider());
        assertThat(allSliceConfig.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_3));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_4));
        assertThat(allSliceConfig.levelInstanceConfigs().get(DIMENSION_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(allSliceConfig.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_6));
        assertThat(allSliceConfig.levelInstanceConfigs().get(DIMENSION_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_8));
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
        assertTrue(slice_1_config.hasDimensions());
        assertThat(slice_1_config.dimensions(), is(List.of(DIMENSION_4, DIMENSION_5, DIMENSION_6)));
        assertTrue(slice_1_config.hasMaxDimensionalInstances());
        assertThat(slice_1_config.maxDimensionalInstances(), is(14));
        assertTrue(slice_1_config.isDimensionalInstanceExpirationEnabled());
        assertThat(slice_1_config.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(15)));
        assertTrue(slice_1_config.hasMeasurables());
        assertThat(slice_1_config.measurables(), is(Set.of(MEASURABLE_4)));

        assertFalse(slice_1_config.isTotalEnabled());
        assertThat(slice_1_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_1_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_5)));
        assertThat(slice_1_config.totalInstanceConfig().context().get("k_4"), is("v_4"));

        assertTrue(slice_1_config.areLevelsEnabled());
        assertTrue(slice_1_config.hasLevelInstanceNameProvider());
        assertThat(slice_1_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_3));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_4));
        assertThat(slice_1_config.levelInstanceConfigs().get(DIMENSION_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(slice_1_config.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_6));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_8));
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
        assertTrue(slice_2_config.hasDimensions());
        assertThat(slice_2_config.dimensions(), is(List.of(DIMENSION_5, DIMENSION_6, DIMENSION_7)));
        assertTrue(slice_2_config.hasMaxDimensionalInstances());
        assertThat(slice_2_config.maxDimensionalInstances(), is(16));
        assertTrue(slice_2_config.isDimensionalInstanceExpirationEnabled());
        assertThat(slice_2_config.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(17)));
        assertTrue(slice_2_config.hasMeasurables());
        assertThat(slice_2_config.measurables(), is(Set.of(MEASURABLE_6)));

        assertFalse(slice_2_config.isTotalEnabled());
        assertThat(slice_2_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_2_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_7)));
        assertThat(slice_2_config.totalInstanceConfig().context().get("k_5"), is("v_5"));

        assertTrue(slice_2_config.areLevelsEnabled());
        assertTrue(slice_2_config.hasLevelInstanceNameProvider());
        assertThat(slice_2_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_4));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(slice_2_config.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_6));
        assertThat(slice_2_config.levelInstanceConfigs().get(DIMENSION_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_8));
        assertTrue(slice_2_config.hasLevelInstanceConfigs());
        assertThat(slice_2_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_2_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_2_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(slice_2_config.areOnlyConfiguredLevelsEnabled());

        builder.modify(withMeter()
            .disable()
            .description("description_2")
            .prefix(dimensionValues(DIMENSION_1.value("v_1_2"), DIMENSION_2.value("v_2")))
            .put("k_1", "v_1_2").put("k_2", "v_2")
            .exclude(EXCLUSION_PREDICATE_2)
            .dimensions(DIMENSION_3, DIMENSION_4, DIMENSION_5, DIMENSION_6, DIMENSION_7, DIMENSION_8)
            .maxDimensionalInstancesPerSlice(110)
            .expireDimensionalInstanceAfter(111, SECONDS)
            .measurables(MEASURABLE_1, MEASURABLE_8)
            // AllSlice
            .allSlice(withName("all_2"))
                .disable()
                .dimensions(DIMENSION_4, DIMENSION_5, DIMENSION_6, DIMENSION_7)
                .maxDimensionalInstances(112)
                .expireDimensionalInstanceAfter(113, SECONDS)
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
                        DIMENSION_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        DIMENSION_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        DIMENSION_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true)
            // Slice 1
            .slice("slice_1", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_1)
                .dimensions(DIMENSION_4, DIMENSION_5, DIMENSION_6)
                .maxDimensionalInstances(114)
                .expireDimensionalInstanceAfter(115, SECONDS)
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
                        DIMENSION_4, LEVEL_INSTANCE_CONFIG_BUILDER_4,
                        DIMENSION_5, LEVEL_INSTANCE_CONFIG_BUILDER_5),
                    LEVEL_INSTANCE_CONFIG_BUILDER_6,
                    true)
            // Slice 3
            .slice("slice_3", "suffix")
                .disable()
                .predicate(SLICE_PREDICATE_2)
                .dimensions(DIMENSION_5, DIMENSION_6, DIMENSION_7)
                .maxDimensionalInstances(116)
                .expireDimensionalInstanceAfter(117, SECONDS)
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
                        DIMENSION_5, LEVEL_INSTANCE_CONFIG_BUILDER_5,
                        DIMENSION_6, LEVEL_INSTANCE_CONFIG_BUILDER_6),
                    LEVEL_INSTANCE_CONFIG_BUILDER_7,
                    true).builder());

        config = builder.build();

        assertFalse(config.isEnabled());
        assertThat(config.description(), is("description_2"));
        assertThat(config.prefixDimensionValues(), is(dimensionValues(DIMENSION_1.value("v_1_2"), DIMENSION_2.value("v_2"))));
        assertThat(config.context().get("k_1"), is("v_1_2"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertTrue(config.hasExclusionPredicate());
        assertThat(config.exclusionPredicate(), is(EXCLUSION_PREDICATE_2));

        assertThat(config.dimensions(), is(List.of(
            DIMENSION_3, DIMENSION_4, DIMENSION_5, DIMENSION_6,
            DIMENSION_7, DIMENSION_8, DIMENSION_9, DIMENSION_10)));

        // AllSlice
        allSliceConfig = config.allSliceConfig();
        assertFalse(allSliceConfig.isEnabled());
        assertThat(allSliceConfig.name(), is(name("all_2")));
        assertFalse(allSliceConfig.hasPredicate());
        assertNull(allSliceConfig.predicate());
        assertTrue(allSliceConfig.hasDimensions());
        assertThat(allSliceConfig.dimensions(), is(List.of(DIMENSION_4, DIMENSION_5, DIMENSION_6, DIMENSION_7)));
        assertTrue(allSliceConfig.hasMaxDimensionalInstances());
        assertThat(allSliceConfig.maxDimensionalInstances(), is(112));
        assertTrue(allSliceConfig.isDimensionalInstanceExpirationEnabled());
        assertThat(allSliceConfig.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(113)));
        assertTrue(allSliceConfig.hasMeasurables());
        assertThat(allSliceConfig.measurables(), is(Set.of(MEASURABLE_2, MEASURABLE_8)));

        assertFalse(allSliceConfig.isTotalEnabled());
        assertThat(allSliceConfig.totalInstanceConfig().name(), is(name("total_2")));
        assertThat(allSliceConfig.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_3, MEASURABLE_8)));
        assertThat(allSliceConfig.totalInstanceConfig().context().get("k_3"), is("v_3_2"));

        assertFalse(allSliceConfig.areLevelsEnabled());
        assertTrue(allSliceConfig.hasLevelInstanceNameProvider());
        assertThat(allSliceConfig.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_3));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_4));
        assertThat(allSliceConfig.levelInstanceConfigs().get(DIMENSION_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(allSliceConfig.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_6));
        assertThat(allSliceConfig.levelInstanceConfigs().get(DIMENSION_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(allSliceConfig.hasLevelInstanceConfigFor(DIMENSION_8));
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
        assertTrue(slice_1_config.hasDimensions());
        assertThat(slice_1_config.dimensions(), is(List.of(DIMENSION_4, DIMENSION_5, DIMENSION_6)));
        assertTrue(slice_1_config.hasMaxDimensionalInstances());
        assertThat(slice_1_config.maxDimensionalInstances(), is(114));
        assertTrue(slice_1_config.isDimensionalInstanceExpirationEnabled());
        assertThat(slice_1_config.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(115)));
        assertTrue(slice_1_config.hasMeasurables());
        assertThat(slice_1_config.measurables(), is(Set.of(MEASURABLE_4, MEASURABLE_8)));

        assertFalse(slice_1_config.isTotalEnabled());
        assertThat(slice_1_config.totalInstanceConfig().name(), is(name("total_2")));
        assertThat(slice_1_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_5, MEASURABLE_8)));
        assertThat(slice_1_config.totalInstanceConfig().context().get("k_4"), is("v_4_2"));

        assertTrue(slice_1_config.areLevelsEnabled());
        assertTrue(slice_1_config.hasLevelInstanceNameProvider());
        assertThat(slice_1_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_3));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_4));
        assertThat(slice_1_config.levelInstanceConfigs().get(DIMENSION_4).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_4.build().name()));
        assertTrue(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(slice_1_config.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_6));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(slice_1_config.hasLevelInstanceConfigFor(DIMENSION_8));
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
        assertTrue(slice_2_config.hasDimensions());
        assertThat(slice_2_config.dimensions(), is(List.of(DIMENSION_5, DIMENSION_6, DIMENSION_7)));
        assertTrue(slice_2_config.hasMaxDimensionalInstances());
        assertThat(slice_2_config.maxDimensionalInstances(), is(16));
        assertTrue(slice_2_config.isDimensionalInstanceExpirationEnabled());
        assertThat(slice_2_config.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(17)));
        assertTrue(slice_2_config.hasMeasurables());
        assertThat(slice_2_config.measurables(), is(Set.of(MEASURABLE_6)));

        assertFalse(slice_2_config.isTotalEnabled());
        assertThat(slice_2_config.totalInstanceConfig().name(), is(name("total")));
        assertThat(slice_2_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_7)));
        assertThat(slice_2_config.totalInstanceConfig().context().get("k_5"), is("v_5"));

        assertTrue(slice_2_config.areLevelsEnabled());
        assertTrue(slice_2_config.hasLevelInstanceNameProvider());
        assertThat(slice_2_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_3));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_4));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(slice_2_config.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_6));
        assertThat(slice_2_config.levelInstanceConfigs().get(DIMENSION_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(slice_2_config.hasLevelInstanceConfigFor(DIMENSION_8));
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
        assertTrue(slice_3_config.hasDimensions());
        assertThat(slice_3_config.dimensions(), is(List.of(DIMENSION_5, DIMENSION_6, DIMENSION_7)));
        assertTrue(slice_3_config.hasMaxDimensionalInstances());
        assertThat(slice_3_config.maxDimensionalInstances(), is(116));
        assertTrue(slice_3_config.isDimensionalInstanceExpirationEnabled());
        assertThat(slice_3_config.dimensionalInstanceExpirationTime(), is(Duration.ofSeconds(117)));
        assertTrue(slice_3_config.hasMeasurables());
        assertThat(slice_3_config.measurables(), is(Set.of(MEASURABLE_6, MEASURABLE_8)));

        assertFalse(slice_3_config.isTotalEnabled());
        assertThat(slice_3_config.totalInstanceConfig().name(), is(name("total_2")));
        assertThat(slice_3_config.totalInstanceConfig().measurables(), is(Set.of(MEASURABLE_7, MEASURABLE_8)));
        assertThat(slice_3_config.totalInstanceConfig().context().get("k_5"), is("v_5_2"));

        assertTrue(slice_3_config.areLevelsEnabled());
        assertTrue(slice_3_config.hasLevelInstanceNameProvider());
        assertThat(slice_3_config.levelInstanceNameProvider(), is(LEVEL_INSTANCE_NAME_PROVIDER_3));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(DIMENSION_1));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(DIMENSION_2));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(DIMENSION_3));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(DIMENSION_4));
        assertTrue(slice_3_config.hasLevelInstanceConfigFor(DIMENSION_5));
        assertThat(slice_3_config.levelInstanceConfigs().get(DIMENSION_5).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_5.build().name()));
        assertTrue(slice_3_config.hasLevelInstanceConfigFor(DIMENSION_6));
        assertThat(slice_3_config.levelInstanceConfigs().get(DIMENSION_6).name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_6.build().name()));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(DIMENSION_7));
        assertFalse(slice_3_config.hasLevelInstanceConfigFor(DIMENSION_8));
        assertTrue(slice_3_config.hasLevelInstanceConfigs());
        assertThat(slice_3_config.levelInstanceConfigs().size(), is(2));
        assertTrue(slice_3_config.hasDefaultLevelInstanceConfig());
        assertThat(slice_3_config.defaultLevelInstanceConfig().name(), is(LEVEL_INSTANCE_CONFIG_BUILDER_7.build().name()));
        assertTrue(slice_3_config.areOnlyConfiguredLevelsEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mod_dimensionsNotUnique() {
        BaseMeterConfigBuilder builder = meter().dimensions(DIMENSION_1, DIMENSION_2);
        builder.modify(withMeter().prefix(dimensionValues(DIMENSION_2.value("v_2"))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void dimensions_dimensionsNotUnique() {
        meter()
            .prefix(dimensionValues(DIMENSION_1.value("v_1")))
            .dimensions(DIMENSION_1, DIMENSION_2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void prefixDimensionValues_dimensionsNotUnique() {
        meter()
            .dimensions(DIMENSION_1, DIMENSION_2)
            .prefix(dimensionValues(DIMENSION_2.value("v_1")));
    }
}