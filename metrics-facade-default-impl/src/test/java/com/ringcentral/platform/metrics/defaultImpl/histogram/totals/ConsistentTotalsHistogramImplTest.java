package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.atomic.AtomicLong;

import static com.ringcentral.platform.metrics.defaultImpl.histogram.totals.ConsistentTotalsHistogramImpl.DEFAULT_SNAPSHOT_MAX_ITER_COUNT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class ConsistentTotalsHistogramImplTest extends AbstractTotalsHistogramImplTest<ConsistentTotalsHistogramImpl> {

    @Override
    protected ConsistentTotalsHistogramImpl makeHistogramImpl() {
        return new ConsistentTotalsHistogramImpl();
    }

    /**
     * Basic logic test without real concurrency.
     */
    @Test
    public void basicConsistency() {
        // given
        AtomicLong parentCounter = new AtomicLong();
        AtomicLong counter = spy(AtomicLong.class);
        AtomicLong totalSumAdder = spy(new AtomicLong());
        AtomicLong updateCounter = spy(new AtomicLong());

        doAnswer(new Answer<>() {

            private boolean firstCall = true;

            @Override
            public Long answer(InvocationOnMock inv) {
                if (firstCall) {
                    firstCall = false;
                    long nextCount = parentCounter.incrementAndGet();
                    totalSumAdder.addAndGet(5);
                    updateCounter.incrementAndGet();
                    return nextCount;
                } else {
                    return parentCounter.get();
                }
            };
        }).when(counter).get();

        doAnswer(inv -> parentCounter.incrementAndGet()).when(counter).incrementAndGet();
        ConsistentTotalsHistogramImpl histogram = new ConsistentTotalsHistogramImpl(DEFAULT_SNAPSHOT_MAX_ITER_COUNT, counter, totalSumAdder, updateCounter);

        // when
        histogram.update(10);

        // then: these values are yielded in the second iteration of the snapshot() method's loop.
        HistogramSnapshot snapshot = histogram.snapshot();
        assertThat(snapshot.count(), is(2L));
        assertThat(snapshot.totalSum(), is(10L + 5L)); // doAnswer: totalSumAdder.addAndGet(5)
        verify(counter, times(2)).get();
        verify(totalSumAdder, times(2)).get();
        verify(updateCounter, times(2)).get();
    }

    @Test
    public void snapshotIterCountLimit() {
        // given
        AtomicLong parentCounter = new AtomicLong();
        AtomicLong counter = spy(AtomicLong.class);
        AtomicLong totalSumAdder = spy(new AtomicLong());
        AtomicLong updateCounter = spy(new AtomicLong());

        // makes snapshot()'s consistency check fail
        doAnswer((Answer<Object>)inv -> {
            long nextCount = parentCounter.incrementAndGet();
            totalSumAdder.addAndGet(5);
            updateCounter.incrementAndGet();
            return nextCount;
        }).when(counter).get();

        doAnswer(inv -> parentCounter.incrementAndGet()).when(counter).incrementAndGet();
        ConsistentTotalsHistogramImpl histogram = new ConsistentTotalsHistogramImpl(3, counter, totalSumAdder, updateCounter);

        // when
        histogram.update(10);

        // then: these values are yielded in the second iteration of the snapshot() method's loop.
        HistogramSnapshot snapshot = histogram.snapshot();
        assertThat(snapshot.count(), is(3L + 1L));
        assertThat(snapshot.totalSum(), is((3L + 1L) * 5)); // doAnswer: totalSumAdder.addAndGet(5)
        verify(counter, times(3)).get();
        verify(totalSumAdder, times(3)).get();
        verify(updateCounter, times(3)).get();
    }
}