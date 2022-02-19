package com.ringcentral.platform.metrics.scale;

import java.util.*;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;

public class LinearScale implements Scale {

    private final List<Long> points;

    public LinearScale(
        long from,
        long step,
        long stepCount,
        boolean withInf) {

        checkArgument(stepCount >= 0, "stepCount must be >= 0");
        checkArgument(stepCount == 0 || step > 0, "step must be > 0 when stepCount > 0");

        this.points = new ArrayList<>((int)stepCount + 2);
        this.points.add(from);
        long p = from;

        for (int i = 0; i < stepCount; ++i) {
            if (p <= Long.MAX_VALUE - step) {
                p += step;
                this.points.add(p);
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
