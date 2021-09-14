package com.ringcentral.platform.metrics.configs;

import java.util.Set;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import static com.ringcentral.platform.metrics.UnmodifiableMetricContext.*;
import static java.util.Collections.*;

public abstract class AbstractMeterInstanceConfig implements MeterInstanceConfig {

    private final MetricName name;
    private final Set<? extends Measurable> measurables;
    private final MetricContext context;

    public AbstractMeterInstanceConfig(
        MetricName name,
        Set<? extends Measurable> measurables,
        MetricContext context) {

        this.name = name;
        this.measurables = measurables != null ? measurables : emptySet();
        this.context = context != null ? context : emptyUnmodifiableMetricContext();
    }

    @Override
    public MetricName name() {
        return name;
    }

    @Override
    public Set<? extends Measurable> measurables() {
        return measurables;
    }

    @Override
    public MetricContext context() {
        return context;
    }
}
