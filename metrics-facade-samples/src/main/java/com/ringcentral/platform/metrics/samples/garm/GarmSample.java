package com.ringcentral.platform.metrics.samples.garm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.infoProviders.MaskTreeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.producers.SystemMetricsProducer;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.reporters.jmx.*;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import com.ringcentral.platform.metrics.reporters.telegraf.TelegrafMetricsJsonExporter;
import com.ringcentral.platform.metrics.reporters.zabbix.*;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixLldMetricsReporter.*;
import com.ringcentral.platform.metrics.samples.*;
import com.ringcentral.platform.metrics.samples.prometheus.*;
import com.ringcentral.platform.metrics.timer.Timer;
import com.ringcentral.platform.metrics.var.longVar.LongVar;
import com.ringcentral.platform.metrics.var.objectVar.ObjectVar;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.ringcentral.platform.metrics.MetricModBuilder.modifying;
import static com.ringcentral.platform.metrics.PrefixDimensionValuesMetricKey.withKey;
import static com.ringcentral.platform.metrics.configs.builders.BaseMeterConfigBuilder.withMeter;
import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.*;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.names.MetricName.*;
import static com.ringcentral.platform.metrics.names.MetricNameMask.*;
import static com.ringcentral.platform.metrics.predicates.CompositeMetricNamedPredicateBuilder.forMetrics;
import static com.ringcentral.platform.metrics.predicates.DefaultMetricInstancePredicate.forMetricInstancesMatching;
import static com.ringcentral.platform.metrics.rate.Rate.ONE_MINUTE_RATE;
import static com.ringcentral.platform.metrics.rate.configs.builders.RateConfigBuilder.withRate;
import static com.ringcentral.platform.metrics.reporters.jmx.MBeanSpec.mBeanSpec;
import static com.ringcentral.platform.metrics.samples.DefaultInstanceSampleSpec.instanceSampleSpec;
import static com.ringcentral.platform.metrics.samples.DefaultSampleSpec.sampleSpec;
import static com.ringcentral.platform.metrics.samples.SampleTypes.DELTA;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;
import static com.ringcentral.platform.metrics.var.Var.noTotal;
import static com.ringcentral.platform.metrics.var.longVar.configs.builders.LongVarConfigBuilder.longVar;
import static java.lang.Thread.sleep;
import static java.time.temporal.ChronoUnit.SECONDS;

@SuppressWarnings("ALL")
public class GarmSample extends AbstractSample {

    static final MetricDimension METHOD = new MetricDimension("method");

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new DefaultMetricRegistry();

        // Defaults
        registry.postConfigure(allMetrics(), modifying()
            .meter(withMeter().allSlice().noLevels())
            .rate(withRate().measurables(COUNT, ONE_MINUTE_RATE))
            .histogram(withHistogram().measurables(COUNT, MAX, MEAN, PERCENTILE_95))
            .timer(withTimer().measurables(COUNT, ONE_MINUTE_RATE, MAX, MEAN, PERCENTILE_95)));

        // System metrics
        new SystemMetricsProducer().produceMetrics(registry);

        // Prometheus
        InstanceSamplesProvider<PrometheusSample, PrometheusInstanceSample> prometheusInstanceSamplesProvider =
            new PrometheusInstanceSamplesProvider(registry);

        PrometheusMetricsExporter prometheusMetricsExporter = new PrometheusMetricsExporter(prometheusInstanceSamplesProvider);
        PrometheusHttpServer prometheusHttpServer = new PrometheusHttpServer(PROMETHEUS_PORT, prometheusMetricsExporter);

        // Rewriting names
        DefaultInstanceSampleSpecModsProvider instanceSampleSpecModsProvider = new DefaultInstanceSampleSpecModsProvider();

        instanceSampleSpecModsProvider.addMod(
            forMetricInstancesMatching(nameMask("allEndpoints.clientRequestProcessingTime"), instance -> instance.hasDimension(METHOD)),
            (metric, instance) -> instanceSampleSpec()
                .name(instance.name().withNewPart(instance.valueOf(METHOD), 1))
                .dimensionValues(instance.dimensionValuesWithout(METHOD)));

        instanceSampleSpecModsProvider.addMod(
            forMetricsWithNamePrefix("longVar"),
            (metric, instance) -> instanceSampleSpec().noMeasurableName());

        // Move certain counters to DELTA bucket
        DefaultSampleSpecModsProvider sampleSpecModsProvider = new DefaultSampleSpecModsProvider();

        sampleSpecModsProvider.addMod(
            forMetrics()
                .including(metricWithName("allEndpoints.activeClientConnectionsCount"))
                .including(metricWithName("allEndpoints.activeRequestsCount")),
            (instanceSampleSpec, instance, measurableValues, measurable) -> sampleSpec().type(DELTA));

        sampleSpecModsProvider.addMod(
            forMetricsWithNamePrefix("longVar"),
            (instanceSampleSpec, instance, measurableValues, measurable) -> sampleSpec().noMeasurableName());

        // Metric instance samples provider
        DefaultInstanceSamplesProvider instanceSamplesProvider = new DefaultInstanceSamplesProvider(
            instanceSampleSpecModsProvider,
            sampleSpecModsProvider,
            new DefaultSampleSpecProvider(GarmMeasurableNameProvider.INSTANCE),
            registry);

