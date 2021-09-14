package com.ringcentral.platform.metrics;

import java.util.*;
import static com.ringcentral.platform.metrics.UnmodifiableMetricContext.*;

public abstract class AbstractMetricContext implements MetricContext {

    private final Map<Object, Object> props;

    protected AbstractMetricContext(Map<Object, Object> props) {
        this.props = props;
    }

    protected void put(Object key, Object value) {
        props.put(key, value);
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
