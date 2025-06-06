package com.ringcentral.platform.metrics.benchmark.histogram;

import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;
import com.ringcentral.platform.metrics.defaultImpl.histogram.totals.ConsistentTotalsHistogramImpl;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode({ Mode.Throughput, Mode.AverageTime })
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ConsistentTotalsBasicConsistencyCheckBenchmark {

    @org.openjdk.jmh.annotations.State(Scope.Benchmark)
    public static class State {
        final ConsistentTotalsHistogramImpl totals = new ConsistentTotalsHistogramImpl();
    }

    @Group("writeReadNoDelays")
    @GroupThreads(8)
    @Benchmark
    public void write(State state) {
        state.totals.update(1);
    }

    @Group("writeReadNoDelays")
    @GroupThreads(4)
    @Benchmark
    public void read(State state) {
        HistogramSnapshot snapshot = state.totals.snapshot();

        if (snapshot.count() != snapshot.totalSum()) {
            throw new IllegalStateException("Snapshot is not consistent");
        }
    }

    public static class Driver {
        public static void main(String[] args) {
            Options options = new OptionsBuilder()
                .include(ConsistentTotalsBasicConsistencyCheckBenchmark.class.getSimpleName())
                .warmupIterations(2)
                .measurementIterations(5)
                .measurementTime(TimeValue.minutes(1L))
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
