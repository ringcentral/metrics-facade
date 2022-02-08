package com.ringcentral.platform.metrics.benchmark.histogram;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.benchmark.utils.ValueIndex;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.x.XMetricRegistry;
import com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleBuilder;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

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
import static java.util.concurrent.TimeUnit.*;

@SuppressWarnings("DuplicatedCode")
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
                // MEAN,
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
                    .highestTrackableValue(HOURS.toNanos(3), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_ResetOnSnapshot_Count_Mean = xMetricRegistry.histogram(
            withName("hdrXHistogram", "resetOnSnapshot", "count", "mean"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    MEAN)
                .with(hdrImpl()
                    .resetOnSnapshot()
                    .highestTrackableValue(HOURS.toNanos(3), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_ResetByChunks = xMetricRegistry.histogram(
            withName("hdrXHistogram", "resetByChunks"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    MIN,
                    MAX,
                    // MEAN,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    PERCENTILE_90,
                    PERCENTILE_99)
                .with(hdrImpl()
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
                    MIN,
                    MAX,
                    MEAN,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    PERCENTILE_90,
                    PERCENTILE_99)
                .with(hdrImpl()
                    .significantDigits(3)
                    .highestTrackableValue(HOURS.toNanos(3), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        final Histogram hdrXHistogram_ResetByChunks_Count_Mean_3_Digits = xMetricRegistry.histogram(
            withName("hdrXHistogram", "resetByChunks", "count", "mean", "3digits"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    MEAN)
                .with(hdrImpl()
                    .significantDigits(3)
                    .highestTrackableValue(HOURS.toNanos(3), REDUCE_TO_HIGHEST_TRACKABLE)
                    .lowestDiscernibleValue(MILLISECONDS.toNanos(1))));

        // X - Scale
        final Histogram scaleXHistogram_NeverReset = xMetricRegistry.histogram(
            withName("scaleXHistogram", "neverReset"),
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
                .with(scaleImpl()
                    .neverReset()
                    .with(scale_1())));

        final Histogram scaleXHistogram_ResetOnSnapshot = xMetricRegistry.histogram(
            withName("scaleXHistogram", "resetOnSnapshot"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    MIN,
                    MAX,
                    // MEAN,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    PERCENTILE_90,
                    PERCENTILE_99)
                .with(scaleImpl()
                    .resetOnSnapshot()
                    .scale(scale_1())));

        final Histogram scaleXHistogram_ResetOnSnapshot_Count_Mean = xMetricRegistry.histogram(
            withName("scaleXHistogram", "resetOnSnapshot", "count", "mean"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    MEAN)
                .with(scaleImpl()
                    .resetOnSnapshot()
                    .scale(scale_1())));

        final Histogram scaleXHistogram_ResetByChunks = xMetricRegistry.histogram(
            withName("scaleXHistogram", "resetByChunks"),
            () -> withHistogram()
                .measurables(
                    COUNT,
                    MIN,
                    MAX,
                    // MEAN,
                    PERCENTILE_50,
                    PERCENTILE_75,
                    PERCENTILE_90,
                    PERCENTILE_99)
                .with(scaleImpl()
                    .resetByChunks()
                    .scale(scale_1())));

        // values
        final long[] values = makeValues_1();
        final ThreadLocal<ValueIndex> valueIndex = ThreadLocal.withInitial(() -> new ValueIndex(values.length));
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

    static long[] makeValues_1() {
        Random random = new Random(123);
        long[] values = new long[100000];

        for (int i = 0; i < values.length; i++) {
            long ms;

            if (i % 3 == 0) {
                ms = 200 + random.nextInt(75);
            } else if (i % 2 == 0) {
                ms = 20 + random.nextInt(25);
            } else {
                ms = 10 + random.nextInt(10);
            }

            if (i % 100 == 0) {
                ms += 1000;
            }

            if (i % 200 == 0) {
                ms += 1500;
            }

            if (i % 500 == 0) {
                ms += 5000;
            }

            values[i] = MILLISECONDS.toNanos(ms);
        }

        return values;
    }

    static long[] makeValues_2() {
        Random random = new Random(123);
        long[] values = new long[100000];

        for (int i = 0; i < values.length; i++) {
            if (i % 250 == 0) {
                values[i] = random.nextInt(10);
            } else {
                values[i] = 0;
            }
        }

        return values;
    }

    long value(State state) {
        return state.values[state.valueIndex.get().next()];
    }

//    @Benchmark
//    public void dwHistogram_Update(State state) {
//        state.dwHistogram.update(value(state));
//    }

//    @Benchmark
//    public void hdrXHistogram_NeverReset_Update(State state) {
//        state.hdrXHistogram_NeverReset.update(value(state));
//    }

//    @Benchmark
//    public void hdrXHistogram_NeverReset_Count_Mean_Update(State state) {
//        state.hdrXHistogram_NeverReset_Count_Mean.update(value(state));
//    }

//    @Benchmark
//    public void hdrXHistogram_ResetOnSnapshot_Update(State state) {
//        state.hdrXHistogram_ResetOnSnapshot.update(value(state));
//    }

//    @Benchmark
//    public void hdrXHistogram_ResetOnSnapshot_Count_Mean_Update(State state) {
//        state.hdrXHistogram_ResetOnSnapshot_Count_Mean.update(value(state));
//    }

    @Benchmark
    public void hdrXHistogram_ResetByChunks_Update(State state) {
        state.hdrXHistogram_ResetByChunks.update(value(state));
    }

//    @Benchmark
//    public void hdrXHistogram_ResetByChunks_Count_Mean_Update(State state) {
//        state.hdrXHistogram_ResetByChunks_Count_Mean.update(value(state));
//    }
//
//    @Benchmark
//    public void hdrXHistogram_ResetByChunks_3_Digits_Update(State state) {
//        state.hdrXHistogram_ResetByChunks_3_Digits.update(value(state));
//    }
//
//    @Benchmark
//    public void hdrXHistogram_ResetByChunks_Count_Mean_3_Digits_Update(State state) {
//        state.hdrXHistogram_ResetByChunks_Count_Mean_3_Digits.update(value(state));
//    }
//
//    @Benchmark
//    public void scaleXHistogram_NeverReset_Update(State state) {
//        state.scaleXHistogram_NeverReset.update(value(state));
//    }

//    @Benchmark
//    public void scaleXHistogram_ResetOnSnapshot_Update(State state) {
//        state.scaleXHistogram_ResetOnSnapshot.update(value(state));
//    }

//    @Benchmark
//    public void scaleXHistogram_ResetOnSnapshot_Count_Mean_Update(State state) {
//        state.scaleXHistogram_ResetOnSnapshot_Count_Mean.update(value(state));
//    }

    @Benchmark
    public void scaleXHistogram_ResetByChunks_Update(State state) {
        state.scaleXHistogram_ResetByChunks.update(value(state));
    }

    public static class Driver {
        public static void main(String[] args) {
            Options options = new OptionsBuilder()
                .include(HistogramUpdateBenchmark.class.getSimpleName())
                .warmupIterations(4)
                .measurementIterations(4)
                .measurementTime(TimeValue.seconds(45))
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
