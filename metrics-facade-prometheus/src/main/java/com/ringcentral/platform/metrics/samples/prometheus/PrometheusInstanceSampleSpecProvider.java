package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.samples.InstanceSampleSpecProvider;
import com.ringcentral.platform.metrics.var.objectVar.ObjectVar;
import com.ringcentral.platform.metrics.var.stringVar.StringVar;

public class PrometheusInstanceSampleSpecProvider implements InstanceSampleSpecProvider<PrometheusInstanceSampleSpec> {

    public static final boolean DEFAULT_EXPORT_TOTAL_INSTANCES = true;
    public static final boolean DEFAULT_EXPORT_LABELED_METRIC_TOTAL_INSTANCES = false;
    public static final boolean DEFAULT_EXPORT_LEVEL_INSTANCES = false;

    private final boolean exportTotalInstances;
    private final boolean exportLabeledMetricTotalInstances;
    private final boolean exportLevelInstances;

    public PrometheusInstanceSampleSpecProvider() {
        this(
            DEFAULT_EXPORT_TOTAL_INSTANCES,
            DEFAULT_EXPORT_LABELED_METRIC_TOTAL_INSTANCES,
            DEFAULT_EXPORT_LEVEL_INSTANCES);
    }

    public PrometheusInstanceSampleSpecProvider(boolean exportLabeledMetricTotalInstances) {
        this(
            DEFAULT_EXPORT_TOTAL_INSTANCES,
            exportLabeledMetricTotalInstances,
            DEFAULT_EXPORT_LEVEL_INSTANCES);
    }

    public PrometheusInstanceSampleSpecProvider(
        boolean exportTotalInstances,
        boolean exportLabeledMetricTotalInstances,
        boolean exportLevelInstances) {

        this.exportTotalInstances = exportTotalInstances;
        this.exportLabeledMetricTotalInstances = exportLabeledMetricTotalInstances;
        this.exportLevelInstances = exportLevelInstances;
    }

    @Override
    public PrometheusInstanceSampleSpec instanceSampleSpecFor(
        Metric metric,
        MetricInstance instance,
        PrometheusInstanceSampleSpec currSpec) {

        if (metric instanceof ObjectVar || metric instanceof StringVar) {
            return null;
        }

        if (instance.isTotalInstance()) {
            if (!exportTotalInstances) {
                return null;
            }

            if (instance.isLabeledMetricTotalInstance() && !exportLabeledMetricTotalInstances) {
                return null;
            }
        }

        if (instance.isLevelInstance() && !exportLevelInstances) {
            return null;
        }

        return new PrometheusInstanceSampleSpec(
            Boolean.TRUE,
            instance,
            instance.name(),
            metric.hasDescription() ? metric.description() : null,
            instance.labelValues());
    }
}
