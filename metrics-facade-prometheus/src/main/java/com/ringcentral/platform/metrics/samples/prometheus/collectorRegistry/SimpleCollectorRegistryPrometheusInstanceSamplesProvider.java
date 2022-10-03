package com.ringcentral.platform.metrics.samples.prometheus.collectorRegistry;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.samples.InstanceSamplesProvider;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSample;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusSample;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Predicate;

import java.util.*;
import java.util.function.Function;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;

public class SimpleCollectorRegistryPrometheusInstanceSamplesProvider implements InstanceSamplesProvider<
    PrometheusSample,
    PrometheusInstanceSample> {

    private final Function<String, MetricName> nameBuilder;
    private final Predicate<String> metricFamilyNameFilter;
    private final Predicate<String> metricFamilySampleNameFilter;
    private final Collection<? extends CollectorRegistry> collectorRegistries;

    /**
     * @param collectorRegistries must be not empty
     */
    public SimpleCollectorRegistryPrometheusInstanceSamplesProvider(CollectorRegistry... collectorRegistries) {
        this(null, collectorRegistries);
    }

    /**
     * @param namePrefix the prefix to be prepended to each MetricFamilySample.name when building PrometheusInstanceSample.instanceName/name
     *                   and to be prepended to each MetricFamilySample.Sample.name when building PrometheusSample.name.
     *                   Optional
     *
     * @param collectorRegistries must be not empty
     */
    public SimpleCollectorRegistryPrometheusInstanceSamplesProvider(
        MetricName namePrefix,
        CollectorRegistry... collectorRegistries) {

        this(namePrefix, null, null, List.of(collectorRegistries));
    }

    /**
     * @param namePrefix the prefix to be prepended to each MetricFamilySample.name when building PrometheusInstanceSample.instanceName/name
     *                   and to be prepended to each MetricFamilySample.Sample.name when building PrometheusSample.name.
     *                   Optional
     *
     * @param metricFamilyNameFilter optional
     * @param metricFamilySampleNameFilter optional
     * @param collectorRegistries must be not empty
     */
    public SimpleCollectorRegistryPrometheusInstanceSamplesProvider(
        MetricName namePrefix,
        Predicate<String> metricFamilyNameFilter,
        Predicate<String> metricFamilySampleNameFilter,
        CollectorRegistry... collectorRegistries) {

        this(namePrefix, metricFamilyNameFilter, metricFamilySampleNameFilter, List.of(collectorRegistries));
    }

    /**
     * @param namePrefix the prefix to be prepended to each MetricFamilySample.name when building PrometheusInstanceSample.instanceName/name
     *                   and to be prepended to each MetricFamilySample.Sample.name when building PrometheusSample.name.
     *                   Optional
     *
     * @param metricFamilyNameFilter optional
     * @param metricFamilySampleNameFilter optional
     * @param collectorRegistries must be not empty
     */
    public SimpleCollectorRegistryPrometheusInstanceSamplesProvider(
        MetricName namePrefix,
        Predicate<String> metricFamilyNameFilter,
        Predicate<String> metricFamilySampleNameFilter,
        Collection<? extends CollectorRegistry> collectorRegistries) {

        this(
            namePrefix == null ? MetricName::of : n -> MetricName.of(namePrefix, n),
            metricFamilyNameFilter,
            metricFamilySampleNameFilter,
            collectorRegistries);
    }

    /**
     * @param nameBuilder the function to be applied to each MetricFamilySample.name when building PrometheusInstanceSample.instanceName/name
     *                    and to be applied to each MetricFamilySample.Sample.name when building PrometheusSample.name.
     *                    Optional
     *
     * @param metricFamilyNameFilter optional
     * @param metricFamilySampleNameFilter optional
     * @param collectorRegistries must be not empty
     */
    public SimpleCollectorRegistryPrometheusInstanceSamplesProvider(
        Function<String, MetricName> nameBuilder,
        Predicate<String> metricFamilyNameFilter,
        Predicate<String> metricFamilySampleNameFilter,
        Collection<? extends CollectorRegistry> collectorRegistries) {

        this.nameBuilder = nameBuilder != null ? nameBuilder : MetricName::of;
        this.metricFamilyNameFilter = metricFamilyNameFilter;
        this.metricFamilySampleNameFilter = metricFamilySampleNameFilter;

        checkArgument(
            collectorRegistries != null && !collectorRegistries.isEmpty(),
            "collectorRegistries is null or empty");

        this.collectorRegistries = collectorRegistries;
    }

    @Override
    public Set<PrometheusInstanceSample> instanceSamples() {
        Set<PrometheusInstanceSample> result = new LinkedHashSet<>();
        collectorRegistries.forEach(collectorRegistry -> processCollectorRegistry(collectorRegistry, result));
        return result;
    }

    private void processCollectorRegistry(CollectorRegistry collectorRegistry, Set<PrometheusInstanceSample> result) {
        Enumeration<MetricFamilySamples> fsEnumeration =
            metricFamilyNameFilter != null ?
            collectorRegistry.filteredMetricFamilySamples(metricFamilyNameFilter) :
            collectorRegistry.metricFamilySamples();

        while (fsEnumeration.hasMoreElements()) {
            MetricFamilySamples fs = fsEnumeration.nextElement();
            MetricName name = nameBuilder.apply(fs.name);
            PrometheusInstanceSample instanceSample = new PrometheusInstanceSample(name, name, fs.help, fs.type);

            fs.samples.forEach(fsSample -> {
                if (metricFamilySampleNameFilter != null && !metricFamilySampleNameFilter.test(fsSample.name)) {
                    return;
                }

                MetricName sampleName = nameBuilder.apply(fsSample.name);

                PrometheusSample sample = new PrometheusSample(
                    null,
                    null,
                    null,
                    sampleName,
                    null,
                    fsSample.labelNames,
                    fsSample.labelValues,
                    fsSample.value);

                instanceSample.add(sample);
            });

            if (!instanceSample.samples().isEmpty()) {
                result.add(instanceSample);
            }
        }
    }
}