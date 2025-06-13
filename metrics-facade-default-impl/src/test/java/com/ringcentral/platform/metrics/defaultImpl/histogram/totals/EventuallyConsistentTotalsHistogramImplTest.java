package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

public class EventuallyConsistentTotalsHistogramImplTest extends AbstractTotalsHistogramImplTest<EventuallyConsistentTotalsHistogramImpl> {

    @Override
    protected EventuallyConsistentTotalsHistogramImpl makeHistogramImpl() {
        return new EventuallyConsistentTotalsHistogramImpl();
    }
}