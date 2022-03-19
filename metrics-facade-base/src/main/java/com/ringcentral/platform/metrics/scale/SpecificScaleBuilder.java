package com.ringcentral.platform.metrics.scale;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

public class SpecificScaleBuilder implements ScaleBuilder<SpecificScale> {

    private final List<Long> points;

    public static SpecificScaleBuilder infOnlyScale() {
        return points(Long.MAX_VALUE);
    }

    public static SpecificScaleBuilder points(TimeUnit unit, long... points) {
        return points(List.copyOf(Arrays.stream(points).map(unit::toNanos).boxed().collect(toList())));
    }

    public static SpecificScaleBuilder points(long... points) {
        return points(List.copyOf(Arrays.stream(points).boxed().collect(toList())));
    }

    public static SpecificScaleBuilder points(List<Long> points) {
        return new SpecificScaleBuilder(points);
    }

    protected SpecificScaleBuilder(List<Long> points) {
        checkArgument(points != null && !points.isEmpty(), "No points");
        this.points = points;
    }

    @Override
    public SpecificScale build() {
        return new SpecificScale(points);
    }
}