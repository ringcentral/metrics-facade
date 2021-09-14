package com.ringcentral.platform.metrics.samples.reporters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.histogram.*;
import com.ringcentral.platform.metrics.reporters.telegraf.TelegrafMetricsJsonExporter;
import com.ringcentral.platform.metrics.samples.*;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.forDimensionValues;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.*;
import static com.ringcentral.platform.metrics.predicates.DefaultMetricInstancePredicate.forMetricInstancesMatching;
import static com.ringcentral.platform.metrics.samples.DefaultInstanceSampleSpec.instanceSampleSpec;
import static com.ringcentral.platform.metrics.samples.DefaultSampleSpec.sampleSpec;

@SuppressWarnings("ALL")
public class TelegrafMetricsJsonExporterSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        MetricRegistry registry = new DropwizardMetricRegistry();
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
                measurable instanceof Max ? sampleSpec().disable() :sampleSpec());

        DefaultInstanceSamplesProvider miSamplesProvider = new DefaultInstanceSamplesProvider(
            miSampleSpecModsProvider,
            sampleSpecModsProvider,
            new DefaultSampleSpecProvider(CustomMeasurableNameProvider.INSTANCE),
            registry);

        // Metrics
        Histogram h = registry.histogram(
            withName("histogram"),
            () -> withHistogram()
                .dimensions(SERVICE, SERVER, PORT)
                .measurables(COUNT, MAX, MEAN));

        h.update(1, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
        h.update(2, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));
        h.update(3, forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));

        System.out.println("Output (without grouping by types):");
        TelegrafMetricsJsonExporter exporter = new TelegrafMetricsJsonExporter(false, miSamplesProvider);
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(exporter.exportMetrics()));
        System.out.println("**********\n");

        System.out.println("Output (with grouping by types):");
        exporter = new TelegrafMetricsJsonExporter(true, miSamplesProvider);
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(exporter.exportMetrics()));
        System.out.println("**********\n");

        hang();
    }
}
