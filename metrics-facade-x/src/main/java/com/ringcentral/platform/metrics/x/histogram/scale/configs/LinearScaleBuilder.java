package com.ringcentral.platform.metrics.x.histogram.scale.configs;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;

public class LinearScaleBuilder implements ScaleBuilder<LinearScale> {

    private long from;
    private long step;
    private long stepCount;
    private boolean withInf;

    public static LinearScaleBuilder linear() {
        return linearScaleBuilder();
    }

    public static LinearScaleBuilder linearScale() {
        return linearScaleBuilder();
    }

    public static LinearScaleBuilder linearScaleBuilder() {
        return new LinearScaleBuilder();
    }

    public LinearScaleBuilder from(long from) {
        this.from = from;
        return this;
    }

    public LinearScaleBuilder steps(long step, long stepCount) {
        checkArgument(stepCount == 0 || step > 0, "stepCount must be > 0 when stepCount > 0");
        this.step = step;
        this.stepCount = stepCount;
        return this;
    }

    public LinearScaleBuilder withInf() {
        return withInf(true);
    }

    public LinearScaleBuilder withInf(boolean withInf) {
        this.withInf = withInf;
        return this;
    }

    @Override
    public LinearScale build() {
        return new LinearScale(from, step, stepCount, withInf);
    }
}
