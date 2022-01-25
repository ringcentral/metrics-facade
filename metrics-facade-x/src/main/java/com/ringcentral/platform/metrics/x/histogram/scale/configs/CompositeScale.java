package com.ringcentral.platform.metrics.x.histogram.scale.configs;

import java.util.*;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class CompositeScale extends AbstractScale {

    private final List<Long> points;

    public CompositeScale(List<Scale> scales) {
        checkArgument(!requireNonNull(scales).isEmpty(), "scales must be non-empty");
        this.points = new ArrayList<>();
        long p = 0L;

        scalesLoop:
        for (int i = 0; i < scales.size(); ++i) {
            Scale scale = scales.get(i);

            if (i > 0) {
                checkArgument(
                    scale.firstPoint() > 0,
                    "scales[i > 0].firstPoint must be > 0");
            }

            for (int j = 0; j < scale.pointCount(); ++j) {
                if (i > 0) {
                    if (p <= Long.MAX_VALUE - scale.point(j)) {
                        p += scale.point(j);
                    } else {
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
