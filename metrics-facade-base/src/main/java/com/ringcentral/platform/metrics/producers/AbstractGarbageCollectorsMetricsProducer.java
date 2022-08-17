package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.names.MetricName;

import java.lang.management.GarbageCollectorMXBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractGarbageCollectorsMetricsProducer extends AbstractMetricsProducer implements GarbageCollectorsMetricsProducer {

    public static final MetricName DEFAULT_NAME_PREFIX = MetricName.of("GarbageCollectors");

    protected static final String COLLECTION_COUNT_DESCRIPTION = "The total number of collections that have occurred";
    protected static final String COLLECTION_TIME_DESCRIPTION = "The approximate accumulated collection elapsed time in milliseconds";

    protected final List<GarbageCollectorMXBean> gcMxBeans;

    public AbstractGarbageCollectorsMetricsProducer(
            MetricName namePrefix,
            MetricModBuilder metricModBuilder,
            Collection<GarbageCollectorMXBean> gcMxBeans) {

        super(namePrefix, metricModBuilder);
        this.gcMxBeans = new ArrayList<>(gcMxBeans);
    }
}
