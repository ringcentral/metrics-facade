package com.ringcentral.platform.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.ringcentral.platform.metrics.UnmodifiableMetricContext.emptyUnmodifiableMetricContext;
import static com.ringcentral.platform.metrics.utils.ClassUtils.interfacesOf;

public abstract class AbstractMetricContext implements MetricContext {

    private final Map<Object, Object> props;
    private static final ConcurrentMap<Class<?>, Class<?>> typeKeys = new ConcurrentHashMap<>();

    protected AbstractMetricContext(Map<Object, Object> props) {
        this.props = props;
    }

    protected void put(Object key, Object value) {
        props.put(key, value);
    }

    protected void with(Object value) {
        if (value instanceof MetricContextTypeKeySubtype) {
            Class<?> type = value.getClass();

            Class<?> typeKey = typeKeys.computeIfAbsent(type, t -> {
                for (Class<?> i : interfacesOf(type)) {
                    if (i.isAnnotationPresent(MetricContextTypeKey.class)) {
                        return i;
                    }
                }

                return t;
            });

            put(typeKey, value);
        } else {
            put(value.getClass(), value);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V get(Object key) {
        return isEmpty() ? null : (V)props.get(key);
    }

    protected Map<Object, Object> properties() {
        return props;
    }

    @Override
    public boolean isEmpty() {
        return props.isEmpty();
    }

    public UnmodifiableMetricContext unmodifiable() {
        return isEmpty() ? emptyUnmodifiableMetricContext() : new UnmodifiableMetricContext(this);
    }
}
