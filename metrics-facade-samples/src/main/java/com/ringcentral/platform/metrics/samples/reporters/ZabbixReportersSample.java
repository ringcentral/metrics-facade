package com.ringcentral.platform.metrics.samples.reporters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.histogram.*;
import com.ringcentral.platform.metrics.reporters.zabbix.*;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixLldMetricsReporter.*;
import com.ringcentral.platform.metrics.samples.*;
import com.ringcentral.platform.metrics.scale.ScaleBuilder;

import java.util.List;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.forDimensionValues;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.*;
import static com.ringcentral.platform.metrics.predicates.DefaultMetricInstancePredicate.forMetricInstancesMatching;
import static com.ringcentral.platform.metrics.samples.DefaultInstanceSampleSpec.instanceSampleSpec;
import static com.ringcentral.platform.metrics.samples.DefaultSampleSpec.sampleSpec;
import static com.ringcentral.platform.metrics.scale.CompositeScaleBuilder.first;
import static com.ringcentral.platform.metrics.scale.LinearScaleBuilder.linear;
import static java.util.concurrent.TimeUnit.*;

@SuppressWarnings("ALL")
public class ZabbixReportersSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new DefaultMetricRegistry();
        DefaultInstanceSampleSpecModsProvider miSampleSpecModsProvider = new DefaultInstanceSampleSpecModsProvider();

        miSampleSpecModsProvider.addMod(
            forMetricInstancesMatching(
                nameMask("histogram.**"),
                instance -> "service_2".equals(instance.valueOf(SERVICE))),
            (metric, instance) -> instanceSampleSpec().disable());

        miSampleSpecModsProvider.addMod(
            forMetricWithName("histogram"),
            (metric, instance) -> instanceSampleSpec().name(instance.name().withNewPart("test")));

        DefaultSampleSpecModsProvider sampleSpecModsProvider = new DefaultSampleSpecModsProvider();

        sampleSpecModsProvider.addMod(
            forMetricInstancesMatching(
                nameMask("histogram.**"),
                instance -> instance instanceof HistogramInstance),
            (instanceSampleSpec, instance, measurableValues, measurable) ->
                measurable instanceof Max ? sampleSpec().disable() : sampleSpec());

        DefaultInstanceSamplesProvider miSamplesProvider = new DefaultInstanceSamplesProvider(
            miSampleSpecModsProvider,
            sampleSpecModsProvider,
            new DefaultSampleSpecProvider(CustomMeasurableNameProvider.INSTANCE),
            registry);

        ZabbixMetricsJsonExporter exporter = new ZabbixMetricsJsonExporter(miSamplesProvider);

        // LLD
        ZGroupMBeansExporter zGroupMBeansExporter = new ZGroupMBeansExporter(
            "zabbixReportersSample.zabbix.lld:type=",
            DefaultZGroupJsonMapper.INSTANCE,
            "JsonData");

        zGroupMBeansExporter.ensureGroup("server");
        ZabbixLldMetricsReporter lldReporter = new ZabbixLldMetricsReporter(zGroupMBeansExporter);

        lldReporter.addRules(
            forMetricInstancesMatching(nameMask("histogram.**")),
            new Rule(
                "service",
                List.of(new RuleItem(SERVICE, "service"), new RuleItem(SERVER, "server"))));

        lldReporter.addRules(
            forMetricInstancesMatching(nameMask("histogram.**")),
            new Rule(
                "server",
                List.of(new RuleItem(i -> i.valueOf(SERVICE) + "/" + i.valueOf(SERVER), "server"))));

        registry.addListener(lldReporter);

        // Metrics
        Histogram h = registry.histogram(
            withName("histogram"),
            () -> withHistogram()
                .dimensions(SERVICE, SERVER, PORT)
                .measurables(COUNT, MAX, MEAN, Buckets.of(scale())));

        h.update(1, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
        h.update(2, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));
        h.update(3, forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));

        System.out.println("Output:");
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(exporter.exportMetrics()));
        System.out.println("**********\n\n");

        hang();
    }

    static ScaleBuilder<?> scale() {
        return
            // 500 ms
            first(linear().steps(5, MILLISECONDS, 100))
            // 1 sec
            .then(linear().steps(25, MILLISECONDS, 20))
            // 2 sec
            .then(linear().steps(100, MILLISECONDS, 10))
            // 10 sec
            .then(linear().steps(1, SECONDS, 8))
            // 30 sec
            .then(linear().steps(5, SECONDS, 4))
            // 1 min
            .then(linear().steps(10, SECONDS, 3))
            // 10 min
            .then(linear().steps(1, MINUTES, 9))
            // 3 h
            .then(linear().steps(10, MINUTES, 5 + 12).withInf());
    }
}
