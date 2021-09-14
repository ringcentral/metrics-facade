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

    public static final boolean DEFAULT_NON_DECREASING = false;

    private List<MetricDimension> dimensions;
    private Boolean nonDecreasing;

    @Override
    public void rebase(MetricConfigBuilder<?> base) {
        if (base instanceof AbstractVarConfigBuilder) {
            AbstractVarConfigBuilder<?, ?> varBase = (AbstractVarConfigBuilder<?, ?>)base;

            if (prefixDimensionValues() == null
                && varBase.prefixDimensionValues() != null
                && dimensions != null) {

                checkDimensionsUnique(varBase.prefixDimensionValues(), dimensions);
            }

            if (varBase.hasNonDecreasing() && !hasNonDecreasing()) {
                nonDecreasing(varBase.getNonDecreasing());
            }
        }

        super.rebase(base);
    }

    @Override
    public void modify(MetricConfigBuilder<?> mod) {
        if (mod instanceof AbstractVarConfigBuilder) {
            AbstractVarConfigBuilder<?, ?> varMod = (AbstractVarConfigBuilder<?, ?>)mod;

            if (varMod.prefixDimensionValues() != null && dimensions != null) {
                checkDimensionsUnique(varMod.prefixDimensionValues(), dimensions);
            }

            if (varMod.hasNonDecreasing()) {
                nonDecreasing(varMod.getNonDecreasing());
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

    public boolean hasNonDecreasing() {
        return nonDecreasing != null;
    }

    public CB nonDecreasing() {
        return nonDecreasing(true);
    }

    public CB nonDecreasing(boolean nonDecreasing) {
        this.nonDecreasing = nonDecreasing;
        return builder();
    }

    protected Boolean getNonDecreasing() {
        return nonDecreasing;
    }

    @Override
    public C build() {
        return buildImpl(
            hasEnabled() ? getEnabled() : DEFAULT_ENABLED,
            description(),
            prefixDimensionValues(),
            dimensions,
            hasNonDecreasing() ? getNonDecreasing() : DEFAULT_NON_DECREASING,
            context().unmodifiable());
    }

    protected abstract C buildImpl(
        boolean enabled,
        String description,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        boolean nonDecreasing,
        MetricContext context);

    @Override
    public CB builder() {
        return (CB)this;
    }
}