package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;
import com.ringcentral.platform.metrics.histogram.Histogram;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An implementation of {@link HistogramImpl} that ensures consistent reporting of totals:
 * {@link Counter.Count} and {@link Histogram.TotalSum}. No other values are reported.
 * <p>
 * This implementation guarantees that all updates are fully reflected in the snapshot.
 * That is, {@link Counter.Count} and {@link Histogram.TotalSum} reported by {@link #snapshot()}
 * will always correspond to the same set of updates, with no partial or missing data.
 */
@SuppressWarnings("ConstantConditions")
public class ConsistentTotalsHistogramImpl implements TotalsHistogramImpl {

    /**
     * Maximum number of iterations the {@link #snapshot()} loop will run.
     *
     * <p>If this limit is hit, we assume something has gone badly wrong
     * (for example, writers stuck in livelock) and return the most recent
     * values instead of spinning forever.
     *
     * <p>The cap - 250k iterations - is far above the expected worst case
     * yet low enough to stop quickly if the system ever enters a pathological state.
     */
    public static final int DEFAULT_SNAPSHOT_MAX_ITER_COUNT = 250_000;

    private final int snapshotMaxIterCount;
    private final AtomicLong counter;
    private final AtomicLong totalSumAdder;
    private final AtomicLong updateCounter;

    public ConsistentTotalsHistogramImpl() {
        this(DEFAULT_SNAPSHOT_MAX_ITER_COUNT);
    }

    public ConsistentTotalsHistogramImpl(int snapshotMaxIterCount) {
        this(snapshotMaxIterCount, new AtomicLong(), new AtomicLong(), new AtomicLong());
    }

    ConsistentTotalsHistogramImpl(
        int snapshotMaxIterCount,
        @Nonnull AtomicLong counter,
        @Nonnull AtomicLong totalSumAdder,
        @Nonnull AtomicLong updateCounter) {

        this.snapshotMaxIterCount = snapshotMaxIterCount;
        this.counter = counter;
        this.totalSumAdder = totalSumAdder;
        this.updateCounter = updateCounter;
    }

    @Override
    public void update(long value) {
        // https://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.4.4:
        //   A synchronization order is a total order over all of the synchronization actions of an execution.
        //   For each thread t, the synchronization order of the synchronization actions (§17.4.2) in t is consistent
        //   with the program order (§17.4.3) of t.
        //
        // Therefore, the three writes below are guaranteed to happen in program order.
        // Since snapshot() reads these fields in reverse order, we cannot observe a torn update if the snapshot()'s
        // loop condition breaks, i.e. if count == updateCount.
        //
        // Indeed, after reading updateCount = updateCounter.get(), we must see all prior complete updates and
        // possibly some incomplete ones (those that haven't incremented updateCounter yet).
        // The only interesting case is the one with incomplete updates.
        // Every update always begins by incrementing the counter; prior to this step the update impacts nothing and
        // shouldn't be considered. Consequently, if we do not see a larger counter value in the loop's condition,
        // no new updates have been started yet, and it's safe to proceed with the current values.

        // We must write counter first to ensure the consistency of the totals.
        counter.incrementAndGet();
        totalSumAdder.addAndGet(value);

        // We must write updateCounter last.
        updateCounter.incrementAndGet();
    }

    @Override
    public HistogramSnapshot snapshot() {
        MutableTotalsHistogramSnapshot snapshot = new MutableTotalsHistogramSnapshot();
        fillSnapshot(snapshot);
        return snapshot;
    }

    @Override
    public void fillSnapshot(@Nonnull MutableTotalsHistogramSnapshot snapshot) {
        long count;
        long totalSum;
        long updateCount;
        int retryCount = 0;

        do {
            // We must read updateCounter first.
            updateCount = updateCounter.get();
            totalSum = totalSumAdder.get();

            // We must read counter last.
            count = counter.get();

            if (count != updateCount) {
                // We will retry on the next loop pass.
                ++retryCount;
                onSpinWaitImpl();
            }
        } while (count != updateCount && retryCount < snapshotMaxIterCount);

        snapshot.setCount(count);
        snapshot.setTotalSum(totalSum);
    }

    protected void onSpinWaitImpl() {
        Thread.onSpinWait();
    }
}