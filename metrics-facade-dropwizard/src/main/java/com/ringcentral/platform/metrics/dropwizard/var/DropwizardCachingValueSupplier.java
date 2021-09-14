package com.ringcentral.platform.metrics.dropwizard.var;

import com.codahale.metrics.CachedGauge;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;

import java.util.function.Supplier;

public class DropwizardCachingValueSupplier<V> extends CachedGauge<V> implements Supplier<V> {

    private final Supplier<V> valueSupplier;

    public DropwizardCachingValueSupplier(CachingVarConfig config, Supplier<V> valueSupplier) {
        super(config.ttl(), config.ttlUnit());
        this.valueSupplier = valueSupplier;
    }

    @Override
    public V get() {
        return getValue();
    }

    @Override
    protected V loadValue() {
        return valueSupplier.get();
    }
}
