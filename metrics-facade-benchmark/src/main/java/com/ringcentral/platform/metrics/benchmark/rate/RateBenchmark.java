package com.ringcentral.platform.metrics.benchmark.rate;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.rate.Rate;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.util.concurrent.TimeUnit;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.rate.Rate.*;
import static com.ringcentral.platform.metrics.rate.configs.builders.RateConfigBuilder.withRate;

@BenchmarkMode({ Mode.Throughput, Mode.AverageTime })
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class RateBenchmark {

    @org.openjdk.jmh.annotations.State(Scope.Benchmark)
    public static class State {
        // DW
        final MetricRegistry dwMetricRegistry = new DropwizardMetricRegistry();

        final Rate dwRate = dwMetricRegistry.rate(
            withName("dwRate"),
            () -> withRate().measurables(COUNT, MEAN_RATE, ONE_MINUTE_RATE, FIVE_MINUTES_RATE, FIFTEEN_MINUTES_RATE));

        // Default
        final MetricRegistry defaultMetricRegistry = new DefaultMetricRegistry();

        final Rate rate_allMeasurables = defaultMetricRegistry.rate(
            withName("rate", "allMeasurables"),
            () -> withRate().measurables(COUNT, MEAN_RATE, ONE_MINUTE_RATE, FIVE_MINUTES_RATE, FIFTEEN_MINUTES_RATE));

        final Rate rate_count = defaultMetricRegistry.rate(
            withName("rate", "count"),
            () -> withRate().measurables(COUNT));

        final Rate rate_count_oneMinuteRate = defaultMetricRegistry.rate(
            withName("rate", "count", "oneMinuteRate"),
            () -> withRate().measurables(COUNT, ONE_MINUTE_RATE));
    }

    @Benchmark
    @Group("dwRate")
    public void dwRate_mark(State rates) {
        rates.dwRate.mark();
    }

    @Benchmark
    @Group("rate_allMeasurables")
    public void rate_allMeasurables(State rates) {
        rates.rate_allMeasurables.mark();
    }

    @Benchmark
    @Group("rate_count")
    public void rate_count(State state) {
        state.rate_count.mark();
    }

    @Benchmark
    @Group("rate_count_oneMinuteRate")
    public void rate_count_oneMinuteRate(State state) {
        state.rate_count_oneMinuteRate.mark();
    }

    public static class SixteenThreads {
        public static void main(String[] args) {
            Options options = new OptionsBuilder()
                .include(RateBenchmark.class.getSimpleName())
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
