package com.ringcentral.platform.metrics.spring;

import org.springframework.util.Assert;

import java.util.function.*;

public class PropertiesConfigAdapter<T> {

    private final T properties;

    public PropertiesConfigAdapter(T properties) {
        Assert.notNull(properties, "Properties must not be null");
        this.properties = properties;
    }

    protected final <V> V get(Function<T, V> getter, Supplier<V> fallback) {
        V value = getter.apply(this.properties);
        return (value != null) ? value : fallback.get();
    }
}
