package com.ringcentral.platform.metrics.samples.reporters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.reporters.zabbix.DefaultZGroupJsonMapper;
import com.ringcentral.platform.metrics.reporters.zabbix.ZGroupMBeansExporter;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixLldMetricsReporter;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixLldMetricsReporter.Rule;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixLldMetricsReporter.RuleItem;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixMetricsJsonExporter;
import com.ringcentral.platform.metrics.samples.*;
import com.ringcentral.platform.metrics.scale.ScaleBuilder;
import com.ringcentral.platform.metrics.timer.Timer;
import com.ringcentral.platform.metrics.timer.TimerInstance;

import java.util.List;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.forDimensionValues;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.forMetricWithName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.nameMask;
import static com.ringcentral.platform.metrics.predicates.DefaultMetricInstancePredicate.forMetricInstancesMatching;
import static com.ringcentral.platform.metrics.samples.DefaultInstanceSampleSpec.instanceSampleSpec;
import static com.ringcentral.platform.metrics.samples.DefaultSampleSpec.sampleSpec;
import static com.ringcentral.platform.metrics.scale.CompositeScaleBuilder.first;
import static com.ringcentral.platform.metrics.scale.LinearScaleBuilder.linear;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SuppressWarnings("ALL")
public class ZabbixReportersSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new DefaultMetricRegistry();
        DefaultInstanceSampleSpecModsProvider miSampleSpecModsProvider = new DefaultInstanceSampleSpecModsProvider();

        miSampleSpecModsProvider.addMod(
            forMetricInstancesMatching(
                nameMask("timer.**"),
                instance -> "service_2".equals(instance.valueOf(SERVICE))),
            (metric, instance, currSpec) -> instanceSampleSpec().disable());

        miSampleSpecModsProvider.addMod(
            forMetricWithName("timer"),
            (metric, instance, currSpec) -> instanceSampleSpec().name(instance.name().withNewPart("test")));

        DefaultSampleSpecModsProvider sampleSpecModsProvider = new DefaultSampleSpecModsProvider();

        sampleSpecModsProvider.addMod(
            forMetricInstancesMatching(
                nameMask("timer.**"),
                instance -> instance instanceof TimerInstance),
            (instanceSampleSpec, instance, measurableValues, measurable, currSpec) ->
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
            forMetricInstancesMatching(nameMask("timer.**")),
            new Rule(
                "service",
                List.of(new RuleItem(SERVICE, "service"), new RuleItem(SERVER, "server"))));

        lldReporter.addRules(
            forMetricInstancesMatching(nameMask("timer.**")),
            new Rule(
                "server",
                List.of(new RuleItem(i -> i.valueOf(SERVICE) + "/" + i.valueOf(SERVER), "server"))));

        registry.addListener(lldReporter);

        // Metrics
        Timer t = registry.timer(
            withName("timer"),
            () -> withTimer()
                .dimensions(SERVICE, SERVER, PORT)
                .measurables(COUNT, MAX, MEAN, Buckets.of(scale())));

        t.update(1, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
        t.update(2, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));
        t.update(3, forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));

        System.out.println("Output:");
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(exporter.exportMetrics()));
        System.out.println("**********\n\n");

        hang();
    }

    static ScaleBuilder<?> scale() {
        return
            // 100 ms
            first(linear().steps(25, MILLISECONDS, 4))
            // 500 ms
            .then(linear().steps(100, MILLISECONDS, 4).withInf());
    }
}
