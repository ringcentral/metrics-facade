package com.ringcentral.platform.metrics.x.histogram.scale.configs;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;

public class ExpScaleBuilder implements ScaleBuilder<ExpScale> {

    private long from;
    private long base;
    private long factor;
    private long stepCount = -1;
    private long max;
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

    public ExpScaleBuilder factor(long factor) {
        checkArgument(factor > 0, "factor must be > 0");
        this.factor = factor;
        return this;
    }

    public ExpScaleBuilder steps(long stepCount) {
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
        checkArgument(factor < max, "factor must be < max");
        return new ExpScale(from, base, factor, stepCount, max, withInf);
    }
}
