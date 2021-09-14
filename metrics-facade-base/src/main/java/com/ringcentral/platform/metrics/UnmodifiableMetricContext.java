package com.ringcentral.platform.metrics;

import java.util.Map;
import static java.util.Collections.*;

public class UnmodifiableMetricContext extends AbstractMetricContext {

    private static final UnmodifiableMetricContext EMPTY = new UnmodifiableMetricContext();

    public static UnmodifiableMetricContext emptyUnmodifiableMetricContext() {
        return EMPTY;
    }

    public UnmodifiableMetricContext() {
        this(emptyMap());
    }

    public UnmodifiableMetricContext(Map<Object, Object> props) {
        super(Map.copyOf(props));
    }

    public UnmodifiableMetricContext(AbstractMetricContext source) {
        super(Map.copyOf(source.properties()));
    }
}
