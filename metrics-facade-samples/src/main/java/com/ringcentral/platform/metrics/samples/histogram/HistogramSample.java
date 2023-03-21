package com.ringcentral.platform.metrics.samples.histogram;

import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistryBuilder;
import com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.configs.HdrHistogramImplConfigBuilder;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.samples.AbstractSample;

import java.time.Duration;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.configs.HdrHistogramImplConfigBuilder.hdr;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.configs.OverflowBehavior.REDUCE_TO_HIGHEST_TRACKABLE;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramInstanceConfigBuilder.histogramInstance;
import static com.ringcentral.platform.metrics.labels.AllLabelValuesPredicate.labelValuesMatchingAll;
import static com.ringcentral.platform.metrics.labels.AnyLabelValuesPredicate.labelValuesMatchingAny;
import static com.ringcentral.platform.metrics.labels.LabelValues.forLabelValues;
import static com.ringcentral.platform.metrics.labels.LabelValues.labelValues;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.scale.SpecificScaleBuilder.points;
import static java.time.temporal.ChronoUnit.SECONDS;

@SuppressWarnings("ALL")
public class HistogramSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        DefaultMetricRegistry registry = new DefaultMetricRegistryBuilder()
            // You can also register custom metric implementations using the extendWith method:
            // registry.extendWith(new CountAndTotalSumScalingHistogramImplMaker());
            .withCustomMetricImplsFromPackages("com.ringcentral.platform.metrics.samples")
            .build();

        // Default config:
        //   no labels
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

                // default: no prefix label values
                .prefix(labelValues(SAMPLE.value("histogram")))

                // default: no labels
                .labels(SERVICE, SERVER, PORT)

                // options: noExclusions()
                // default: no exclusions
                .exclude(labelValuesMatchingAny(
                    SERVICE.mask("serv*2|serv*4*"),
                    SERVER.mask("server_5")))

                // default: unlimited
                .maxLabeledInstancesPerSlice(5)

                // options: notExpireLabeledInstances()
                // default: no expiration
                .expireLabeledInstanceAfter(25, SECONDS)

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

                /**
                 * options:
                 *   - hdr() == {@link HdrHistogramImplConfigBuilder#hdr()},
                 *   - scale() == {@link com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfigBuilder#scale()},
                 *   - custom impl, e.g. countAndTotalSum() == {@link CountAndTotalSumScalingHistogramConfigBuilder#countAndTotalSumScaling()}.
                 *     Custom impls must be registered: registry.extendWith(new CountAndTotalSumScalingHistogramConfigBuilder()).
                 * default: hdr()
                 */
                .impl(hdr()
                    .resetByChunks(6, Duration.ofMinutes(2))
                    .highestTrackableValue(1000, REDUCE_TO_HIGHEST_TRACKABLE)
                    .significantDigits(3)
                    .snapshotTtl(30, SECONDS))
                // .impl(countAndTotalSumScaling().factor(2)) // custom impl

                // the properties specific to the metrics implementation
                // default: no properties
                .put("key_1", "value_1_1")

                .allSlice()
                    // options: disable(), enabled(boolean)
                    // default: enabled
                    .enable()

                    // default: the metric's labels [ SERVICE, SERVER, PORT ]
                    .labels(SERVICE, SERVER)

                    // options: noMaxLabeledInstances()
                    // default: the metric's maxLabeledInstancesPerSlice = 5
                    .maxLabeledInstances(10)

                    // options: notExpireLabeledInstances()
                    // default: the metric's expireLabeledInstanceAfter = 25 SECONDS
                    .expireLabeledInstanceAfter(42, SECONDS)

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
                    .predicate(labelValuesMatchingAll(
                        SERVICE.mask("serv*_1*"),
                        SERVER.predicate(s -> s.equals("server_1_1"))))

                    // default: no labels
                    .labels(SERVICE)

                    // options: noMaxLabeledInstances()
                    // default: the metric's maxLabeledInstancesPerSlice = 5
                    .maxLabeledInstances(2)

                    // options: notExpireLabeledInstances()
                    // default: the metric's expireLabeledInstanceAfter = 25 SECONDS
                    .expireLabeledInstanceAfter(42, SECONDS)

                    // options: noMeasurables()
                    // default: the metric's measurables { COUNT, MEAN }
                    .measurables(
                        COUNT,
                        TOTAL_SUM,
                        MEAN,
                        PERCENTILE_50,
                        PERCENTILE_95,
                        MAX,
                        Buckets.of(points(0, 1, 24, 25, 30, 49, 50, 55)))

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

        fullConfigHistogram.update(20, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
        fullConfigHistogram.update(25, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("121")));
        fullConfigHistogram.update(30, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("121")));
        fullConfigHistogram.update(50, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("121")));
        fullConfigHistogram.update(55, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("121")));
        fullConfigHistogram.update(65, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("121")));
        fullConfigHistogram.update(70, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("121")));

        fullConfigHistogram.update(25, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));
        fullConfigHistogram.update(75, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_2"), PORT.value("221")));
        fullConfigHistogram.update(100, forLabelValues(SERVICE.value("service_3"), SERVER.value("server_3_1"), PORT.value("311")));

        export(registry);
        hang();
    }
}
