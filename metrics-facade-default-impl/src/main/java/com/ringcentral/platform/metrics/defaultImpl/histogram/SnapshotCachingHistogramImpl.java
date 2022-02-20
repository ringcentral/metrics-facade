package com.ringcentral.platform.metrics.defaultImpl.histogram;

import com.ringcentral.platform.metrics.utils.*;

import java.time.Duration;

public class SnapshotCachingHistogramImpl implements HistogramImpl {

    private final HistogramImpl parent;
    private final CachingSupplier<HistogramSnapshot> cachingSnapshotSupplier;

    public SnapshotCachingHistogramImpl(HistogramImpl parent, Duration ttl) {
        this(
            parent,
            ttl,
            SystemTimeNanosProvider.INSTANCE);
    }

    public SnapshotCachingHistogramImpl(
        HistogramImpl parent,
        Duration ttl,
        TimeNanosProvider timeNanosProvider) {

        this.parent = parent;
        this.cachingSnapshotSupplier = new CachingSupplier<>(parent::snapshot, ttl, timeNanosProvider);
    }

    @Override
    public void update(long value) {
        parent.update(value);
    }

    @Override
    public synchronized HistogramSnapshot snapshot() {
        return cachingSnapshotSupplier.get();
    }
}
