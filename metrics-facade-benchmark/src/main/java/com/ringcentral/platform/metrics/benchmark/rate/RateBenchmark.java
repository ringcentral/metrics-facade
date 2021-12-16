package com.ringcentral.platform.metrics.benchmark.rate;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.x.XMetricRegistry;
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

    @State(Scope.Benchmark)
    public static class Rates {
        // dw
        final MetricRegistry dwMetricRegistry = new DropwizardMetricRegistry();

        final Rate dwRate = dwMetricRegistry.rate(
            withName("dwRate"),
            () -> withRate().measurables(COUNT, MEAN_RATE, ONE_MINUTE_RATE, FIVE_MINUTES_RATE, FIFTEEN_MINUTES_RATE));

        // x
        final MetricRegistry xMetricRegistry = new XMetricRegistry();

        final Rate xRate_allMeasurables = xMetricRegistry.rate(
            withName("xRate", "allMeasurables"),
            () -> withRate().measurables(COUNT, MEAN_RATE, ONE_MINUTE_RATE, FIVE_MINUTES_RATE, FIFTEEN_MINUTES_RATE));

        final Rate xRate_count = xMetricRegistry.rate(
            withName("xRate", "count"),
            () -> withRate().measurables(COUNT));

        final Rate xRate_count_oneMinuteRate = xMetricRegistry.rate(
            withName("xRate", "count", "oneMinuteRate"),
            () -> withRate().measurables(COUNT, ONE_MINUTE_RATE));
    }

    @Benchmark
    @Group("zdwRate")
    // @GroupThreads(16)
    public void zdwRate_mark(Rates rates) {
        rates.dwRate.mark();
    }

    @Benchmark
    @Group("xRate_allMeasurables")
    // @GroupThreads(16)
    public void xRate_allMeasurables(Rates rates) {
        rates.xRate_allMeasurables.mark();
    }

    @Benchmark
    @Group("xRate_count")
    // @GroupThreads(16)
    public void xRate_count(Rates rates) {
        rates.xRate_count.mark();
    }

    @Benchmark
    @Group("xRate_count_oneMinuteRate")
    // @GroupThreads(16)
    public void xRate_count_oneMinuteRate(Rates rates) {
        rates.xRate_count_oneMinuteRate.mark();
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
