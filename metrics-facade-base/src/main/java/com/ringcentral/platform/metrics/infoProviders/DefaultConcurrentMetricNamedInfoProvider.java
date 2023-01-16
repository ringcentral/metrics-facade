package com.ringcentral.platform.metrics.infoProviders;

import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;

import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultConcurrentMetricNamedInfoProvider<I>
    extends DefaultMetricNamedInfoProvider<I>
    implements ConcurrentMetricNamedInfoProvider<I> {

    public DefaultConcurrentMetricNamedInfoProvider() {
        super(new CopyOnWriteArrayList<>(), new HashMap<>());
    }

    @Override
    public synchronized DefaultConcurrentMetricNamedInfoProvider<I> addInfo(String key, MetricNamedPredicate predicate, I info) {
        super.addInfo(key, predicate, info);
        return this;
    }

    @Override
    public synchronized DefaultConcurrentMetricNamedInfoProvider<I> removeInfo(String key) {
        super.removeInfo(key);
        return this;
    }
}
