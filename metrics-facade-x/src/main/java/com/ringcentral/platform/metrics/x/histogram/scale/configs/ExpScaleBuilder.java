package com.ringcentral.platform.metrics.x.histogram.scale.configs;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;

public class ExpScaleBuilder implements ScaleBuilder<ExpScale> {

    private long from;
    private long base;
    private double factor = 2.0;
    private long stepCount = Long.MAX_VALUE;
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

    public ExpScaleBuilder base(long base) {
        checkArgument(base > 0, "base must be > 0");
        this.base = base;
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
        checkArgument(base > 0, "base must be > 0");
        checkArgument(factor >= 0.01, "factor must be >= 0.01");
        checkArgument(base * factor > base, "(base * factor) must be > base");
        checkArgument(factor < max, "factor must be < max");
        checkArgument(stepCount >= 0, "stepCount must be >= 0");

        return new ExpScale(from, base, factor, stepCount, max, withInf);
    }
}
