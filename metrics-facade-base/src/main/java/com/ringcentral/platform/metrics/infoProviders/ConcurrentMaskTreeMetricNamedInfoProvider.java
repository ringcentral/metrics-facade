package com.ringcentral.platform.metrics.infoProviders;

import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

public class ConcurrentMaskTreeMetricNamedInfoProvider<I>
    extends MaskTreeMetricNamedInfoProvider<I>
    implements ConcurrentMetricNamedInfoProvider<I> {

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    @Override
    public MaskTreeMetricNamedInfoProvider<I> addInfo(String key, MetricNamedPredicate predicate, I info) {
        try {
            rwLock.writeLock().lock();
            return super.addInfo(key, predicate, info);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public MaskTreeMetricNamedInfoProvider<I> removeInfo(String key) {
        try {
            rwLock.writeLock().lock();
            return super.removeInfo(key);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public PredicativeMetricNamedInfoProvider<I> removeInfos(Predicate<String> keyPredicate) {
        try {
            rwLock.writeLock().lock();
            return super.removeInfos(keyPredicate);
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
