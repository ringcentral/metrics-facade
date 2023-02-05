package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.*;

import java.util.*;

import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;

public abstract class AbstractMeterConfig<IC extends MeterInstanceConfig, SC extends MeterSliceConfig<IC>>
    extends AbstractMetricConfig implements MeterConfig<IC, SC> {

    private final List<Label> labels;
    private final LabelValuesPredicate exclusionPredicate;
    private final SC allSliceConfig;
    private final Set<SC> sliceConfigs;

    protected AbstractMeterConfig(
        boolean enabled,
        String description,
        LabelValues prefixLabelValues,
        List<Label> labels,
        LabelValuesPredicate exclusionPredicate,
        SC allSliceConfig,
        Set<SC> sliceConfigs,
        MetricContext context) {

        super(
            enabled,
            description,
            prefixLabelValues,
            context);

        this.labels = labels != null ? labels : emptyList();
        this.exclusionPredicate = exclusionPredicate;
        this.allSliceConfig = requireNonNull(allSliceConfig);
        this.sliceConfigs = sliceConfigs != null ? sliceConfigs : emptySet();
    }

    @Override
    public List<Label> labels() {
        return labels;
    }

    @Override
    public LabelValuesPredicate exclusionPredicate() {
        return exclusionPredicate;
    }

    @Override
    public SC allSliceConfig() {
        return allSliceConfig;
    }

    @Override
    public Set<SC> sliceConfigs() {
        return sliceConfigs;
    }
}