        // JSON exporters
        ZabbixMetricsJsonExporter zabbixMetricsJsonExporter = new ZabbixMetricsJsonExporter(instanceSamplesProvider);
        TelegrafMetricsJsonExporter telegrafMetricsJsonExporter = new TelegrafMetricsJsonExporter(true, instanceSamplesProvider);

        // Zabbix LLD
        ZGroupMBeansExporter zGroupMBeansExporter = new ZGroupMBeansExporter(
            "agw.zabbix.lld:type=",
            DefaultZGroupJsonMapper.INSTANCE,
            "JsonData");

        zGroupMBeansExporter.ensureGroup("ensuredGroup");
        ZabbixLldMetricsReporter zabbixLldMetricsReporter = new ZabbixLldMetricsReporter(zGroupMBeansExporter);

        zabbixLldMetricsReporter.addRules(
            forMetricInstancesMatching(nameMask("allEndpoints.clientRequestProcessingTime")),
            new Rule("methodName", List.of(new RuleItem(METHOD, "methodName"))));

        zabbixLldMetricsReporter.addRules(
            forMetricInstancesMatching(nameMask("ensuredGroup.**")),
            new Rule("ensuredGroup", List.of(new RuleItem(i -> i.valueOf(METHOD) + "_" + i.valueOf(METHOD), "methodName"))));

        registry.addListener(zabbixLldMetricsReporter);

        // JMX
        MaskTreeMetricNamedInfoProvider<MBeanSpecProvider> mBeanSpecs = new MaskTreeMetricNamedInfoProvider<>();

        mBeanSpecs.addInfo(
            forMetricInstancesMatching(nameMask("allEndpoints.clientRequestProcessingTime"), instance -> instance.hasDimension(METHOD)),
            instance -> mBeanSpec()
                .name(instance.name().withNewPart(instance.valueOf(METHOD), 1))
                .dimensionValues(instance.dimensionValuesWithout(METHOD)));

        JmxMetricsReporter jmxReporter = new JmxMetricsReporter(
            mBeanSpecs,
            GarmMeasurableNameProvider.INSTANCE);

        registry.addListener(jmxReporter);

        // Metrics
        Timer allEndpointsRequestTimer_garm_4 = registry.timer(
            withKey(name("allEndpoints", "clientRequestProcessingTime"), dimensionValues(SAMPLE.value("garm_4"))),
            () -> withTimer()
                .dimensions(METHOD)
                .expireDimensionalInstanceAfter(50L, SECONDS));

        Timer allEndpointsRequestTimer_garm_5 = registry.timer(
            withKey(name("allEndpoints", "clientRequestProcessingTime"), dimensionValues(SAMPLE.value("garm_5"))),
            () -> withTimer()
                .dimensions(METHOD)
                .expireDimensionalInstanceAfter(50L, SECONDS));

        allEndpointsRequestTimer_garm_4.update(10L, dimensionValues(METHOD.value("POST")));
        allEndpointsRequestTimer_garm_4.update(20L, dimensionValues(METHOD.value("POST")));
        allEndpointsRequestTimer_garm_4.update(30L, dimensionValues(METHOD.value("HEAD")));

        allEndpointsRequestTimer_garm_5.update(30L, dimensionValues(METHOD.value("TEST")));

        registry.counter(withName("allEndpoints", "activeClientConnectionsCount"));
        registry.counter(withName("allEndpoints", "activeRequestsCount"));
        registry.counter(withName("allEndpoints", "2xxCount"));

        LongVar longVar = registry.longVar(
            withName("longVar"),
            noTotal(),
            () -> longVar().dimensions(SERVICE));

        ObjectVar objectVar = registry.objectVar(
            withName("stringSet"),
            () -> Set.of("A", "B", "C"));

        LongVar ensuredGroupVar = registry.longVar(
            withName("ensuredGroup"),
            noTotal(),
            () -> longVar().dimensions(METHOD));

        AtomicLong valueSupplier_1 = new AtomicLong();
        longVar.register(valueSupplier_1::incrementAndGet, dimensionValues(SERVICE.value("agw")));

        AtomicLong valueSupplier_2 = new AtomicLong(500);
        longVar.register(valueSupplier_2::incrementAndGet, dimensionValues(SERVICE.value("wsg")));

        Rate rate = registry.rate(withName("rate"), () -> withRate().dimensions(SERVICE));
        rate.mark(forDimensionValues(SERVICE.value("amd")));

        longVar.deregister(forDimensionValues(SERVICE.value("agw")));

        System.out.println("Telegraf 1:");
        System.out.println(new ObjectMapper().writeValueAsString(telegrafMetricsJsonExporter.exportMetrics()));
        System.out.println("**********\n\n");

        System.out.println("Zabbix:");
        System.out.println(new ObjectMapper().writeValueAsString(zabbixMetricsJsonExporter.exportMetrics()));
        System.out.println("**********\n\n");

        allEndpointsRequestTimer_garm_4.update(30L, dimensionValues(METHOD.value("GET")));
        sleep(55L * 1000L);

        System.out.println("Telegraf 2:");
        System.out.println(new ObjectMapper().writeValueAsString(telegrafMetricsJsonExporter.exportMetrics()));
        System.out.println("**********\n\n");

        hang();
    }
}
