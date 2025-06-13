package com.ringcentral.platform.metrics.defaultImpl.histogram;

import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.TotalsMeasurementType;
import com.ringcentral.platform.metrics.defaultImpl.histogram.totals.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
import org.junit.Test;

import javax.annotation.Nonnull;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.configs.TotalsMeasurementType.*;
import static com.ringcentral.platform.metrics.histogram.Histogram.TOTAL_SUM;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class AbstractHistogramImplTest<H extends AbstractHistogramImpl> {

    protected abstract H makeHistogramImpl(@Nonnull TotalsMeasurementType totalsMeasurementType, @Nonnull Measurable... measurables);

    @Test
    public void totalsOnly() {
        assertThat(makeHistogramImpl(CONSISTENT, COUNT).parent(), is(instanceOf(CountHistogramImpl.class)));
        assertThat(makeHistogramImpl(EVENTUALLY_CONSISTENT, COUNT).parent(), is(instanceOf(CountHistogramImpl.class)));

        assertThat(makeHistogramImpl(CONSISTENT, TOTAL_SUM).parent(), is(instanceOf(TotalSumHistogramImpl.class)));
        assertThat(makeHistogramImpl(EVENTUALLY_CONSISTENT, TOTAL_SUM).parent(), is(instanceOf(TotalSumHistogramImpl.class)));

        assertThat(makeHistogramImpl(CONSISTENT, COUNT, TOTAL_SUM).parent(), is(instanceOf(ConsistentTotalsHistogramImpl.class)));
        assertThat(makeHistogramImpl(EVENTUALLY_CONSISTENT, COUNT, TOTAL_SUM).parent(), is(instanceOf(EventuallyConsistentTotalsHistogramImpl.class)));

        // no Measurables
        assertThat(makeHistogramImpl(EVENTUALLY_CONSISTENT).parent(), is(instanceOf(NoOpHistogramImpl.class)));
    }
}
