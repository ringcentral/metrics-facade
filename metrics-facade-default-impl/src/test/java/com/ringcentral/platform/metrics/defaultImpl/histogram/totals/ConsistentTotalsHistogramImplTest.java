package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

public class ConsistentTotalsHistogramImplTest extends AbstractTotalsHistogramImplTest<ConsistentTotalsHistogramImpl> {

    @Override
    protected ConsistentTotalsHistogramImpl makeHistogramImpl() {
        return new ConsistentTotalsHistogramImpl();
    }
}