package com.ringcentral.platform.metrics.scale;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class SpecificScale implements Scale {

    private final List<Long> points;

    public SpecificScale(List<Long> points) {
        this.points = points.stream().sorted().collect(toList());
    }

    @Override
    public List<Long> points() {
        return points;
    }
}
