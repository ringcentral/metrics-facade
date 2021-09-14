package com.ringcentral.platform.metrics;

import java.util.function.Consumer;
import org.slf4j.Logger;
import com.ringcentral.platform.metrics.names.MetricName;
import static java.util.Objects.*;
import static org.slf4j.LoggerFactory.*;

public abstract class AbstractMetric implements Metric {

    private final boolean enabled;
    private final MetricName name;
    private final String description;

    private static final Logger logger = getLogger(AbstractMetric.class);

    protected AbstractMetric(
        boolean enabled,
        MetricName name,
        String description) {

        this.enabled = enabled;
        this.name = requireNonNull(name);
        this.description = description;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public MetricName name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    protected static void notifyListener(MetricListener listener, Consumer<MetricListener> notification) {
        try {
            notification.accept(listener);
        } catch (Exception e) {
            logger.error("Failed to notify listener", e);
        }
    }
}
