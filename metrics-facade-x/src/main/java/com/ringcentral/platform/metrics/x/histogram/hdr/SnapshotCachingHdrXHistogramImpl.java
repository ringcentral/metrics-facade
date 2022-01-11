package com.ringcentral.platform.metrics.x.histogram.hdr;

import com.ringcentral.platform.metrics.utils.*;

import java.time.Duration;

public class SnapshotCachingHdrXHistogramImpl implements HdrXHistogramImpl {

    private final HdrXHistogramImpl parent;
    private final CachingSupplier<HdrXHistogramImplSnapshot> cachingSnapshotSupplier;

    public SnapshotCachingHdrXHistogramImpl(HdrXHistogramImpl parent, Duration ttl) {
        this(
            parent,
            ttl,
            SystemTimeNanosProvider.INSTANCE);
    }

    public SnapshotCachingHdrXHistogramImpl(
        HdrXHistogramImpl parent,
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
    public long count() {
        return parent.count();
    }

    @Override
    public long totalSum() {
        return parent.totalSum();
    }

    @Override
    public synchronized HdrXHistogramImplSnapshot snapshot() {
        return cachingSnapshotSupplier.get();
    }
}
