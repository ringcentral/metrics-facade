package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;

import java.util.*;

import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;

public abstract class AbstractMeterConfig<IC extends MeterInstanceConfig, SC extends MeterSliceConfig<IC>>
    extends AbstractMetricConfig implements MeterConfig<IC, SC> {

    private final List<MetricDimension> dimensions;
    private final MetricDimensionValuesPredicate exclusionPredicate;
    private final SC allSliceConfig;
    private final Set<SC> sliceConfigs;

    protected AbstractMeterConfig(
        boolean enabled,
        String description,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricDimensionValuesPredicate exclusionPredicate,
        SC allSliceConfig,
        Set<SC> sliceConfigs,
        MetricContext context) {

        super(
            enabled,
            description,
            prefixDimensionValues,
            context);

        this.dimensions = dimensions != null ? dimensions : emptyList();
        this.exclusionPredicate = exclusionPredicate;
        this.allSliceConfig = requireNonNull(allSliceConfig);
        this.sliceConfigs = sliceConfigs != null ? sliceConfigs : emptySet();
    }

    @Override
    public List<MetricDimension> dimensions() {
        return dimensions;
    }

    @Override
    public MetricDimensionValuesPredicate exclusionPredicate() {
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
