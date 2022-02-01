package com.ringcentral.platform.metrics.x.histogram;

import com.ringcentral.platform.metrics.utils.*;

import java.time.Duration;

public class SnapshotCachingXHistogramImpl implements XHistogramImpl {

    private final XHistogramImpl parent;
    private final CachingSupplier<XHistogramSnapshot> cachingSnapshotSupplier;

    public SnapshotCachingXHistogramImpl(XHistogramImpl parent, Duration ttl) {
        this(
            parent,
            ttl,
            SystemTimeNanosProvider.INSTANCE);
    }

    public SnapshotCachingXHistogramImpl(
        XHistogramImpl parent,
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
    public synchronized XHistogramSnapshot snapshot() {
        return cachingSnapshotSupplier.get();
    }
}
