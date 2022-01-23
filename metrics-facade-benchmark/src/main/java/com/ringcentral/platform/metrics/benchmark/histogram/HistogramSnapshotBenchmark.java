package com.ringcentral.platform.metrics.benchmark.histogram;

import com.github.rollingmetrics.histogram.OverflowResolver;
import com.github.rollingmetrics.histogram.hdr.RollingHdrHistogram;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.x.XMetricRegistry;
import com.ringcentral.platform.metrics.x.histogram.hdr.configs.HdrXHistogramImplConfig;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.time.Duration;
import java.util.concurrent.*;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.x.histogram.hdr.configs.HdrXHistogramImplConfigBuilder.hdrImpl;
import static com.ringcentral.platform.metrics.x.histogram.hdr.configs.OverflowBehavior.REDUCE_TO_HIGHEST_TRACKABLE;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.*;

@BenchmarkMode({ Mode.Throughput, Mode.AverageTime })
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class HistogramSnapshotBenchmark {

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

        final Histogram hdrXHistogram_NeverReset = xMetricRegistry.histogram(
            withName("hdrXHistogram", "neverReset"),
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
                    .neverReset()
                    .highestTrackableValue(MINUTES.toNanos(2L), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_NeverReset_Count_Mean = xMetricRegistry.histogram(
            withName("hdrXHistogram", "neverReset", "count", "mean"),
            () -> withHistogram()
                .measurables(COUNT, MEAN)
                .with(hdrImpl()
                    .neverReset()
                    .highestTrackableValue(MINUTES.toNanos(2L), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

//        final Histogram hdrXHistogram_ResetOnSnapshot = xMetricRegistry.histogram(
//            withName("hdrXHistogram", "resetOnSnapshot"),
//            () -> withHistogram()
//                .measurables(
//                    COUNT,
//                    MIN,
//                    MAX,
//                    MEAN,
//                    PERCENTILE_50,
//                    PERCENTILE_75,
//                    PERCENTILE_90,
//                    PERCENTILE_99)
//                .with(hdrImpl()
//                    .resetOnSnapshot()
//                    .highestTrackableValue(MINUTES.toNanos(2L), REDUCE_TO_HIGHEST_TRACKABLE)));
//
//        final Histogram hdrXHistogram_ResetOnSnapshot_Count_Mean = xMetricRegistry.histogram(
//            withName("hdrXHistogram", "resetOnSnapshot", "count", "mean"),
//            () -> withHistogram()
//                .measurables(COUNT, MEAN)
//                .with(hdrImpl()
//                    .resetOnSnapshot()
//                    .highestTrackableValue(MINUTES.toNanos(2L), REDUCE_TO_HIGHEST_TRACKABLE)));

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
                    .highestTrackableValue(MINUTES.toNanos(2L), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_ResetByChunks_Count_Mean = xMetricRegistry.histogram(
            withName("hdrXHistogram", "resetByChunks", "count", "mean"),
            () -> withHistogram()
                .measurables(COUNT, MEAN)
                .with(hdrImpl()
                    .highestTrackableValue(MINUTES.toNanos(2L), REDUCE_TO_HIGHEST_TRACKABLE)
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
                    .highestTrackableValue(MINUTES.toNanos(2L), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_ResetByChunks_Count_Mean_3_Digits = xMetricRegistry.histogram(
            withName("hdrXHistogram", "resetByChunks", "count", "mean", "3digits"),
            () -> withHistogram()
                .measurables(COUNT, MEAN)
                .with(hdrImpl()
                    .significantDigits(3)
                    .highestTrackableValue(MINUTES.toNanos(2L), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final RollingHdrHistogram rollingHistogram_ResetByChunks = RollingHdrHistogram.builder()
            .resetReservoirPeriodicallyByChunks(
                Duration.ofMillis(HdrXHistogramImplConfig.DEFAULT.chunkResetPeriodMs() * HdrXHistogramImplConfig.DEFAULT.chunkCount()),
                HdrXHistogramImplConfig.DEFAULT.chunkCount())
            .withHighestTrackableValue(MINUTES.toNanos(2L), OverflowResolver.REDUCE_TO_HIGHEST_TRACKABLE)
            .withLowestDiscernibleValue(MILLISECONDS.toNanos(1))
            .build();

        @Setup
        public void setup() throws InterruptedException {
            long allChunksResetPeriodMs = HdrXHistogramImplConfig.DEFAULT.chunkResetPeriodMs() * HdrXHistogramImplConfig.DEFAULT.chunkCount();
            long sampleCount = (allChunksResetPeriodMs + SECONDS.toMillis(30L)) / 10L;
            System.out.println("Sample count: " + sampleCount);

            for (int i = 0; i < sampleCount; ++i) {
                long duration = ThreadLocalRandom.current().nextLong(SECONDS.toNanos(10)) + MILLISECONDS.toNanos(10);

                dwHistogram.update(duration);
                hdrXHistogram_NeverReset.update(duration);
                hdrXHistogram_NeverReset_Count_Mean.update(duration);
//                hdrXHistogram_ResetOnSnapshot.update(duration);
//                hdrXHistogram_ResetOnSnapshot_Count_Mean.update(duration);
                hdrXHistogram_ResetByChunks.update(duration);
                hdrXHistogram_ResetByChunks_Count_Mean.update(duration);
                hdrXHistogram_ResetByChunks_3_Digits.update(duration);
                hdrXHistogram_ResetByChunks_Count_Mean_3_Digits.update(duration);
                rollingHistogram_ResetByChunks.update(duration);

                sleep(10L);
            }
        }
    }

    @Benchmark
    public void dwHistogram_Snapshot(State state) {
        state.dwHistogram.iterator().next().measurableValues().valueOf(MEAN);
    }

    @Benchmark
    public void hdrXHistogram_NeverReset_Snapshot(State state) {
        state.hdrXHistogram_NeverReset.iterator().next().measurableValues();
    }

    @Benchmark
    public void hdrXHistogram_NeverReset_Count_Mean_Snapshot(State state) {
        state.hdrXHistogram_NeverReset_Count_Mean.iterator().next().measurableValues().valueOf(MEAN);
    }

//    @Benchmark
//    public void hdrXHistogram_ResetOnSnapshot_Snapshot(State state) {
//        state.hdrXHistogram_ResetOnSnapshot.iterator().next().measurableValues();
//    }
//
//    @Benchmark
//    public void hdrXHistogram_ResetOnSnapshot_Count_Mean_Snapshot(State state) {
//        state.hdrXHistogram_ResetOnSnapshot_Count_Mean.iterator().next().measurableValues();
//    }

    @Benchmark
    public void hdrXHistogram_ResetByChunks_Snapshot(State state) {
        state.hdrXHistogram_ResetByChunks.iterator().next().measurableValues().valueOf(MEAN);
    }

    @Benchmark
    public void hdrXHistogram_ResetByChunks_Count_Mean_Snapshot(State state) {
        state.hdrXHistogram_ResetByChunks_Count_Mean.iterator().next().measurableValues().valueOf(MEAN);
    }

    @Benchmark
    public void hdrXHistogram_ResetByChunks_3_Digits_Snapshot(State state) {
        state.hdrXHistogram_ResetByChunks_3_Digits.iterator().next().measurableValues().valueOf(MEAN);
    }

    @Benchmark
    public void hdrXHistogram_ResetByChunks_Count_Mean_3_Digits_Snapshot(State state) {
        state.hdrXHistogram_ResetByChunks_Count_Mean_3_Digits.iterator().next().measurableValues().valueOf(MEAN);
    }

    @Benchmark
    public void rollingHistogram_ResetByChunks_Snapshot(State state) {
        state.rollingHistogram_ResetByChunks.getSnapshot().getMean();
    }

    public static class Driver {
        public static void main(String[] args) {
            Options options = new OptionsBuilder()
                .include(HistogramSnapshotBenchmark.class.getSimpleName())
                .warmupIterations(3)
                .measurementIterations(3)
                .measurementTime(TimeValue.seconds(10L))
                .threads(1)
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
