package com.ringcentral.platform.metrics.benchmark.histogram;

import com.ringcentral.platform.metrics.defaultImpl.histogram.totals.ConsistentTotalsHistogramImpl;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.*;

import java.util.concurrent.locks.LockSupport;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

/**
 * We did not observe any significant performance difference, but
 * {@link Thread#onSpinWait()} is the recommended hint for busy-wait
 * loops, so we chose to leave it enabled.
 */
@BenchmarkMode({ Mode.AverageTime, Mode.SampleTime, Mode.Throughput })
@OutputTimeUnit(MICROSECONDS)
public class ConsistentTotalsSnapshotThreadOnSpinWaitBenchmark {

    public static class NoOpOnSpinWaitImpl extends ConsistentTotalsHistogramImpl {

        @Override
        protected void onSpinWaitImpl() {}
    }

    public static final int WRITER_THREAD_COUNT = 11;
    public static final int READER_THREAD_COUNT = 1;

    // 55 µs -> ~18k updates/s per writer -> up to ~200k in total with 11 writers
    public static final long WRITER_PAUSE = MICROSECONDS.toNanos(1) * 5 * WRITER_THREAD_COUNT;

    // 1 µs pause -> up to ~1m snapshots/s with a single reader
    public static final long READER_PAUSE = MICROSECONDS.toNanos(1) * READER_THREAD_COUNT;

    @org.openjdk.jmh.annotations.State(Scope.Benchmark)
    public static class State {
        final ConsistentTotalsHistogramImpl baseImpl = new ConsistentTotalsHistogramImpl();
        final NoOpOnSpinWaitImpl noOpOnSpinWaitImpl = new NoOpOnSpinWaitImpl();
    }

    @Benchmark
    @Group("baseImpl")
    @GroupThreads(11)
    public void baseImpl_Write(State state) {
        state.baseImpl.update(1);
        LockSupport.parkNanos(WRITER_PAUSE);
    }

    @Benchmark
    @Group("baseImpl")
    @GroupThreads(1)
    public void baseImpl_Read(State state) {
        state.baseImpl.snapshot();
        LockSupport.parkNanos(READER_PAUSE);
    }

    @Benchmark
    @Group("noOpOnSpinWaitImpl")
    @GroupThreads(11)
    public void noOpOnSpinWaitImpl_Write(State state) {
        state.noOpOnSpinWaitImpl.update(1);
        LockSupport.parkNanos(WRITER_PAUSE);
    }

    @Benchmark
    @Group("noOpOnSpinWaitImpl")
    @GroupThreads(1)
    public void noOpOnSpinWaitImpl_Read(State state) {
        state.noOpOnSpinWaitImpl.snapshot();
        LockSupport.parkNanos(READER_PAUSE);
    }

    public static class Driver {
        public static void main(String[] args) {
            Options options = new OptionsBuilder()
                .include(ConsistentTotalsSnapshotThreadOnSpinWaitBenchmark.class.getSimpleName())
                .warmupIterations(1)
                .measurementIterations(2)
                .syncIterations(false)
                .measurementTime(TimeValue.minutes(1))
                .threads(12)
                .forks(1)
                .build();
            try {
                new Runner(options).run();
            } catch (RunnerException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
