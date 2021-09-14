package com.ringcentral.platform.metrics.var.configs.builders;

import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.var.configs.BaseCachingVarConfig;
import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.dimensionValues;
import static com.ringcentral.platform.metrics.var.configs.builders.AbstractCachingVarConfigBuilder.DEFAULT_TTL_SEC;
import static com.ringcentral.platform.metrics.var.configs.builders.BaseCachingVarConfigBuilder.*;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class BaseCachingVarConfigBuilderTest {

    static final MetricDimension DIMENSION_1 = new MetricDimension("dimension_1");
    static final MetricDimension DIMENSION_2 = new MetricDimension("dimension_2");
    static final MetricDimension DIMENSION_3 = new MetricDimension("dimension_3");
    static final MetricDimension DIMENSION_4 = new MetricDimension("dimension_4");

    @Test
    public void minConfig() {
        BaseCachingVarConfig config = new BaseCachingVarConfigBuilder().build();
        assertTrue(config.isEnabled());
        assertTrue(config.prefixDimensionValues().isEmpty());
        assertTrue(config.context().isEmpty());
        assertTrue(config.dimensions().isEmpty());
        assertThat(config.ttl(), is(DEFAULT_TTL_SEC));
        assertThat(config.ttlUnit(), is(SECONDS));
    }

    @Test
    public void maxConfig() {
        BaseCachingVarConfig config = cachingVar()
            .disable()
            .prefix(dimensionValues(DIMENSION_1.value("v_1"), DIMENSION_2.value("v_2")))
            .put("k_1", "v_1").put("k_2", "v_2")
            .dimensions(DIMENSION_3)
            .ttl(1L, HOURS)
            .build();

        assertFalse(config.isEnabled());
        assertThat(config.prefixDimensionValues(), is(dimensionValues(DIMENSION_1.value("v_1"), DIMENSION_2.value("v_2"))));
        assertThat(config.context().get("k_1"), is("v_1"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertThat(config.dimensions(), is(List.of(DIMENSION_3)));
        assertThat(config.ttl(), is(1L));
        assertThat(config.ttlUnit(), is(HOURS));
    }

    @Test
    public void rebase() {
        BaseCachingVarConfigBuilder builder = cachingVar();

        builder.rebase(withCachingVar()
            .disable()
            .prefix(dimensionValues(DIMENSION_1.value("v_1"), DIMENSION_2.value("v_2")))
            .put("k_1", "v_1_2").put("k_2", "v_2")
            .dimensions(DIMENSION_3)
            .ttl(1L, HOURS));

        BaseCachingVarConfig config = builder.build();
        assertFalse(config.isEnabled());
        assertThat(config.prefixDimensionValues(), is(dimensionValues(DIMENSION_1.value("v_1"), DIMENSION_2.value("v_2"))));
        assertThat(config.context().get("k_1"), is("v_1_2"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertTrue(config.dimensions().isEmpty());
        assertThat(config.ttl(), is(1L));
        assertThat(config.ttlUnit(), is(HOURS));

        builder = cachingVar()
            .enable()
            .prefix(dimensionValues(DIMENSION_3.value("v_3")))
            .put("k_1", "v_1_1")
            .dimensions(DIMENSION_1, DIMENSION_2)
            .ttl(2L, DAYS);

        builder.rebase(cachingVar()
            .disable()
            .prefix(dimensionValues(DIMENSION_1.value("v_1"), DIMENSION_2.value("v_2")))
            .put("k_1", "v_1_2").put("k_2", "v_2")
            .dimensions(DIMENSION_3)
            .ttl(1L, HOURS));

        config = builder.build();
        assertTrue(config.isEnabled());
        assertThat(config.prefixDimensionValues(), is(dimensionValues(DIMENSION_3.value("v_3"))));
        assertThat(config.context().get("k_1"), is("v_1_1"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertThat(config.dimensions(), is(List.of(DIMENSION_1, DIMENSION_2)));
        assertThat(config.ttl(), is(2L));
        assertThat(config.ttlUnit(), is(DAYS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rebase_dimensionsNotUnique() {
        BaseCachingVarConfigBuilder builder = cachingVar().dimensions(DIMENSION_1, DIMENSION_2);
        builder.rebase(withCachingVar().prefix(dimensionValues(DIMENSION_2.value("v_2"))));
    }

    @Test
    public void mod() {
        BaseCachingVarConfigBuilder builder = cachingVar();

        builder.modify(withCachingVar()
            .disable()
            .prefix(dimensionValues(DIMENSION_1.value("v_1"), DIMENSION_2.value("v_2")))
            .put("k_1", "v_1_2").put("k_2", "v_2")
            .dimensions(DIMENSION_3)
            .ttl(1L, HOURS));

        BaseCachingVarConfig config = builder.build();
        assertFalse(config.isEnabled());
        assertThat(config.prefixDimensionValues(), is(dimensionValues(DIMENSION_1.value("v_1"), DIMENSION_2.value("v_2"))));
        assertThat(config.context().get("k_1"), is("v_1_2"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertTrue(config.dimensions().isEmpty());
        assertThat(config.ttl(), is(1L));
        assertThat(config.ttlUnit(), is(HOURS));

        builder = cachingVar()
            .enable()
            .prefix(dimensionValues(DIMENSION_3.value("v_3")))
            .put("k_1", "v_1_1")
            .dimensions(DIMENSION_1, DIMENSION_2)
            .ttl(2L, DAYS);

        builder.modify(cachingVar()
            .disable()
            .prefix(dimensionValues(DIMENSION_4.value("v_4")))
            .put("k_1", "v_1_2").put("k_2", "v_2")
            .dimensions(DIMENSION_3)
            .ttl(1L, HOURS));

        config = builder.build();
        assertFalse(config.isEnabled());
        assertThat(config.prefixDimensionValues(), is(dimensionValues(DIMENSION_4.value("v_4"))));
        assertThat(config.context().get("k_1"), is("v_1_2"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertThat(config.dimensions(), is(List.of(DIMENSION_1, DIMENSION_2)));
        assertThat(config.ttl(), is(1L));
        assertThat(config.ttlUnit(), is(HOURS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void mod_dimensionsNotUnique() {
        BaseCachingVarConfigBuilder builder = cachingVar().dimensions(DIMENSION_1, DIMENSION_2);
        builder.modify(withCachingVar().prefix(dimensionValues(DIMENSION_2.value("v_2"))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void dimensions_dimensionsNotUnique() {
        cachingVar()
            .prefix(dimensionValues(DIMENSION_1.value("v_1")))
            .dimensions(DIMENSION_1, DIMENSION_2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void prefixDimensionValues_dimensionsNotUnique() {
        cachingVar()
            .dimensions(DIMENSION_1, DIMENSION_2)
            .prefix(dimensionValues(DIMENSION_2.value("v_1")));
    }
}