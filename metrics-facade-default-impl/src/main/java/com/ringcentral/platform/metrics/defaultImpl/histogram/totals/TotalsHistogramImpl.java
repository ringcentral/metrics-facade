package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramImpl;
import com.ringcentral.platform.metrics.histogram.Histogram.TotalSum;

import javax.annotation.Nonnull;

import static com.ringcentral.platform.metrics.counter.Counter.Count;

public interface TotalsHistogramImpl extends HistogramImpl {

    /**
     * Fills the given snapshot with the totals specific to this implementation.
     * <p>
     * For example, if the implementation tracks only {@link Count},
     * it will invoke {@link MutableTotalsHistogramSnapshot#setCount(long)}.
     * If it tracks both {@link Count} and {@link TotalSum},
     * it will invoke both {@link MutableTotalsHistogramSnapshot#setCount(long)} and
     * {@link MutableTotalsHistogramSnapshot#setTotalSum(long)}.
     *
     * @param snapshot the snapshot object to be filled
     */
    void fillSnapshot(@Nonnull MutableTotalsHistogramSnapshot snapshot);
}
