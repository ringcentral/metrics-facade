package com.ringcentral.platform.metrics.x.histogram.scale.configs;

import java.util.*;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class CompositeScaleBuilder implements ScaleBuilder<CompositeScale> {

    public List<Scale> scales;

    public CompositeScaleBuilder(Scale first) {
        this.scales = new ArrayList<>();
        this.scales.add(first);
    }

    public static CompositeScaleBuilder first(ScaleBuilder<?> first) {
        return compositeScaleBuilder(first.build());
    }

    public static CompositeScaleBuilder first(Scale first) {
        return compositeScaleBuilder(first);
    }

    public static CompositeScaleBuilder scale(ScaleBuilder<?> first) {
        return compositeScaleBuilder(first.build());
    }

    public static CompositeScaleBuilder scale(Scale first) {
        return compositeScaleBuilder(first);
    }

    public static CompositeScaleBuilder compositeScaleBuilder(ScaleBuilder<?> first) {
        return new CompositeScaleBuilder(first.build());
    }

    public static CompositeScaleBuilder compositeScaleBuilder(Scale first) {
        return new CompositeScaleBuilder(first);
    }

    public CompositeScaleBuilder then(Scale next) {
        this.scales.add(next);
        return this;
    }

    @Override
    public CompositeScale build() {
        checkArgument(!requireNonNull(scales).isEmpty(), "scales must be non-empty");
        return new CompositeScale(scales);
    }
}
