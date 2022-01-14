package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.x.XMetricRegistry;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.withCounter;
import static com.ringcentral.platform.metrics.counter.configs.builders.CounterInstanceConfigBuilder.counterInstance;
import static com.ringcentral.platform.metrics.dimensions.AllMetricDimensionValuesPredicate.dimensionValuesMatchingAll;
import static com.ringcentral.platform.metrics.dimensions.AnyMetricDimensionValuesPredicate.dimensionValuesMatchingAny;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.*;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static java.time.temporal.ChronoUnit.SECONDS;

@SuppressWarnings("ALL")
public class CounterSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new XMetricRegistry();

        // Default config:
        //   no dimensions
        //   measurables: { COUNT }
        Counter defaultConfigCounter = registry.counter(withName("counter", "defaultConfig"));

        defaultConfigCounter.inc();
        defaultConfigCounter.inc(2);
        defaultConfigCounter.dec();

        // Full config
        Counter fullConfigCounter = registry.counter(
            withName("counter", "fullConfig"),
            () -> withCounter()
                // options: disable(), enabled(boolean)
                // default: enabled
                .enable()

                // default: no prefix dimension values
                .prefix(dimensionValues(SAMPLE.value("counter")))

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
                // default: { COUNT }
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
                    .measurables(COUNT)

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

                    .total(counterInstance()
                        // default: empty name suffix
                        .name("total")

                        // options: noMeasurables()
                        // default: the slice's measurables { COUNT }
                        .measurables(COUNT)

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
                    .measurables(COUNT)

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

                    .total(counterInstance()
                        // default: empty name suffix
                        .name("total")

                        // options: noMeasurables()
                        // default: the slice's measurables { COUNT }
                        .measurables(COUNT)

                        // the properties specific to the metrics implementation
                        // default: no properties (no overrides)
                        .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_2"
                        .put("key_2", "value_2_2")) // overrides "key_2" -> "value_2_1"
        );

        fullConfigCounter.inc(forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
        fullConfigCounter.inc(forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));
        fullConfigCounter.inc(8, forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));
        fullConfigCounter.inc(forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_2"), PORT.value("221")));
        fullConfigCounter.dec(2, forDimensionValues(SERVICE.value("service_3"), SERVER.value("server_3_1"), PORT.value("311")));

        export(registry);
        hang();
    }
}
