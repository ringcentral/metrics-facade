package com.ringcentral.platform.metrics;

import java.util.HashMap;

public class ModifiableMetricContext extends AbstractMetricContext {

    public ModifiableMetricContext() {
        super(new HashMap<>());
    }

    public ModifiableMetricContext(AbstractMetricContext source) {
        super(new HashMap<>(source.properties()));
    }

    @Override
    public void put(Object key, Object value) {
        super.put(key, value);
    }

    public void put(AbstractMetricContext source) {
        source.properties().forEach(this::put);
    }

    public void putIfAbsent(Object key, Object value) {
        if (!has(key)) {
            put(key, value);
        }
    }

    public void putIfAbsent(AbstractMetricContext source) {
        source.properties().forEach(this::putIfAbsent);
    }
}
