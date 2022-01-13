package com.ringcentral.platform.metrics.x.histogram.hdr;

import com.ringcentral.platform.metrics.utils.*;

import java.time.Duration;

/**
 * That's just a slightly refactored version of
 * https://github.com/vladimir-bukhtoyarov/rolling-metrics/blob/3.0/rolling-metrics-core/src/main/java/com/github/rollingmetrics/histogram/hdr/SnapshotCachingRollingHdrHistogram.java
 * (Copyright 2017 Vladimir Bukhtoyarov Licensed under the Apache License, Version 2.0)
 * We thank Vladimir Bukhtoyarov for his great library.
 */
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
