package com.ringcentral.platform.metrics.benchmark.histogram;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.x.XMetricRegistry;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.x.histogram.hdr.configs.HdrXHistogramImplConfigBuilder.hdrImpl;
import static com.ringcentral.platform.metrics.x.histogram.hdr.configs.OverflowBehavior.REDUCE_TO_HIGHEST_TRACKABLE;
import static java.util.concurrent.TimeUnit.MINUTES;

@BenchmarkMode({ Mode.Throughput, Mode.AverageTime })
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class HistogramBenchmark {

    @org.openjdk.jmh.annotations.State(Scope.Benchmark)
    public static class State {
        // dw
        final MetricRegistry dwMetricRegistry = new DropwizardMetricRegistry();

        final Histogram dwHistogram = dwMetricRegistry.histogram(
            withName("dwHistogram"),
            () -> withHistogram().measurables(
                COUNT,
                MIN,
                MAX,
                MEAN,
                PERCENTILE_50,
                PERCENTILE_90,
                PERCENTILE_99));

        // x
        final MetricRegistry xMetricRegistry = new XMetricRegistry();

        final Histogram hdrHistogram_neverReset_allMeasurables = xMetricRegistry.histogram(
            withName("xHistogram", "neverReset", "allMeasurables"),
            () -> withHistogram()
                .measurables(COUNT, MIN, MAX, MEAN, PERCENTILE_50, PERCENTILE_90, PERCENTILE_99)
                .with(hdrImpl().highestTrackableValue(MINUTES.toNanos(1L), REDUCE_TO_HIGHEST_TRACKABLE)));

        final Histogram xHistogram_neverReset_count_mean = xMetricRegistry.histogram(
            withName("xHistogram", "neverReset", "count", "mean"),
            () -> withHistogram()
                .measurables(COUNT, MEAN)
                .with(hdrImpl().highestTrackableValue(MINUTES.toNanos(1L), REDUCE_TO_HIGHEST_TRACKABLE)));

        final Histogram hdrHistogram_resetOnSnapshot_allMeasurables = xMetricRegistry.histogram(
            withName("xHistogram", "resetOnSnapshot", "allMeasurables"),
            () -> withHistogram()
                .measurables(COUNT, MIN, MAX, MEAN, PERCENTILE_50, PERCENTILE_90, PERCENTILE_99)
                .with(hdrImpl().resetOnSnapshot().highestTrackableValue(MINUTES.toNanos(1L), REDUCE_TO_HIGHEST_TRACKABLE)));

        final Histogram xHistogram_resetOnSnapshot_count_mean = xMetricRegistry.histogram(
            withName("xHistogram", "resetOnSnapshot", "count", "mean"),
            () -> withHistogram()
                .measurables(COUNT, MEAN)
                .with(hdrImpl().resetOnSnapshot().highestTrackableValue(MINUTES.toNanos(1L), REDUCE_TO_HIGHEST_TRACKABLE)));

        // test counter
        AtomicLong counter = new AtomicLong();
    }

    @Benchmark
    @Group("dwHistogram")
    public void dwHistogram_update(State state) {
        state.dwHistogram.update(value(state));
    }

    private long value(State state) {
        long value = state.counter.incrementAndGet();

        if (value > 10000000L) {
            state.counter.set(0L);
        }

        return value;
    }

    @Benchmark
    @Group("hdrHistogram_neverReset_allMeasurables")
    public void xRate_neverReset_allMeasurables(State state) {
        state.hdrHistogram_neverReset_allMeasurables.update(value(state));
    }

    @Benchmark
    @Group("xHistogram_neverReset_count_mean")
    public void xHistogram_neverReset_count_mean(State state) {
        state.xHistogram_neverReset_count_mean.update(value(state));
    }

    @Benchmark
    @Group("hdrHistogram_resetOnSnapshot_allMeasurables")
    public void xRate_resetOnSnapshot_allMeasurables(State state) {
        state.hdrHistogram_resetOnSnapshot_allMeasurables.update(value(state));
    }

    @Benchmark
    @Group("xHistogram_resetOnSnapshot_count_mean")
    public void xHistogram_resetOnSnapshot_count_mean(State state) {
        state.xHistogram_resetOnSnapshot_count_mean.update(value(state));
    }

    public static class SixteenThreads {
        public static void main(String[] args) {
            Options options = new OptionsBuilder()
                .include(HistogramBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .measurementTime(TimeValue.minutes(1L))
                .threads(16)
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
