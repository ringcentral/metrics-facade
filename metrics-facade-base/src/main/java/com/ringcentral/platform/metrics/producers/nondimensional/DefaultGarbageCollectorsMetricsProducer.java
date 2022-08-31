package com.ringcentral.platform.metrics.producers.nondimensional;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractGarbageCollectorsMetricsProducer;

import java.lang.management.GarbageCollectorMXBean;
import java.util.Collection;

import static com.ringcentral.platform.metrics.names.MetricName.name;
import static java.lang.management.ManagementFactory.getGarbageCollectorMXBeans;
import static org.apache.commons.lang3.StringUtils.split;

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
            MetricName namePrefix = nameWithSuffix(split(
                WHITESPACE_PATTERN.matcher(gcMxBean.getName()).replaceAll("-"),
                "."));

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
