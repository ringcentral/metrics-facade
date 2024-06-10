package com.ringcentral.platform.metrics.producers.unlabeled;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractGarbageCollectorsMetricsProducer;

import java.lang.management.GarbageCollectorMXBean;
import java.util.Collection;

import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.utils.StringUtils.splitByDot;
import static java.lang.management.ManagementFactory.getGarbageCollectorMXBeans;

/**
 * Produces<br>
 * <ul>
 *     <li>
 *     <li><i>G1_Old_Generation.time</i> - the approximate accumulated collection elapsed time in milliseconds.</li>
 *     <li><i>G1_Old_Generation.count</i> - the total number of collections that have occurred.</li>
 *     <li><i>G1_Young_Generation.time</i> - the approximate accumulated collection elapsed time in milliseconds.</li>
 *     <li><i>G1_Young_Generation.count</i> - the total number of collections that have occurred.</li>
 * </ul>
 *
 * All metrics have a name prefix. By default it is 'GarbageCollectors'.<br>
 * <br>
 * Example of usage:
 * <pre>
 * MetricRegistry registry = new DefaultMetricRegistry();
 * new DefaultGarbageCollectorsMetricsProducer().produceMetrics(registry);
 * PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);
 * System.out.println(exporter.exportMetrics());
 * </pre>
 * Corresponding output:
 * <pre>
 * # HELP GarbageCollectors_G1_Old_Generation_time The approximate accumulated collection elapsed time in milliseconds
 * # TYPE GarbageCollectors_G1_Old_Generation_time gauge
 * GarbageCollectors_G1_Old_Generation_time 0.0
 * # HELP GarbageCollectors_G1_Young_Generation_count The total number of collections that have occurred
 * # TYPE GarbageCollectors_G1_Young_Generation_count gauge
 * GarbageCollectors_G1_Young_Generation_count 0.0
 * # HELP GarbageCollectors_G1_Young_Generation_time The approximate accumulated collection elapsed time in milliseconds
 * # TYPE GarbageCollectors_G1_Young_Generation_time gauge
 * GarbageCollectors_G1_Young_Generation_time 0.0
 * # HELP GarbageCollectors_G1_Old_Generation_count The total number of collections that have occurred
 * # TYPE GarbageCollectors_G1_Old_Generation_count gauge
 * GarbageCollectors_G1_Old_Generation_count 0.0
 * </pre>
 */
public class DefaultGarbageCollectorsMetricsProducer extends AbstractGarbageCollectorsMetricsProducer {

    public DefaultGarbageCollectorsMetricsProducer() {
        this(DEFAULT_NAME_PREFIX);
    }

    public DefaultGarbageCollectorsMetricsProducer(MetricName namePrefix) {
        this(namePrefix, null);
    }

    public DefaultGarbageCollectorsMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(namePrefix, metricModBuilder, getGarbageCollectorMXBeans());
    }

    public DefaultGarbageCollectorsMetricsProducer(
        MetricName namePrefix,
        MetricModBuilder metricModBuilder,
        Collection<GarbageCollectorMXBean> gcMxBeans) {

        super(namePrefix, metricModBuilder, gcMxBeans);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        for (GarbageCollectorMXBean gcMxBean : gcMxBeans) {
            MetricName namePrefix = nameWithSuffix(splitByDot(WHITESPACE_PATTERN.matcher(gcMxBean.getName()).replaceAll("-")));

            registry.longVar(
                name(namePrefix, "count"),
                gcMxBean::getCollectionCount,
                longVarConfigBuilderSupplier(COLLECTION_COUNT_DESCRIPTION));

            registry.longVar(
                name(namePrefix, "time"),
                gcMxBean::getCollectionTime,
                longVarConfigBuilderSupplier(COLLECTION_TIME_DESCRIPTION));
        }
    }
}
