package com.ringcentral.platform.metrics.x.histogram.scale.configs;

import java.util.*;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;

public class ExpScale implements Scale {

    private final List<Long> points;

    public ExpScale(
        long from,
        long base,
        double factor,
        long stepCount,
        long max,
        boolean withInf) {

        checkArgument(from <= max, "from must be <= max");
        checkArgument(base > 0, "base must be > 0");
        checkArgument(factor >= 0.01, "factor must be >= 0.01");
        checkArgument(base * factor > base, "(base * factor) must be > base");
        checkArgument(factor < max, "factor must be < max");
        checkArgument(stepCount >= 0, "stepCount must be >= 0");

        this.points = new ArrayList<>();
        this.points.add(from);
        long p = from;
        long increase = base;

        for (int i = 0; i < stepCount; ++i) {
            if (p <= max - increase) {
                p += increase;
                this.points.add(p);

                if (increase < max / factor) {
                    increase *= factor;
                } else {
                    if (increase - max / factor < 1.0) {
                        this.points.add(max);
                    }

                    break;
                }
            } else {
                break;
            }
        }

        if (withInf && p != Long.MAX_VALUE) {
            this.points.add(Long.MAX_VALUE);
        }
    }

    @Override
    public List<Long> points() {
        return points;
    }
}
