package com.ringcentral.platform.metrics.benchmark.histogram;

import com.github.rollingmetrics.histogram.OverflowResolver;
import com.github.rollingmetrics.histogram.hdr.RollingHdrHistogram;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.x.XMetricRegistry;
import com.ringcentral.platform.metrics.x.histogram.hdr.configs.HdrXHistogramImplConfig;
import com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleBuilder;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.x.histogram.hdr.configs.HdrXHistogramImplConfigBuilder.hdrImpl;
import static com.ringcentral.platform.metrics.x.histogram.hdr.configs.OverflowBehavior.REDUCE_TO_HIGHEST_TRACKABLE;
import static com.ringcentral.platform.metrics.x.histogram.scale.configs.CompositeScaleBuilder.first;
import static com.ringcentral.platform.metrics.x.histogram.scale.configs.LinearScaleBuilder.linear;
import static com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleXHistogramImplConfigBuilder.scaleImpl;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.*;

@BenchmarkMode({ Mode.Throughput, Mode.AverageTime })
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@SuppressWarnings("DuplicatedCode")
public class HistogramSnapshotBenchmark {

    @SuppressWarnings("DuplicatedCode")
    @org.openjdk.jmh.annotations.State(Scope.Benchmark)
    public static class State {
        // dw
        final MetricRegistry dwMetricRegistry = new DropwizardMetricRegistry();

        final Histogram dwHistogram = dwMetricRegistry.histogram(
            withName("dwHistogram"),
            () -> withHistogram().measurables(
                COUNT,
                TOTAL_SUM,
                MIN,
                MAX,
                MEAN,
                PERCENTILE_50,
                PERCENTILE_75,
                PERCENTILE_90,
                PERCENTILE_99));

        // X - HDR
        final MetricRegistry xMetricRegistry = new XMetricRegistry();

