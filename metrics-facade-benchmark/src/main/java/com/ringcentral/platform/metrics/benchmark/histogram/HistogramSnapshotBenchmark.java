package com.ringcentral.platform.metrics.benchmark.histogram;

import com.github.rollingmetrics.histogram.OverflowResolver;
import com.github.rollingmetrics.histogram.hdr.RollingHdrHistogram;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.configs.HdrHistogramImplConfig;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.scale.ScaleBuilder;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.configs.HdrHistogramImplConfigBuilder.hdrImpl;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.configs.OverflowBehavior.REDUCE_TO_HIGHEST_TRACKABLE;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfigBuilder.scaleImpl;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.scale.CompositeScaleBuilder.first;
import static com.ringcentral.platform.metrics.scale.LinearScaleBuilder.linear;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.*;
import static org.openjdk.jmh.runner.options.TimeValue.seconds;

@BenchmarkMode({ Mode.Throughput, Mode.AverageTime })
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@SuppressWarnings("DuplicatedCode")
public class HistogramSnapshotBenchmark {

    @org.openjdk.jmh.annotations.State(Scope.Benchmark)
    public static class State {
        // DW
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

        // Default - HDR
        final MetricRegistry defaultMetricRegistry = new DefaultMetricRegistry();

        final Histogram hdrHistogram_NeverReset = defaultMetricRegistry.histogram(
            withName("hdrHistogram", "neverReset"),
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

        final Histogram hdrHistogram_ResetByChunks = defaultMetricRegistry.histogram(
            withName("hdrHistogram", "resetByChunks"),
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

        final Histogram hdrHistogram_ResetByChunks_3_Digits = defaultMetricRegistry.histogram(
            withName("hdrHistogram", "resetByChunks", "3digits"),
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

        // Default - Scale
        final Histogram scaleHistogram_NeverReset = defaultMetricRegistry.histogram(
            withName("scaleHistogram", "neverReset"),
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

        final Histogram scaleHistogram_ResetByChunks = defaultMetricRegistry.histogram(
            withName("scaleHistogram", "resetByChunks"),
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

        // Rolling - HDR
        final com.codahale.metrics.Histogram rollingHdrHistogram_ResetByChunks = new com.codahale.metrics.Histogram(
            new RollingHdrReservoir(
                RollingHdrHistogram.builder()
                    .resetReservoirPeriodicallyByChunks(
                        Duration.ofMillis(HdrHistogramImplConfig.DEFAULT.chunkResetPeriodMs() * HdrHistogramImplConfig.DEFAULT.chunkCount()),
                        HdrHistogramImplConfig.DEFAULT.chunkCount())
                    .withHighestTrackableValue(HOURS.toNanos(3), OverflowResolver.REDUCE_TO_HIGHEST_TRACKABLE)
                    .withLowestDiscernibleValue(MILLISECONDS.toNanos(1))
                    .build()));

        @Setup
        public void setup() throws InterruptedException {
            long allChunksResetPeriodMs = HdrHistogramImplConfig.DEFAULT.chunkResetPeriodMs() * HdrHistogramImplConfig.DEFAULT.chunkCount();
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

                // DW
                dwHistogram.update(duration);

                // Default - HDR
                hdrHistogram_NeverReset.update(duration);
                hdrHistogram_ResetByChunks.update(duration);
                hdrHistogram_ResetByChunks_3_Digits.update(duration);

                // Default - Scale
                scaleHistogram_NeverReset.update(duration);
                scaleHistogram_ResetByChunks.update(duration);

                // Rolling - HDR
                rollingHdrHistogram_ResetByChunks.update(duration);

                sleep(10L);
            }
        }
    }

    static ScaleBuilder<?> scale_1() {
        return
            // 500 ms
            first(linear().steps(5, MILLISECONDS, 100))
            // 1 sec
            .then(linear().steps(25, MILLISECONDS, 20))
            // 2 sec
            .then(linear().steps(100, MILLISECONDS, 10))
            // 10 sec
            .then(linear().steps(1, SECONDS, 8))
            // 30 sec
            .then(linear().steps(5, SECONDS, 4))
            // 1 min
            .then(linear().steps(10, SECONDS, 3))
            // 10 min
            .then(linear().steps(1, MINUTES, 9))
            // 3 h
            .then(linear().steps(10, MINUTES, 5 + 12).withInf());
    }

    static ScaleBuilder<?> scale_2() {
        return linear().from(1).steps(1, 9);
    }

    @Benchmark
    public void dwHistogram(State state) {
        state.dwHistogram.iterator().next().measurableValues();
    }

    @Benchmark
    public void hdrHistogram_NeverReset(State state) {
        state.hdrHistogram_NeverReset.iterator().next().measurableValues();
    }

    @Benchmark
    public void hdrHistogram_ResetByChunks(State state) {
        state.hdrHistogram_ResetByChunks.iterator().next().measurableValues();
    }

    @Benchmark
    public void hdrHistogram_ResetByChunks_3_Digits(State state) {
        state.hdrHistogram_ResetByChunks_3_Digits.iterator().next().measurableValues();
    }

    @Benchmark
    public void scaleHistogram_NeverReset(State state) {
        state.scaleHistogram_NeverReset.iterator().next().measurableValues();
    }

    @Benchmark
    public void scaleHistogram_ResetByChunks(State state) {
        state.scaleHistogram_ResetByChunks.iterator().next().measurableValues();
    }

    @Benchmark
    public void rollingHdrHistogram_ResetByChunks(State state) {
        state.rollingHdrHistogram_ResetByChunks.getSnapshot();
    }

    @SuppressWarnings("DuplicatedCode")
    public static class Driver {
        public static void main(String[] args) {
            Options options = new OptionsBuilder()
                .include(HistogramSnapshotBenchmark.class.getSimpleName())
                .warmupIterations(1)
                .measurementIterations(2)
                .measurementTime(seconds(15))
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
