package com.ringcentral.platform.metrics.var;

import java.util.function.Supplier;

public class CountingValueSupplier<V> implements Supplier<V> {

    final Supplier<V> parent;
    int count;

    public CountingValueSupplier(Supplier<V> parent) {
        this.parent = parent;
    }

    @Override
    public V get() {
        ++count;
        return parent.get();
    }

    public int count() {
        return count;
    }
}