        final Histogram hdrXHistogram_NeverReset = xMetricRegistry.histogram(
            withName("hdrXHistogram", "neverReset"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    TOTAL_SUM,
                    MIN,
                    MAX,
                    MEAN,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    PERCENTILE_90,
                    PERCENTILE_99)
                .with(hdrImpl()
                    .neverReset()
                    .highestTrackableValue(HOURS.toNanos(3), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_NeverReset_Count_Mean = xMetricRegistry.histogram(
            withName("hdrXHistogram", "neverReset", "count", "mean"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    MEAN)
                .with(hdrImpl()
                    .neverReset()
                    .highestTrackableValue(HOURS.toNanos(3), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_ResetByChunks = xMetricRegistry.histogram(
            withName("hdrXHistogram", "resetByChunks"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    TOTAL_SUM,
                    MIN,
                    MAX,
                    MEAN,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    PERCENTILE_90,
                    PERCENTILE_99)
                .with(hdrImpl()
                    .eventuallyConsistentTotals()
                    .resetByChunks()
                    .highestTrackableValue(HOURS.toNanos(3), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_ResetByChunks_Count_Mean = xMetricRegistry.histogram(
            withName("hdrXHistogram", "resetByChunks", "count", "mean"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    MEAN)
                .with(hdrImpl()
                    .resetByChunks()
                    .highestTrackableValue(HOURS.toNanos(3), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_ResetByChunks_3_Digits = xMetricRegistry.histogram(
            withName("hdrXHistogram", "resetByChunks", "3digits"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    TOTAL_SUM,
                    MIN,
                    MAX,
                    MEAN,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    PERCENTILE_90,
                    PERCENTILE_99)
                .with(hdrImpl()
                    .resetByChunks()
                    .significantDigits(3)
                    .highestTrackableValue(HOURS.toNanos(3), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_ResetByChunks_Count_Mean_3_Digits = xMetricRegistry.histogram(
            withName("hdrXHistogram", "resetByChunks", "count", "mean", "3digits"),
            () -> withHistogram()
                .measurables(COUNT, MEAN)
                .with(hdrImpl()
                    .resetByChunks()
                    .significantDigits(3)
                    .highestTrackableValue(HOURS.toNanos(3), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        // X - Scale
        final Histogram scaleXHistogram_NeverReset = xMetricRegistry.histogram(
            withName("scaleXHistogram", "neverReset"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    TOTAL_SUM,
                    MIN,
                    MAX,
                    MEAN,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    PERCENTILE_90,
                    PERCENTILE_99)
                .with(scaleImpl()
                    .neverReset()
                    .with(scale_1())));

        final Histogram scaleXHistogram_NeverReset_Count_Mean = xMetricRegistry.histogram(
            withName("scaleXHistogram", "neverReset", "count", "mean"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    MEAN)
                .with(scaleImpl()
                    .neverReset()
                    .with(scale_1())));

        final Histogram scaleXHistogram_ResetByChunks = xMetricRegistry.histogram(
            withName("scaleXHistogram", "resetByChunks"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    TOTAL_SUM,
                    MIN,
                    MAX,
                    MEAN,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    PERCENTILE_90,
                    PERCENTILE_99)
                .with(scaleImpl()
                    .resetByChunks()
                    .with(scale_1())));

        final Histogram scaleXHistogram_ResetByChunks_Count_Mean = xMetricRegistry.histogram(
            withName("scaleXHistogram", "resetByChunks", "count", "mean"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    MEAN)
                .with(scaleImpl()
                    .neverReset()
                    .with(scale_1())));

        // Rolling - HDR
        final com.codahale.metrics.Histogram rollingHdrHistogram_ResetByChunks = new com.codahale.metrics.Histogram(
            new RollingHdrReservoir(
                RollingHdrHistogram.builder()
                    .resetReservoirPeriodicallyByChunks(
                        Duration.ofMillis(HdrXHistogramImplConfig.DEFAULT.chunkResetPeriodMs() * HdrXHistogramImplConfig.DEFAULT.chunkCount()),
                        HdrXHistogramImplConfig.DEFAULT.chunkCount())
                    .withHighestTrackableValue(HOURS.toNanos(3), OverflowResolver.REDUCE_TO_HIGHEST_TRACKABLE)
                    .withLowestDiscernibleValue(MILLISECONDS.toNanos(1))
                    .build()));

        @Setup
        public void setup() throws InterruptedException {
            long allChunksResetPeriodMs = HdrXHistogramImplConfig.DEFAULT.chunkResetPeriodMs() * HdrXHistogramImplConfig.DEFAULT.chunkCount();
            long updateCount = (allChunksResetPeriodMs + SECONDS.toMillis(30)) / 10;
            System.out.println("Update count: " + updateCount);
            Random random = new Random(123);

            for (int i = 0; i < updateCount; ++i) {
                long durationMs;

                if (i % 3 == 0) {
                    durationMs = 200 + random.nextInt(75);
                } else if (i % 2 == 0) {
                    durationMs = 20 + random.nextInt(25);
                } else {
                    durationMs = 10 + random.nextInt(10);
                }

                if (i % 100 == 0) {
                    durationMs += 1000;
                }

                if (i % 200 == 0) {
                    durationMs += 1500;
                }

                if (i % 500 == 0) {
                    durationMs += 5000;
                }

                long duration = MILLISECONDS.toNanos(durationMs);

                // dw
                dwHistogram.update(duration);

                // X - HDR
                hdrXHistogram_NeverReset.update(duration);
                hdrXHistogram_NeverReset_Count_Mean.update(duration);
                hdrXHistogram_ResetByChunks.update(duration);
                hdrXHistogram_ResetByChunks_Count_Mean.update(duration);
                hdrXHistogram_ResetByChunks_3_Digits.update(duration);
                hdrXHistogram_ResetByChunks_Count_Mean_3_Digits.update(duration);

                // X - Scale
                scaleXHistogram_NeverReset.update(duration);
                scaleXHistogram_NeverReset_Count_Mean.update(duration);
                scaleXHistogram_ResetByChunks.update(duration);
                scaleXHistogram_ResetByChunks_Count_Mean.update(duration);

                // Rolling - HDR
                rollingHdrHistogram_ResetByChunks.update(duration);

                sleep(10L);
            }
        }
    }

    static ScaleBuilder<?> scale_1() {
        // 500 ms
        return first(linear().steps(MILLISECONDS.toNanos(5), 100))
            // 1 sec
            .then(linear().steps(MILLISECONDS.toNanos(25), 20))
            // 2 sec
            .then(linear().steps(MILLISECONDS.toNanos(100), 10))
            // 10 sec
            .then(linear().steps(SECONDS.toNanos(1), 8))
            // 30 sec
            .then(linear().steps(SECONDS.toNanos(5), 4))
            // 1 min
            .then(linear().steps(SECONDS.toNanos(10), 3))
            // 10 min
            .then(linear().steps(MINUTES.toNanos(1), 9))
            // 3 h
            .then(linear().steps(MINUTES.toNanos(10), 5 + 12).withInf());
    }

    static ScaleBuilder<?> scale_2() {
        return linear().from(1).steps(1, 9);
    }

    @Benchmark
    public void dwHistogram_Snapshot(State state) {
        state.dwHistogram.iterator().next().measurableValues();
    }

    @Benchmark
    public void hdrXHistogram_NeverReset_Snapshot(State state) {
        state.hdrXHistogram_NeverReset.iterator().next().measurableValues();
    }

//    @Benchmark
//    public void hdrXHistogram_NeverReset_Count_Mean_Snapshot(State state) {
//        state.hdrXHistogram_NeverReset_Count_Mean.iterator().next().measurableValues();
//    }
//
////    @Benchmark
////    public void hdrXHistogram_ResetOnSnapshot_Snapshot(State state) {
////        state.hdrXHistogram_ResetOnSnapshot.iterator().next();
////    }
////
////    @Benchmark
////    public void hdrXHistogram_ResetOnSnapshot_Count_Mean_Snapshot(State state) {
////        state.hdrXHistogram_ResetOnSnapshot_Count_Mean.iterator().next();
////    }
//
    @Benchmark
    public void hdrXHistogram_ResetByChunks_Snapshot(State state) {
        state.hdrXHistogram_ResetByChunks.iterator().next().measurableValues();
    }
//
//    @Benchmark
//    public void hdrXHistogram_ResetByChunks_Count_Mean_Snapshot(State state) {
//        state.hdrXHistogram_ResetByChunks_Count_Mean.iterator().next().measurableValues();
//    }
//
    @Benchmark
    public void hdrXHistogram_ResetByChunks_3_Digits_Snapshot(State state) {
        state.hdrXHistogram_ResetByChunks_3_Digits.iterator().next().measurableValues();
    }
//
//    @Benchmark
//    public void hdrXHistogram_ResetByChunks_Count_Mean_3_Digits_Snapshot(State state) {
//        state.hdrXHistogram_ResetByChunks_Count_Mean_3_Digits.iterator().next().measurableValues();
//    }

    @Benchmark
    public void scaleXHistogram_NeverReset_Snapshot(State state) {
        state.scaleXHistogram_NeverReset.iterator().next().measurableValues();
    }

//    @Benchmark
//    public void scaleXHistogram_NeverReset_Count_Mean_Snapshot(State state) {
//        state.scaleXHistogram_NeverReset_Count_Mean.iterator().next().measurableValues();
//    }
//
    @Benchmark
    public void scaleXHistogram_ResetByChunks_Snapshot(State state) {
        state.scaleXHistogram_ResetByChunks.iterator().next().measurableValues();
    }
//
//    @Benchmark
//    public void scaleXHistogram_ResetByChunks_Count_Mean_Snapshot(State state) {
//        state.scaleXHistogram_ResetByChunks_Count_Mean.iterator().next().measurableValues();
//    }
//
    @Benchmark
    public void rollingHdrHistogram_ResetByChunks_Snapshot(State state) {
        state.rollingHdrHistogram_ResetByChunks.getSnapshot();
    }

    @SuppressWarnings("DuplicatedCode")
    public static class Driver {
        public static void main(String[] args) {
            Options options = new OptionsBuilder()
                .include(HistogramSnapshotBenchmark.class.getSimpleName())
                .warmupIterations(1)
                .measurementIterations(2)
                .measurementTime(TimeValue.seconds(15))
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
