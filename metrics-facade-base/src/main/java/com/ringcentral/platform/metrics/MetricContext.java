package com.ringcentral.platform.metrics;

public interface MetricContext {
    default boolean has(Object key) {
        return get(key) != null;
    }

    default <V> V get(Object key, V defaultValue) {
        V v = get(key);
        return v != null ? v : defaultValue;
    }

    <V> V get(Object key);
    boolean isEmpty();
}
