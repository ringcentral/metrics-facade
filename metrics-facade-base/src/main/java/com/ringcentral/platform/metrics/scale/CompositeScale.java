package com.ringcentral.platform.metrics.scale;

import java.util.*;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class CompositeScale implements Scale {

    private final List<Long> points;

    public CompositeScale(List<Scale> scales) {
        checkArgument(!requireNonNull(scales).isEmpty(), "No scales");
        this.points = new ArrayList<>();
        long p = 0L;

        scalesLoop:
        for (int i = 0; i < scales.size(); ++i) {
            Scale scale = scales.get(i);
            int j = 0;

            if (i > 0) {
                if (scale.firstPoint() == 0) {
                    if (scale.pointCount() > 1) {
                        j = 1;
                    } else {
                        continue;
                    }
                } else if (scale.firstPoint() < 0) {
                    throw new IllegalArgumentException("scales[i > 0].firstPoint() must be >= 0");
                }
            }

            for (; j < scale.pointCount(); ++j) {
                if (i > 0) {
                    long increase =
                        j > 0 ?
                        scale.point(j) - scale.point(j - 1) :
                        scale.point(j);

                    if (p <= Long.MAX_VALUE - increase) {
                        p += increase;
                    } else {
                        if (scale.point(j) == Long.MAX_VALUE && p != Long.MAX_VALUE) {
                            this.points.add(Long.MAX_VALUE);
                        }

                        break scalesLoop;
                    }
                } else {
                    p = scale.point(j);
                }

                this.points.add(p);
            }
        }
    }

    @Override
    public List<Long> points() {
        return points;
    }
}
