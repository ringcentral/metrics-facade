package com.ringcentral.platform.metrics.scale;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.lang.Math.round;

public class ExpScaleBuilder implements ScaleBuilder<ExpScale> {

    private double from = 0.0;
    private double factor = 2.0;
    private long stepCount = 64;
    private long max = Long.MAX_VALUE;
    private boolean withInf;

    public static ExpScaleBuilder exp() {
        return expScaleBuilder();
    }

    public static ExpScaleBuilder expScale() {
        return expScaleBuilder();
    }

    public static ExpScaleBuilder expScaleBuilder() {
        return new ExpScaleBuilder();
    }

    public ExpScaleBuilder from(long from) {
        this.from = from;
        return this;
    }

    public ExpScaleBuilder factor(double factor) {
        checkArgument(factor >= 0.01, "factor must be >= 0.01");
        this.factor = factor;
        return this;
    }

    public ExpScaleBuilder steps(long stepCount) {
        checkArgument(stepCount >= 0, "stepCount must be >= 0");
        this.stepCount = stepCount;
        return this;
    }

    public ExpScaleBuilder max(long max) {
        this.max = max;
        return this;
    }

    public ExpScaleBuilder withInf() {
        return withInf(true);
    }

    public ExpScaleBuilder withInf(boolean withInf) {
        this.withInf = withInf;
        return this;
    }

    @Override
    public ExpScale build() {
        checkArgument(from <= max, "from must be <= max");
        long fromLong = round(from);
        checkArgument(fromLong >= 0, "round(from) must be >= 0");
        checkArgument(factor >= 0.01, "factor must be >= 0.01");
        checkArgument(factor < max, "factor must be < max");
        checkArgument(stepCount >= 0, "stepCount must be >= 0");

        return new ExpScale(from, factor, stepCount, max, withInf);
    }
}
