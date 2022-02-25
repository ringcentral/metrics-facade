package com.ringcentral.platform.metrics.dimensions;

import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MetricDimensionValuesTest {

    static final MetricDimension DIMENSION_1 = new MetricDimension("dim_1");
    static final MetricDimension DIMENSION_2 = new MetricDimension("dim_2");
    static final MetricDimension DIMENSION_3 = new MetricDimension("dim_3");
    static final MetricDimension DIMENSION_4 = new MetricDimension("dim_4");

    @Test
    public void makingDimensionValues() {
        MetricDimensionValues dimensionValues = dimensionValues(
            dimensionValues(DIMENSION_1.value("1"), DIMENSION_2.value("2")),
            DIMENSION_3.value("3"), DIMENSION_4.value("4"));

        assertThat(
            dimensionValues.list(),
            is(List.of(DIMENSION_1.value("1"), DIMENSION_2.value("2"), DIMENSION_3.value("3"), DIMENSION_4.value("4"))));

        dimensionValues = dimensionValues(
            noDimensionValues(),
            DIMENSION_3.value("3"), DIMENSION_4.value("4"));

        assertThat(
            dimensionValues.list(),
            is(List.of(DIMENSION_3.value("3"), DIMENSION_4.value("4"))));

        dimensionValues = dimensionValues(dimensionValues(DIMENSION_1.value("1"), DIMENSION_2.value("2")));

        assertThat(
            dimensionValues.list(),
            is(List.of(DIMENSION_1.value("1"), DIMENSION_2.value("2"))));
    }
}