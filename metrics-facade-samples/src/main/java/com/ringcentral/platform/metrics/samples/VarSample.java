package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.var.doubleVar.CachingDoubleVar;
import com.ringcentral.platform.metrics.var.longVar.LongVar;

import java.util.concurrent.atomic.AtomicLong;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.*;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.var.doubleVar.configs.builders.CachingDoubleVarConfigBuilder.withCachingDoubleVar;
import static com.ringcentral.platform.metrics.var.longVar.configs.builders.LongVarConfigBuilder.withLongVar;
import static java.util.concurrent.TimeUnit.SECONDS;

@SuppressWarnings("ALL")
public class VarSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        MetricRegistry registry = new DropwizardMetricRegistry();
        AtomicLong valueSupplier_1 = new AtomicLong();

        // Supported var types: ObjectVar, LongVar, DoubleVar, StringVar
        LongVar defaultConfigVar = registry.longVar(
            withName("var", "defaultConfig"),
            () -> valueSupplier_1.incrementAndGet());

        AtomicLong valueSupplier_2 = new AtomicLong();

        LongVar fullConfigVar = registry.longVar(
            withName("var", "fullConfig"),

            // options: Var.noTotal()
            () -> valueSupplier_2.incrementAndGet(),

            () -> withLongVar()
                // options: disable(), enabled(boolean)
                // default: enabled
                .enable()

                // default: no prefix dimension values
                .prefix(dimensionValues(SAMPLE.value("var")))

                .dimensions(SERVICE, SERVER, PORT)

                // the properties specific to the metrics implementation
                // default: no properties
                .put("key", "value"));

        AtomicLong valueSupplier_3 = new AtomicLong();

        fullConfigVar.register(
            () -> valueSupplier_3.incrementAndGet(),
            forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));

        AtomicLong valueSupplier_4 = new AtomicLong();

        fullConfigVar.register(
            () -> valueSupplier_4.incrementAndGet(),
            forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));

        fullConfigVar.deregister(dimensionValues(
            SERVICE.value("service_1"),
            SERVER.value("server_1_1"),
            PORT.value("111")));

        AtomicLong valueSupplier_5 = new AtomicLong();

        // Supported caching var types:
        //   CachingObjectVar,
        //   CachingLongVar,
        //   CachingDoubleVar,
        //   CachingStringVar
        CachingDoubleVar defaultConfigCachingVar = registry.cachingDoubleVar(
            withName("cachingVar", "defaultConfig"),
            () -> valueSupplier_5.incrementAndGet() + 0.5);

        AtomicLong valueSupplier_6 = new AtomicLong();

        CachingDoubleVar fullConfigCachingVar = registry.cachingDoubleVar(
            withName("cachingVar", "fullConfig"),

            // options: Var.noTotal()
            () -> valueSupplier_6.incrementAndGet() + 0.5,

            () -> withCachingDoubleVar()
                // options: disable(), enabled(boolean)
                // default: enabled
                .enable()

                // default: no prefix dimension values
                .prefix(dimensionValues(SAMPLE.value("var")))

                .dimensions(SERVICE, SERVER, PORT)

                // default: 30 SECONDS
                .ttl(10, SECONDS)

                // the properties specific to the metrics implementation
                // default: no properties
                .put("key", "value"));

        export(registry);
        hang();
    }
}
