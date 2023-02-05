package com.ringcentral.platform.metrics.samples.rate;

import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistryBuilder;
import com.ringcentral.platform.metrics.defaultImpl.rate.ema.configs.ExpMovingAverageRateImplConfigBuilder;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.samples.AbstractSample;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.defaultImpl.rate.ema.configs.ExpMovingAverageRateImplConfigBuilder.expMovingAverage;
import static com.ringcentral.platform.metrics.labels.AllLabelValuesPredicate.labelValuesMatchingAll;
import static com.ringcentral.platform.metrics.labels.AnyLabelValuesPredicate.labelValuesMatchingAny;
import static com.ringcentral.platform.metrics.labels.LabelValues.forLabelValues;
import static com.ringcentral.platform.metrics.labels.LabelValues.labelValues;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.rate.Rate.*;
import static com.ringcentral.platform.metrics.rate.configs.builders.RateConfigBuilder.withRate;
import static com.ringcentral.platform.metrics.rate.configs.builders.RateInstanceConfigBuilder.rateInstance;
import static java.time.temporal.ChronoUnit.SECONDS;

@SuppressWarnings("ALL")
public class RateSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        DefaultMetricRegistry registry = new DefaultMetricRegistryBuilder()
            // You can also register custom metric implementations using the extendWith method:
            // registry.extendWith(new CountScalingRateImplMaker());
            .withCustomMetricImplsFromPackages("com.ringcentral.platform.metrics.samples")
            .build();

        // Default config:
        //   no labels
        //   measurables: {
        //     COUNT,
        //     MEAN_RATE,
        //     ONE_MINUTE_RATE,
        //     FIVE_MINUTES_RATE,
        //     FIFTEEN_MINUTES_RATE,
        //     RATE_UNIT
        //   }
        Rate defaultConfigRate = registry.rate(withName("rate", "defaultConfig"));

        defaultConfigRate.mark();
        defaultConfigRate.mark(2);

        // Full config
        Rate fullConfigRate = registry.rate(
            withName("rate", "fullConfig"),
            () -> withRate()
                // options: disable(), enabled(boolean)
                // default: enabled
                .enable()

                // default: no prefix label values
                .prefix(labelValues(SAMPLE.value("rate")))

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
                //   MEAN_RATE,
                //   ONE_MINUTE_RATE,
                //   FIVE_MINUTES_RATE,
                //   FIFTEEN_MINUTES_RATE,
                //   RATE_UNIT
                // }
                .measurables(COUNT)

                /**
                 * options:
                 *   - expMovingAverage() == {@link ExpMovingAverageRateImplConfigBuilder#expMovingAverage()},
                 *   - custom impl, e.g. countAndMean() == {@link CountScalingRateConfigBuilder#countScaling()}.
                 *     Custom impls must be registered: registry.extendWith(new CountScalingRateConfigBuilder()).
                 * default: expMovingAverage()
                 */
                .impl(expMovingAverage())
                // .impl(countScaling().factor(2)) // custom impl

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
                    // default: the metric's measurables { COUNT }
                    .measurables(COUNT, MEAN_RATE)

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

                    .total(rateInstance()
                        // default: empty name suffix
                        .name("total")

                        // options: noMeasurables()
                        // default: the slice's measurables { COUNT, MEAN_RATE }
                        .measurables(
                            COUNT,
                            MEAN_RATE, ONE_MINUTE_RATE, FIVE_MINUTES_RATE,
                            RATE_UNIT)

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
                    // default: the metric's measurables { COUNT }
                    .measurables(COUNT, ONE_MINUTE_RATE)

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

                    .total(rateInstance()
                        // default: empty name suffix
                        .name("total")

                        // options: noMeasurables()
                        // default: the slice's measurables { COUNT, ONE_MINUTE_RATE }
                        .measurables(
                            COUNT,
                            MEAN_RATE, ONE_MINUTE_RATE, FIVE_MINUTES_RATE,
                            RATE_UNIT)

                        // the properties specific to the metrics implementation
                        // default: no properties (no overrides)
                        .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_2"
                        .put("key_2", "value_2_2")) // overrides "key_2" -> "value_2_1"
        );

        fullConfigRate.mark(forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
        fullConfigRate.mark(forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));
        fullConfigRate.mark(8, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));
        fullConfigRate.mark(forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_2"), PORT.value("221")));
        fullConfigRate.mark(2, forLabelValues(SERVICE.value("service_3"), SERVER.value("server_3_1"), PORT.value("311")));

        export(registry);
        hang();
    }
}
