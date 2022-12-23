package com.ringcentral.platform.metrics.defaultImpl.rate.custom;

import com.ringcentral.platform.metrics.defaultImpl.rate.configs.RateImplConfig;
import com.ringcentral.platform.metrics.defaultImpl.rate.configs.RateImplConfigBuilder;

@SuppressWarnings("unchecked")
public abstract class AbstractTestCustomRateConfigBuilder<
    C extends RateImplConfig,
    CB extends AbstractTestCustomRateConfigBuilder<C, CB>> implements RateImplConfigBuilder<C> {

    protected long measurableValue;

    public CB measurableValue(long measurableValue) {
        this.measurableValue = measurableValue;
        return (CB)this;
    }
}
