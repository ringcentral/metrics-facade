package com.ringcentral.platform.metrics.infoProviders;

import java.util.List;
import java.util.concurrent.locks.*;
import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;

public class ConcurrentMaskTreeMetricNamedInfoProvider<I> extends MaskTreeMetricNamedInfoProvider<I> {

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    @Override
    public MaskTreeMetricNamedInfoProvider<I> addInfo(MetricNamedPredicate predicate, I info) {
        try {
            rwLock.writeLock().lock();
            return super.addInfo(predicate, info);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public List<I> infosFor(MetricNamed named) {
        try {
            rwLock.readLock().lock();
            return super.infosFor(named);
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
