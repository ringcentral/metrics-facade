package com.ringcentral.platform.metrics.producers.labeled;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractGarbageCollectorsMetricsProducer;
import com.ringcentral.platform.metrics.var.Var;

import java.lang.management.GarbageCollectorMXBean;
import java.util.Collection;

import static com.ringcentral.platform.metrics.labels.LabelValues.labelValues;
import static java.lang.management.ManagementFactory.getGarbageCollectorMXBeans;

/**
 * Produces<br>
 * <ul>
 *     <li>
 *         <i>collection.time</i> - the approximate accumulated collection elapsed time in milliseconds.<br>
 *         Labels:<br>
 *         name = {"G1 Young Generation", "G1 Old Generation"}<br>
 *     </li>
 *     <li>
 *         <i>collection.count</i> - the total number of collections that have occurred.<br>
 *         Labels:<br>
 *         name = {"G1 Young Generation", "G1 Old Generation"}<br>
 *     </li>
 * </ul>
 *
 * All metrics have a name prefix. By default it is 'GarbageCollectors'.<br>
 * <br>
 * Example of usage:
 * <pre>
 * MetricRegistry registry = new DefaultMetricRegistry();
 * new LabeledGarbageCollectorsMetricsProducer().produceMetrics(registry);
 * PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);
 * System.out.println(exporter.exportMetrics());
 * </pre>
 * Corresponding output:
 * <pre>
 * # HELP GarbageCollectors_collection_time The approximate accumulated collection elapsed time in milliseconds
 * # TYPE GarbageCollectors_collection_time gauge
 * GarbageCollectors_collection_time{name="G1 Young Generation",} 0.0
 * GarbageCollectors_collection_time{name="G1 Old Generation",} 0.0
 * # HELP GarbageCollectors_collection_count The total number of collections that have occurred
 * # TYPE GarbageCollectors_collection_count gauge
 * GarbageCollectors_collection_count{name="G1 Young Generation",} 0.0
 * GarbageCollectors_collection_count{name="G1 Old Generation",} 0.0
 * </pre>
 */
public class LabeledGarbageCollectorsMetricsProducer extends AbstractGarbageCollectorsMetricsProducer {

    private static final Label NAME_LABEL = new Label("name");

    public LabeledGarbageCollectorsMetricsProducer() {
        this(DEFAULT_NAME_PREFIX);
    }

    public LabeledGarbageCollectorsMetricsProducer(MetricName namePrefix) {
        this(namePrefix, null);
    }

    public LabeledGarbageCollectorsMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(namePrefix, metricModBuilder, getGarbageCollectorMXBeans());
    }

    public LabeledGarbageCollectorsMetricsProducer(
        MetricName namePrefix,
        MetricModBuilder metricModBuilder,
        Collection<GarbageCollectorMXBean> gcMxBeans) {

        super(namePrefix, metricModBuilder, gcMxBeans);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        final var collectionCount = registry.longVar(
            nameWithSuffix("collection", "count"),
            Var.noTotal(),
            longVarConfigBuilderSupplier(COLLECTION_COUNT_DESCRIPTION, NAME_LABEL));

        final var collectionTime = registry.longVar(
            nameWithSuffix("collection", "time"),
            Var.noTotal(),
            longVarConfigBuilderSupplier(COLLECTION_TIME_DESCRIPTION, NAME_LABEL));

        for (GarbageCollectorMXBean gcMxBean : gcMxBeans) {
            final var name = gcMxBean.getName();
            final var nameLabelValue = NAME_LABEL.value(name);
            final var labelValues = labelValues(nameLabelValue);

            collectionCount.register(gcMxBean::getCollectionCount, labelValues);
            collectionTime.register(gcMxBean::getCollectionTime, labelValues);
        }
    }
}
