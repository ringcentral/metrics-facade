package com.ringcentral.platform.metrics.producers.nondimensional;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractMetricsProducer;

import java.lang.management.GarbageCollectorMXBean;
import java.util.*;

import static com.ringcentral.platform.metrics.names.MetricName.*;
import static java.lang.management.ManagementFactory.*;
import static org.apache.commons.lang3.StringUtils.*;

public class GarbageCollectorsMetricsProducer extends AbstractMetricsProducer {

    public static final MetricName DEFAULT_NAME_PREFIX = MetricName.of("GarbageCollectors");
    private final List<GarbageCollectorMXBean> gcMxBeans;

    public GarbageCollectorsMetricsProducer() {
        this(DEFAULT_NAME_PREFIX);
    }

    public GarbageCollectorsMetricsProducer(MetricName namePrefix) {
        this(namePrefix, null);
    }

    public GarbageCollectorsMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(namePrefix, metricModBuilder, getGarbageCollectorMXBeans());
    }

    public GarbageCollectorsMetricsProducer(
        MetricName namePrefix,
        MetricModBuilder metricModBuilder,
        Collection<GarbageCollectorMXBean> gcMxBeans) {

        super(namePrefix, metricModBuilder);
        this.gcMxBeans = new ArrayList<>(gcMxBeans);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        for (GarbageCollectorMXBean gcMxBean : gcMxBeans) {
            MetricName namePrefix = nameWithSuffix(split(
                WHITESPACE_PATTERN.matcher(gcMxBean.getName()).replaceAll("-"),
                "."));

            registry.longVar(name(namePrefix, "count"), gcMxBean::getCollectionCount, longVarConfigBuilderSupplier());
            registry.longVar(name(namePrefix, "time"), gcMxBean::getCollectionTime, longVarConfigBuilderSupplier());
        }
    }
}
