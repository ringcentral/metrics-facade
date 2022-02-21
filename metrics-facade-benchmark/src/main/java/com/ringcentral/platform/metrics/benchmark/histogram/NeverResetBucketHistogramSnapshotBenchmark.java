package com.ringcentral.platform.metrics.benchmark.histogram;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.configs.HdrHistogramImplConfig;
import com.ringcentral.platform.metrics.scale.ScaleBuilder;
import com.ringcentral.platform.metrics.timer.Timer;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.configs.HdrHistogramImplConfigBuilder.hdrImpl;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.configs.OverflowBehavior.REDUCE_TO_HIGHEST_TRACKABLE;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfigBuilder.scaleImpl;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.scale.CompositeScaleBuilder.first;
import static com.ringcentral.platform.metrics.scale.LinearScaleBuilder.linear;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;
import static java.util.concurrent.TimeUnit.*;
import static org.openjdk.jmh.runner.options.TimeValue.seconds;

@BenchmarkMode({ Mode.Throughput, Mode.AverageTime })
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@SuppressWarnings("DuplicatedCode")
public class NeverResetBucketHistogramSnapshotBenchmark {

    @org.openjdk.jmh.annotations.State(Scope.Benchmark)
    public static class State {
        final MetricRegistry metricRegistry = new DefaultMetricRegistry();

        final Timer hdrHistogram_NeverReset = metricRegistry.timer(
            withName("hdrHistogram", "neverReset"),
            () -> withTimer()
                .measurables(
                    COUNT,
                    TOTAL_SUM,

                    MS_1_BUCKET,
                    MS_5_BUCKET,
                    MS_10_BUCKET,
                    MS_15_BUCKET,
                    MS_20_BUCKET,
                    MS_25_BUCKET,
                    MS_30_BUCKET,
                    MS_35_BUCKET,
                    MS_40_BUCKET,
                    MS_45_BUCKET,
                    MS_50_BUCKET,
                    MS_75_BUCKET,
                    MS_100_BUCKET,
                    MS_250_BUCKET,
                    MS_500_BUCKET,
                    MS_750_BUCKET,

                    SEC_1_BUCKET,
                    SEC_2p5_BUCKET,
                    SEC_5_BUCKET,
                    SEC_7p5_BUCKET,
                    SEC_10_BUCKET,
                    SEC_20_BUCKET,
                    SEC_30_BUCKET,

                    PERCENTILE_50)
                .with(hdrImpl()
                    .neverReset()
                    .highestTrackableValue(HOURS.toNanos(3), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Timer scaleHistogram_NeverReset = metricRegistry.timer(
            withName("scaleHistogram", "neverReset"),
            () -> withTimer()
                .measurables(
                    COUNT,
                    TOTAL_SUM,

                    MS_1_BUCKET,
                    MS_5_BUCKET,
                    MS_10_BUCKET,
                    MS_15_BUCKET,
                    MS_20_BUCKET,
                    MS_25_BUCKET,
                    MS_30_BUCKET,
                    MS_35_BUCKET,
                    MS_40_BUCKET,
                    MS_45_BUCKET,
                    MS_50_BUCKET,
                    MS_75_BUCKET,
                    MS_100_BUCKET,
                    MS_250_BUCKET,
                    MS_500_BUCKET,
                    MS_750_BUCKET,

                    SEC_1_BUCKET,
                    SEC_2p5_BUCKET,
                    SEC_5_BUCKET,
                    SEC_7p5_BUCKET,
                    SEC_10_BUCKET,
                    SEC_20_BUCKET,
                    SEC_30_BUCKET,

                    PERCENTILE_50)
                .with(scaleImpl()
                    .neverReset()
                    .with(scale())));

        final Timer neverResetBucketHistogram = metricRegistry.timer(
            withName("neverResetBucketHistogram"),
            () -> withTimer()
                .measurables(
                    COUNT,
                    TOTAL_SUM,

                    MS_1_BUCKET,
                    MS_5_BUCKET,
                    MS_10_BUCKET,
                    MS_15_BUCKET,
                    MS_20_BUCKET,
                    MS_25_BUCKET,
                    MS_30_BUCKET,
                    MS_35_BUCKET,
                    MS_40_BUCKET,
                    MS_45_BUCKET,
                    MS_50_BUCKET,
                    MS_75_BUCKET,
                    MS_100_BUCKET,
                    MS_250_BUCKET,
                    MS_500_BUCKET,
                    MS_750_BUCKET,

                    SEC_1_BUCKET,
                    SEC_2p5_BUCKET,
                    SEC_5_BUCKET,
                    SEC_7p5_BUCKET,
                    SEC_10_BUCKET,
                    SEC_20_BUCKET,
                    SEC_30_BUCKET)
                .with(hdrImpl()
                    .resetByChunks()
                    .highestTrackableValue(HOURS.toNanos(3), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

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

                hdrHistogram_NeverReset.update(duration);
                scaleHistogram_NeverReset.update(duration);
                neverResetBucketHistogram.update(duration);
            }
        }
    }

    static ScaleBuilder<?> scale() {
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

    @Benchmark
    public void hdrHistogram_NeverReset(State state) {
        state.hdrHistogram_NeverReset.iterator().next().measurableValues();
    }

    @Benchmark
    public void scaleHistogram_NeverReset(State state) {
        state.scaleHistogram_NeverReset.iterator().next().measurableValues();
    }

    @Benchmark
    public void neverResetBucketHistogram(State state) {
        state.neverResetBucketHistogram.iterator().next().measurableValues();
    }

    @SuppressWarnings("DuplicatedCode")
    public static class Driver {
        public static void main(String[] args) {
            Options options = new OptionsBuilder()
                .include(NeverResetBucketHistogramSnapshotBenchmark.class.getSimpleName())
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