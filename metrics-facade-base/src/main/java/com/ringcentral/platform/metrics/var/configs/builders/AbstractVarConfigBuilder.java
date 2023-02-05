package com.ringcentral.platform.metrics.var.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.builders.AbstractMetricConfigBuilder;
import com.ringcentral.platform.metrics.configs.builders.MetricConfigBuilder;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.var.configs.VarConfig;

import java.util.List;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkState;

@SuppressWarnings("unchecked")
public abstract class AbstractVarConfigBuilder<C extends VarConfig, CB extends VarConfigBuilder<C, CB>>
    extends AbstractMetricConfigBuilder<C, CB> implements VarConfigBuilder<C, CB> {

    public static final boolean DEFAULT_NON_DECREASING = false;

    private List<Label> labels;
    private Boolean nonDecreasing;

    @Override
    public void rebase(MetricConfigBuilder<?> base) {
        if (base instanceof AbstractVarConfigBuilder) {
            AbstractVarConfigBuilder<?, ?> varBase = (AbstractVarConfigBuilder<?, ?>)base;

            if (prefixLabelValues() == null
                && varBase.prefixLabelValues() != null
                && labels != null) {

                checkLabelsUnique(varBase.prefixLabelValues(), labels);
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

            if (varMod.prefixLabelValues() != null && labels != null) {
                checkLabelsUnique(varMod.prefixLabelValues(), labels);
            }

            if (varMod.hasNonDecreasing()) {
                nonDecreasing(varMod.getNonDecreasing());
            }
        }

        super.modify(mod);
    }

    @Override
    public CB prefix(LabelValues labelValues) {
        checkLabelsUnique(labelValues, labels);
        return super.prefix(labelValues);
    }

    public CB labels(Label... labels) {
        return labels(List.of(labels));
    }

    public CB labels(List<Label> labels) {
        checkState(this.labels == null, "Labels change is not allowed");
        checkArgument(labels != null && !labels.isEmpty(), "labels is null or empty");
        checkLabelsUnique(prefixLabelValues(), labels);
        this.labels = labels;
        return builder();
    }

    protected List<Label> labels() {
        return labels;
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
            prefixLabelValues(),
            labels,
            hasNonDecreasing() ? getNonDecreasing() : DEFAULT_NON_DECREASING,
            context().unmodifiable());
    }

    protected abstract C buildImpl(
        boolean enabled,
        String description,
        LabelValues prefixLabelValues,
        List<Label> labels,
        boolean nonDecreasing,
        MetricContext context);

    @Override
    public CB builder() {
        return (CB)this;
    }
}