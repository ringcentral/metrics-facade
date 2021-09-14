package com.ringcentral.platform.metrics.var.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.builders.*;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.VarConfig;

import java.util.List;

import static com.ringcentral.platform.metrics.utils.Preconditions.*;

@SuppressWarnings("unchecked")
public abstract class AbstractVarConfigBuilder<C extends VarConfig, CB extends VarConfigBuilder<C, CB>>
    extends AbstractMetricConfigBuilder<C, CB> implements VarConfigBuilder<C, CB> {

    private List<MetricDimension> dimensions;

    @Override
    public void rebase(MetricConfigBuilder<?> base) {
        if (base instanceof AbstractVarConfigBuilder) {
            AbstractVarConfigBuilder<?, ?> varBase = (AbstractVarConfigBuilder<?, ?>)base;

            if (prefixDimensionValues() == null
                && varBase.prefixDimensionValues() != null
                && dimensions != null) {

                checkDimensionsUnique(varBase.prefixDimensionValues(), dimensions);
            }
        }

        super.rebase(base);
    }

    @Override
    public void modify(MetricConfigBuilder<?> mod) {
        if (mod instanceof AbstractVarConfigBuilder) {
            AbstractVarConfigBuilder<?, ?> modBase = (AbstractVarConfigBuilder<?, ?>)mod;

            if (modBase.prefixDimensionValues() != null && dimensions != null) {
                checkDimensionsUnique(modBase.prefixDimensionValues(), dimensions);
            }
        }

        super.modify(mod);
    }

    @Override
    public CB prefix(MetricDimensionValues dimensionValues) {
        checkDimensionsUnique(dimensionValues, dimensions);
        return super.prefix(dimensionValues);
    }

    public CB dimensions(MetricDimension... dimensions) {
        return dimensions(List.of(dimensions));
    }

    public CB dimensions(List<MetricDimension> dimensions) {
        checkState(this.dimensions == null, "Dimensions change is not allowed");
        checkArgument(dimensions != null && !dimensions.isEmpty(), "dimensions is null or empty");
        checkDimensionsUnique(prefixDimensionValues(), dimensions);
        this.dimensions = dimensions;
        return builder();
    }

    protected List<MetricDimension> dimensions() {
        return dimensions;
    }

    @Override
    public C build() {
        return buildImpl(
            hasEnabled() ? getEnabled() : DEFAULT_ENABLED,
            prefixDimensionValues(),
            dimensions,
            context().unmodifiable());
    }

    protected abstract C buildImpl(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricContext context);

    @Override
    public CB builder() {
        return (CB)this;
    }
}