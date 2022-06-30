package com.ringcentral.platform.metrics.dimensions;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionUtils.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class MetricDimensionUtilsTest {

    static final MetricDimension DIMENSION_1 = new MetricDimension("dimension_1");
    static final MetricDimension DIMENSION_2 = new MetricDimension("dimension_2");
    static final MetricDimension DIMENSION_3 = new MetricDimension("dimension_3");

    static MetricDimensionValue DV_1 = DIMENSION_1.value("1");
    static MetricDimensionValue DV_2 = DIMENSION_2.value("2");
    static MetricDimensionValue DV_3 = DIMENSION_3.value("3");

    @Test
    public void test_hasDimensionValues() {
        assertTrue(hasDimensionValues(List.of(DV_1)));
        assertTrue(hasDimensionValues(List.of(DV_1, DV_2)));
        assertFalse(hasDimensionValues(null));
        assertFalse(hasDimensionValues(emptyList()));
    }

    @Test
    public void test_hasDimension() {
        assertTrue(hasDimension(List.of(DV_1), DIMENSION_1));
        assertTrue(hasDimension(List.of(DV_1, DV_2), DIMENSION_1));
        assertTrue(hasDimension(List.of(DV_1, DV_2), DIMENSION_2));

        assertFalse(hasDimension(emptyList(), DIMENSION_1));
        assertFalse(hasDimension(List.of(DV_1, DV_2), DIMENSION_3));
    }

    @Test
    public void test_valueOf() {
        assertThat(valueOf(List.of(DV_1), DIMENSION_1), is("1"));
        assertThat(valueOf(List.of(DV_1, DV_2), DIMENSION_1), is("1"));
        assertThat(valueOf(List.of(DV_1, DV_2), DIMENSION_2), is("2"));

        assertNull(valueOf(emptyList(), DIMENSION_1));
        assertNull(valueOf(List.of(DV_1), DIMENSION_2));
        assertNull(valueOf(List.of(DV_1, DV_2), DIMENSION_3));
    }

    @Test
    public void test_dimensionValueOf() {
        assertThat(dimensionValueOf(List.of(DV_1), DIMENSION_1), is(DV_1));
        assertThat(dimensionValueOf(List.of(DV_1, DV_2), DIMENSION_1), is(DV_1));
        assertThat(dimensionValueOf(List.of(DV_1, DV_2), DIMENSION_2), is(DV_2));

        assertNull(dimensionValueOf(emptyList(), DIMENSION_1));
        assertNull(dimensionValueOf(List.of(DV_1), DIMENSION_2));
        assertNull(dimensionValueOf(List.of(DV_1, DV_2), DIMENSION_3));
    }

    @Test
    public void test_dimensionToValue() {
        assertThat(dimensionToValue(List.of(DV_1)), is(Map.of(DIMENSION_1, DV_1)));
        assertThat(dimensionToValue(List.of(DV_1, DV_2)), is(Map.of(DIMENSION_1, DV_1, DIMENSION_2, DV_2)));
        assertThat(dimensionToValue(List.of(DV_1, DV_2, DV_3)), is(Map.of(DIMENSION_1, DV_1, DIMENSION_2, DV_2, DIMENSION_3, DV_3)));

        assertThat(dimensionToValue(null), is(emptyMap()));
        assertThat(dimensionToValue(emptyList()), is(emptyMap()));
    }

    @Test
    public void test_dimensionValuesWithout() {
        assertThat(dimensionValuesWithout(List.of(DV_1), DIMENSION_1), is(emptyList()));
        assertThat(dimensionValuesWithout(List.of(DV_1, DV_2), DIMENSION_1), is(List.of(DV_2)));
        assertThat(dimensionValuesWithout(List.of(DV_1, DV_2), DIMENSION_2), is(List.of(DV_1)));
        assertThat(dimensionValuesWithout(List.of(DV_1, DV_2, DV_3), DIMENSION_1), is(List.of(DV_2, DV_3)));
        assertThat(dimensionValuesWithout(List.of(DV_1, DV_2, DV_3), DIMENSION_2), is(List.of(DV_1, DV_3)));
        assertThat(dimensionValuesWithout(List.of(DV_1, DV_2, DV_3), DIMENSION_3), is(List.of(DV_1, DV_2)));
        assertThat(dimensionValuesWithout(List.of(DV_1, DV_2, DV_3), DIMENSION_1, DIMENSION_2), is(List.of(DV_3)));
        assertThat(dimensionValuesWithout(List.of(DV_1, DV_2, DV_3), DIMENSION_2, DIMENSION_3), is(List.of(DV_1)));
        assertThat(dimensionValuesWithout(List.of(DV_1, DV_2, DV_3), DIMENSION_1, DIMENSION_3), is(List.of(DV_2)));
        assertThat(dimensionValuesWithout(List.of(DV_1, DV_2, DV_3), DIMENSION_1, DIMENSION_2, DIMENSION_3), is(emptyList()));

        assertThat(dimensionValuesWithout(null, DIMENSION_1), is(emptyList()));
        assertThat(dimensionValuesWithout(emptyList(), DIMENSION_1), is(emptyList()));
        assertThat(dimensionValuesWithout(List.of(DV_1), DIMENSION_2), is(List.of(DV_1)));
        assertThat(dimensionValuesWithout(List.of(DV_1, DV_2), DIMENSION_3), is(List.of(DV_1, DV_2)));
    }
}