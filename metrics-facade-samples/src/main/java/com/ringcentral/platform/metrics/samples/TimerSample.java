package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.timer.*;

import java.util.concurrent.TimeUnit;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.dimensions.AllMetricDimensionValuesPredicate.dimensionValuesMatchingAll;
import static com.ringcentral.platform.metrics.dimensions.AnyMetricDimensionValuesPredicate.dimensionValuesMatchingAny;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.*;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.rate.Rate.MEAN_RATE;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerInstanceConfigBuilder.timerInstance;
import static java.lang.Thread.sleep;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SuppressWarnings("ALL")
public class TimerSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new DefaultMetricRegistry();

        // Default config:
        //   no dimensions
        //   measurables: {
        //     Counter.COUNT,
        //
        //     Rate.MEAN_RATE,
        //     Rate.ONE_MINUTE_RATE,
        //     Rate.FIVE_MINUTES_RATE,
        //     Rate.FIFTEEN_MINUTES_RATE,
        //     Rate.RATE_UNIT,
        //
        //     Histogram.MIN,
        //     Histogram.MAX,
        //     Histogram.MEAN,
        //     Histogram.PERCENTILE_50,
        //     Histogram.PERCENTILE_90,
        //     Histogram.PERCENTILE_99,
        //
        //     Timer.DURATION_UNIT
        //  }
        Timer defaultConfigTimer = registry.timer(withName("timer", "defaultConfig"));

        for (int i = 1; i <= 100; ++i) {
            defaultConfigTimer.update(i, TimeUnit.SECONDS);
        }

        // Full config
        Timer fullConfigTimer = registry.timer(
            withName("timer", "fullConfig"),
            () -> withTimer()
                // options: disable(), enabled(boolean)
                // default: enabled
                .enable()

                // default: no prefix dimension values
                .prefix(dimensionValues(SAMPLE.value("timer")))

                // default: no dimensions
                .dimensions(SERVICE, SERVER, PORT)

                // options: noExclusions()
                // default: no exclusions
                .exclude(dimensionValuesMatchingAny(
                    SERVICE.mask("serv*2|serv*4*"),
                    SERVER.mask("server_5")))

                // default: unlimited
                .maxDimensionalInstancesPerSlice(5)

                // options: notExpireDimensionalInstances()
                // default: no expiration
                .expireDimensionalInstanceAfter(25, SECONDS)

                // options: noMeasurables()
                // default: {
                //   Counter.COUNT,
                //
                //   Rate.MEAN_RATE,
                //   Rate.ONE_MINUTE_RATE,
                //   Rate.FIVE_MINUTES_RATE,
                //   Rate.FIFTEEN_MINUTES_RATE,
                //   Rate.RATE_UNIT,
                //
                //   Histogram.MIN,
                //   Histogram.MAX,
                //   Histogram.MEAN,
                //   Histogram.PERCENTILE_50,
                //   Histogram.PERCENTILE_90,
                //   Histogram.PERCENTILE_99,
                //
                //   Timer.DURATION_UNIT
                // }
                .measurables(COUNT, MEAN_RATE, MAX, MEAN)

                // the properties specific to the metrics implementation
                // default: no properties
                .put("key_1", "value_1_1")

                .allSlice()
                    // options: disable(), enabled(boolean)
                    // default: enabled
                    .enable()

                    // default: the metric's dimensions [ SERVICE, SERVER, PORT ]
                    .dimensions(SERVICE, SERVER)

                    // options: noMaxDimensionalInstances()
                    // default: the metric's maxDimensionalInstancesPerSlice = 5
                    .maxDimensionalInstances(10)

                    // options: notExpireDimensionalInstances()
                    // default: the metric's expireDimensionalInstanceAfter = 25 SECONDS
                    .expireDimensionalInstanceAfter(42, SECONDS)

                    // options: noMeasurables()
                    // default: the metric's measurables { COUNT, MEAN_RATE, MAX, MEAN }
                    .measurables(COUNT, MEAN_RATE, MAX, MEAN, PERCENTILE_50)

                    // options: disableTotal(), noTotal(), totalEnabled(boolean)
                    // default: enabled
                    .enableTotal()

                    // options: disableLevels(), noLevels(), levelsEnabled(boolean)
                    // default: enabled
                    .enableLevels()

                    // the properties specific to the metrics implementation
                    // default: no properties (no overrides)
                    .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_1"
                    .put("key_2", "value_2_1")

                    .total(timerInstance()
                        // default: empty name suffix
                        .name("total")

                        // options: noMeasurables()
                        // default: the slice's measurables { COUNT, MEAN_RATE, MAX, MEAN, PERCENTILE_50 }
                        .measurables(COUNT, MEAN_RATE, MAX, MEAN, PERCENTILE_50, PERCENTILE_90)

                        // the properties specific to the metrics implementation
                        // default: no properties (no overrides)
                        .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_2"
                        .put("key_2", "value_2_2")) // overrides "key_2" -> "value_2_1"
                .slice("byService")
                    // options: disable(), enabled(boolean)
                    // default: enabled
                    .enable()

                    // default: no predicate
                    .predicate(dimensionValuesMatchingAll(
                        SERVICE.mask("serv*_1*"),
                        SERVER.predicate(s -> s.equals("server_1_1"))))

                    // default: no dimensions
                    .dimensions(SERVICE)

                    // options: noMaxDimensionalInstances()
                    // default: the metric's maxDimensionalInstancesPerSlice = 5
                    .maxDimensionalInstances(2)

                    // options: notExpireDimensionalInstances()
                    // default: the metric's expireDimensionalInstanceAfter = 25 SECONDS
                    .expireDimensionalInstanceAfter(42, SECONDS)

                    // options: noMeasurables()
                    // default: the metric's measurables { COUNT, MEAN_RATE, MAX, MEAN }
                    .measurables(
                        COUNT,
                        MEAN_RATE,
                        MAX,
                        MEAN,
                        PERCENTILE_75,
                        MS_10_BUCKET,
                        MS_30_BUCKET,
                        MS_50_BUCKET,
                        MS_75_BUCKET,
                        MS_100_BUCKET,
                        MS_250_BUCKET)

                    // options: disableTotal(), noTotal(), totalEnabled(boolean)
                    // default: enabled
                    .enableTotal()

                    // options: disableLevels(), noLevels(), levelsEnabled(boolean)
                    // default: disabled
                    .enableLevels()

                    // the properties specific to the metrics implementation
                    // default: no properties (no overrides)
                    .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_1"
                    .put("key_2", "value_2_1")

                    .total(timerInstance()
                        // default: empty name suffix
                        .name("total")

                        // options: noMeasurables()
                        // default: the slice's measurables { COUNT, MEAN_RATE, MAX, MEAN, PERCENTILE_75 }
                        .measurables(COUNT, MEAN_RATE, MIN, MAX, MEAN, PERCENTILE_75, PERCENTILE_90)

                        // the properties specific to the metrics implementation
                        // default: no properties (no overrides)
                        .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_2"
                        .put("key_2", "value_2_2")) // overrides "key_2" -> "value_2_1"
        );

        // service_1/server_1_1
        fullConfigTimer.update(
            25, MILLISECONDS,
            forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));

        fullConfigTimer.update(
            50, MILLISECONDS,
            forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));

        fullConfigTimer.update(
            75, MILLISECONDS,
            forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));

        fullConfigTimer.update(
            100, MILLISECONDS,
            forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));

        fullConfigTimer.update(
            50, MILLISECONDS,
            forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));

        // other services/servers
        Stopwatch stopwatch = fullConfigTimer.stopwatch(forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));
        sleep(25);
        stopwatch.stop();

        stopwatch = fullConfigTimer.stopwatch();
        sleep(75);
        stopwatch.stop(forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_2"), PORT.value("221")));

        stopwatch = fullConfigTimer.stopwatch();
        sleep(100);
        stopwatch.stop(forDimensionValues(SERVICE.value("service_3"), SERVER.value("server_3_1"), PORT.value("311")));

        export(registry);
        hang();
    }
}
