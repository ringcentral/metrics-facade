package com.ringcentral.platform.metrics.defaultImpl.var;

import com.ringcentral.platform.metrics.utils.*;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import org.slf4j.Logger;

import java.util.concurrent.atomic.*;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

public class DefaultCachingValueSupplier<V> implements Supplier<V> {

    private final long ttl;
    private final Supplier<V> valueSupplier;
    private final TimeNanosProvider timeNanosProvider;
    private final AtomicReference<V> value;
    private final AtomicLong valueSupplyTime;

    private static final Logger logger = getLogger(DefaultCachingValueSupplier.class);

    public DefaultCachingValueSupplier(CachingVarConfig config, Supplier<V> valueSupplier) {
        this(config, valueSupplier, SystemTimeNanosProvider.INSTANCE);
    }

    public DefaultCachingValueSupplier(
        CachingVarConfig config,
        Supplier<V> valueSupplier,
        TimeNanosProvider timeNanosProvider) {

        this.ttl = config.ttlUnit().toNanos(config.ttl());
        this.valueSupplier = valueSupplier;
        this.timeNanosProvider = timeNanosProvider;
        this.value = new AtomicReference<>();
        this.valueSupplyTime = new AtomicLong(timeNanosProvider.timeNanos());
    }

    @Override
    public V get() {
        V currValue = value.get();

        if (updateValueSupplyTimeIfNeeded() || currValue == null) {
            V newValue;

            try {
                newValue = valueSupplier.get();
            } catch (Exception e) {
                logger.error("Failed to supply value", e);
                return currValue;
            }

            return value.compareAndSet(currValue, newValue) ? newValue : value.get();
        }

        return currValue;
    }

    private boolean updateValueSupplyTimeIfNeeded() {
        for (;;) {
            long now = timeNanosProvider.timeNanos();
            long localValueSupplyTime = valueSupplyTime.get();

            if (localValueSupplyTime > now) {
                return false;
            }

            if (valueSupplyTime.compareAndSet(localValueSupplyTime, now + ttl)) {
                return true;
            }
        }
    }
}
