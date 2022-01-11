package com.ringcentral.platform.metrics.utils;

import java.time.Duration;
import java.util.function.Supplier;

public class CachingSupplier<V> implements Supplier<V> {

    private final Supplier<V> parent;
    private final long ttl;
    private final TimeNanosProvider timeNanosProvider;

    private V value;
    private long valueSupplyTime;

    public CachingSupplier(
        Supplier<V> parent,
        Duration ttl,
        TimeNanosProvider timeNanosProvider) {

        this.parent = parent;
        this.ttl = ttl.toNanos();
        this.timeNanosProvider = timeNanosProvider;
    }

    @Override
    public V get() {
        long now = timeNanosProvider.timeNanos();

        if (value != null && (now - valueSupplyTime) <= ttl) {
            return value;
        }

        value = parent.get();
        valueSupplyTime = now;
        return value;
    }
}
