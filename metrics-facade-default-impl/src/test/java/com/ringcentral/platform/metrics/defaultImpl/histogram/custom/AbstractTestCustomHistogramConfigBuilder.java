package com.ringcentral.platform.metrics.defaultImpl.histogram.custom;

import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.HistogramImplConfig;
import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.HistogramImplConfigBuilder;

@SuppressWarnings("unchecked")
public abstract class AbstractTestCustomHistogramConfigBuilder<
    C extends HistogramImplConfig,
    CB extends AbstractTestCustomHistogramConfigBuilder<C, CB>> implements HistogramImplConfigBuilder<C> {

    protected long measurableValue;

    public CB measurableValue(long measurableValue) {
        this.measurableValue = measurableValue;
        return (CB)this;
    }
}
