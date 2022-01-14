package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.x.XMetricRegistry;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.dimensions.AllMetricDimensionValuesPredicate.dimensionValuesMatchingAll;
import static com.ringcentral.platform.metrics.dimensions.AnyMetricDimensionValuesPredicate.dimensionValuesMatchingAny;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.*;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.rate.Rate.*;
import static com.ringcentral.platform.metrics.rate.configs.builders.RateConfigBuilder.withRate;
import static com.ringcentral.platform.metrics.rate.configs.builders.RateInstanceConfigBuilder.rateInstance;
import static java.time.temporal.ChronoUnit.SECONDS;

@SuppressWarnings("ALL")
public class RateSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new XMetricRegistry();

        // Default config:
        //   no dimensions
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

                // default: no prefix dimension values
                .prefix(dimensionValues(SAMPLE.value("rate")))

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
                //   MEAN_RATE,
                //   ONE_MINUTE_RATE,
                //   FIVE_MINUTES_RATE,
                //   FIFTEEN_MINUTES_RATE,
                //   RATE_UNIT
                // }
                .measurables(COUNT)

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

        fullConfigRate.mark(forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
        fullConfigRate.mark(forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));
        fullConfigRate.mark(8, forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));
        fullConfigRate.mark(forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_2"), PORT.value("221")));
        fullConfigRate.mark(2, forDimensionValues(SERVICE.value("service_3"), SERVER.value("server_3_1"), PORT.value("311")));

        export(registry);
        hang();
    }
}
