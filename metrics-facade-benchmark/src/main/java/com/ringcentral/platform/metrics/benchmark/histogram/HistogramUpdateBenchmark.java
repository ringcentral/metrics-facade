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
import static java.util.concurrent.TimeUnit.*;

@BenchmarkMode({ Mode.Throughput, Mode.AverageTime })
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class HistogramUpdateBenchmark {

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
                PERCENTILE_75,
                PERCENTILE_90,
                PERCENTILE_99));

        // x
        final MetricRegistry xMetricRegistry = new XMetricRegistry();

        final Histogram hdrXHistogram_Uniform = xMetricRegistry.histogram(
            withName("hdrXHistogram", "uniform"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    MIN,
                    MAX,
                    MEAN,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    PERCENTILE_90,
                    PERCENTILE_99)
                .with(hdrImpl()
                    .uniform()
                    .highestTrackableValue(MINUTES.toNanos(1L), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_Uniform_Count_Mean = xMetricRegistry.histogram(
            withName("hdrXHistogram", "uniform", "count", "mean"),
            () -> withHistogram()
                .measurables(COUNT, MEAN)
                .with(hdrImpl()
                    .uniform()
                    .highestTrackableValue(MINUTES.toNanos(1L), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_ResetOnSnapshot = xMetricRegistry.histogram(
            withName("hdrXHistogram", "resetOnSnapshot"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    MIN,
                    MAX,
                    MEAN,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    PERCENTILE_90,
                    PERCENTILE_99)
                .with(hdrImpl()
                    .resetOnSnapshot()
                    .highestTrackableValue(MINUTES.toNanos(1L), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_ResetOnSnapshot_Count_Mean = xMetricRegistry.histogram(
            withName("hdrXHistogram", "resetOnSnapshot", "count", "mean"),
            () -> withHistogram()
                .measurables(COUNT, MEAN)
                .with(hdrImpl()
                    .resetOnSnapshot()
                    .highestTrackableValue(MINUTES.toNanos(1L), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_ResetByChunks = xMetricRegistry.histogram(
            withName("hdrXHistogram", "resetByChunks"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    MIN,
                    MAX,
                    MEAN,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    PERCENTILE_90,
                    PERCENTILE_99)
                .with(hdrImpl()
                    .highestTrackableValue(MINUTES.toNanos(1L), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_ResetByChunks_Count_Mean = xMetricRegistry.histogram(
            withName("hdrXHistogram", "resetByChunks", "count", "mean"),
            () -> withHistogram()
                .measurables(COUNT, MEAN)
                .with(hdrImpl()
                    .highestTrackableValue(MINUTES.toNanos(1L), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_ResetByChunks_3_Digits = xMetricRegistry.histogram(
            withName("hdrXHistogram", "resetByChunks", "3digits"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    MIN,
                    MAX,
                    MEAN,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    PERCENTILE_90,
                    PERCENTILE_99)
                .with(hdrImpl()
                    .significantDigits(3)
                    .highestTrackableValue(MINUTES.toNanos(1L), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_ResetByChunks_Count_Mean_3_Digits = xMetricRegistry.histogram(
            withName("hdrXHistogram", "resetByChunks", "count", "mean", "3digits"),
            () -> withHistogram()
                .measurables(COUNT, MEAN)
                .with(hdrImpl()
                    .significantDigits(3)
                    .highestTrackableValue(MINUTES.toNanos(1L), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        // test counter
        final AtomicLong counter = new AtomicLong();
    }

    @Benchmark
    public void dwHistogram_Update(State state) {
        state.dwHistogram.update(value(state));
    }

    private long value(State state) {
        long value = state.counter.incrementAndGet();

        if (value > 1000000000L) {
            state.counter.set(0L);
        }

        return value;
    }

    @Benchmark
    public void hdrXHistogram_Uniform_Update(State state) {
        state.hdrXHistogram_Uniform.update(value(state));
    }

    @Benchmark
    public void hdrXHistogram_Uniform_Count_Mean_Update(State state) {
        state.hdrXHistogram_Uniform_Count_Mean.update(value(state));
    }

    @Benchmark
    public void hdrXHistogram_ResetOnSnapshot_Update(State state) {
        state.hdrXHistogram_ResetOnSnapshot.update(value(state));
    }

    @Benchmark
    public void hdrXHistogram_ResetOnSnapshot_Count_Mean_Update(State state) {
        state.hdrXHistogram_ResetOnSnapshot_Count_Mean.update(value(state));
    }

    @Benchmark
    public void hdrXHistogram_ResetByChunks_Update(State state) {
        state.hdrXHistogram_ResetByChunks.update(value(state));
    }

    @Benchmark
    public void hdrXHistogram_ResetByChunks_Count_Mean_Update(State state) {
        state.hdrXHistogram_ResetByChunks_Count_Mean.update(value(state));
    }

    @Benchmark
    public void hdrXHistogram_ResetByChunks_3_Digits_Update(State state) {
        state.hdrXHistogram_ResetByChunks_3_Digits.update(value(state));
    }

    @Benchmark
    public void hdrXHistogram_ResetByChunks_Count_Mean_3_Digits_Update(State state) {
        state.hdrXHistogram_ResetByChunks_Count_Mean_3_Digits.update(value(state));
    }

    public static class SixteenThreads {
        public static void main(String[] args) {
            Options options = new OptionsBuilder()
                .include(HistogramUpdateBenchmark.class.getSimpleName())
                .warmupIterations(3)
                .measurementIterations(3)
                .measurementTime(TimeValue.seconds(30L))
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
