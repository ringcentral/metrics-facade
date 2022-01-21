package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.x.XMetricRegistry;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.dimensions.AllMetricDimensionValuesPredicate.dimensionValuesMatchingAll;
import static com.ringcentral.platform.metrics.dimensions.AnyMetricDimensionValuesPredicate.dimensionValuesMatchingAny;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.*;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramInstanceConfigBuilder.histogramInstance;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static java.time.temporal.ChronoUnit.SECONDS;

@SuppressWarnings("ALL")
public class HistogramSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new XMetricRegistry();

        // Default config:
        //   no dimensions
        //   measurables: {
        //     COUNT,
        //     MIN,
        //     MAX,
        //     MEAN,
        //     PERCENTILE_50,
        //     PERCENTILE_90,
        //     PERCENTILE_99
        //   }
        Histogram defaultConfigHistogram = registry.histogram(withName("histogram", "defaultConfig"));

        for (int i = 1; i <= 100; ++i) {
            defaultConfigHistogram.update(i);
        }

        // Full config
        Histogram fullConfigHistogram = registry.histogram(
            withName("histogram", "fullConfig"),
            () -> withHistogram()
                // options: disable(), enabled(boolean)
                // default: enabled
                .enable()

                // default: no prefix dimension values
                .prefix(dimensionValues(SAMPLE.value("histogram")))

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
                //   COUNT,
                //   MIN,
                //   MAX,
                //   MEAN,
                //   PERCENTILE_50,
                //   PERCENTILE_90,
                //   PERCENTILE_99
                // }
                .measurables(COUNT, MEAN)

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
                    // default: the metric's measurables { COUNT, MEAN }
                    .measurables(COUNT, MEAN, MAX)

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

                    .total(histogramInstance()
                        // default: empty name suffix
                        .name("total")

                        // options: noMeasurables()
                        // default: the slice's measurables { COUNT, MEAN, MAX }
                        .measurables(COUNT, MEAN, PERCENTILE_95, MAX, Bucket.of(1), Bucket.of(2))

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
                    // default: the metric's measurables { COUNT, MEAN }
                    .measurables(
                        COUNT,
                        TOTAL_SUM,
                        MEAN,
                        PERCENTILE_50,
                        PERCENTILE_95,
                        MAX,
                        Bucket.of(0),
                        Bucket.of(1),
                        Bucket.of(24),
                        Bucket.of(25),
                        Bucket.of(30),
                        Bucket.of(49),
                        Bucket.of(50),
                        Bucket.of(55))

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

                    .total(histogramInstance()
                        // default: empty name suffix
                        .name("total")

                        // options: noMeasurables()
                        // default: the slice's measurables { COUNT, MEAN, PERCENTILE_50, PERCENTILE_95, MAX }
                        .measurables(COUNT, MIN, MEAN, MAX)

                        // the properties specific to the metrics implementation
                        // default: no properties (no overrides)
                        .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_2"
                        .put("key_2", "value_2_2")) // overrides "key_2" -> "value_2_1"
        );

        fullConfigHistogram.update(20, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
        fullConfigHistogram.update(25, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("121")));
        fullConfigHistogram.update(30, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("121")));
        fullConfigHistogram.update(50, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("121")));
        fullConfigHistogram.update(55, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("121")));
        fullConfigHistogram.update(65, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("121")));
        fullConfigHistogram.update(70, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("121")));

        fullConfigHistogram.update(25, forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));
        fullConfigHistogram.update(75, forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_2"), PORT.value("221")));
        fullConfigHistogram.update(100, forDimensionValues(SERVICE.value("service_3"), SERVER.value("server_3_1"), PORT.value("311")));

        export(registry);
        hang();
    }
}
