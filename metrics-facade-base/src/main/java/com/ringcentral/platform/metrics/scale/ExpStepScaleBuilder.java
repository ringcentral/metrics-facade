package com.ringcentral.platform.metrics.scale;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;

public class ExpStepScaleBuilder implements ScaleBuilder<ExpStepScale> {

    private long from;
    private long base;
    private double factor = 2.0;
    private long stepCount = Long.MAX_VALUE;
    private long max = Long.MAX_VALUE;
    private boolean withInf;

    public static ExpStepScaleBuilder expStep() {
        return expStepScaleBuilder();
    }

    public static ExpStepScaleBuilder expStepScale() {
        return expStepScaleBuilder();
    }

    public static ExpStepScaleBuilder expStepScaleBuilder() {
        return new ExpStepScaleBuilder();
    }

    public ExpStepScaleBuilder from(long from) {
        this.from = from;
        return this;
    }

    public ExpStepScaleBuilder base(long base) {
        checkArgument(base > 0, "base must be > 0");
        this.base = base;
        return this;
    }

    public ExpStepScaleBuilder factor(double factor) {
        checkArgument(factor >= 0.01, "factor must be >= 0.01");
        this.factor = factor;
        return this;
    }

    public ExpStepScaleBuilder steps(long stepCount) {
        checkArgument(stepCount >= 0, "stepCount must be >= 0");
        this.stepCount = stepCount;
        return this;
    }

    public ExpStepScaleBuilder max(long max) {
        this.max = max;
        return this;
    }

    public ExpStepScaleBuilder withInf() {
        return withInf(true);
    }

    public ExpStepScaleBuilder withInf(boolean withInf) {
        this.withInf = withInf;
        return this;
    }

    @Override
    public ExpStepScale build() {
        checkArgument(from <= max, "from must be <= max");
        checkArgument(base > 0, "base must be > 0");
        checkArgument(factor >= 0.01, "factor must be >= 0.01");
        checkArgument(base * factor > base, "(base * factor) must be > base");
        checkArgument(factor < max, "factor must be < max");
        checkArgument(stepCount >= 0, "stepCount must be >= 0");

        return new ExpStepScale(from, base, factor, stepCount, max, withInf);
    }
}
