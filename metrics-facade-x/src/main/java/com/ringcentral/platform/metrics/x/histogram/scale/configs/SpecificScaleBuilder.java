package com.ringcentral.platform.metrics.x.histogram.scale.configs;

import java.util.List;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;

public class SpecificScaleBuilder implements ScaleBuilder<SpecificScale> {

    private final List<Long> points;

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
