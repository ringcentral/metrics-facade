package com.ringcentral.platform.metrics.scale;

import java.util.*;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.lang.Math.*;

public class ExpScale implements Scale {

    private final List<Long> points;

    public ExpScale(
        double from,
        double factor,
        long stepCount,
        long max,
        boolean withInf) {

        checkArgument(from <= max, "from must be <= max");
        long fromLong = round(from);
        checkArgument(fromLong >= 0, "round(from) must be >= 0");
        checkArgument(factor >= 0.01, "factor must be >= 0.01");
        checkArgument(factor < max, "factor must be < max");
        checkArgument(stepCount >= 0, "stepCount must be >= 0");

        this.points = new ArrayList<>();
        this.points.add(fromLong);
        long lastPoint = fromLong;

        for (int i = 0; i < stepCount; ++i) {
            long p = round(lastPoint * factor);

            if (p == lastPoint) {
                ++p;
            }

            if (p > lastPoint && p <= max) {
                this.points.add(p);
                lastPoint = p;
            } else {
                break;
            }
        }

        if (withInf && lastPoint != Long.MAX_VALUE) {
            this.points.add(Long.MAX_VALUE);
        }
    }

    @Override
    public List<Long> points() {
        return points;
    }
}
