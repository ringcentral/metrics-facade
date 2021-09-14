package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.builders.VarConfigBuilder;
import com.ringcentral.platform.metrics.var.doubleVar.configs.builders.DoubleVarConfigBuilder;
import com.ringcentral.platform.metrics.var.longVar.configs.builders.LongVarConfigBuilder;
import com.ringcentral.platform.metrics.var.objectVar.configs.builders.ObjectVarConfigBuilder;
import com.ringcentral.platform.metrics.var.stringVar.configs.builders.StringVarConfigBuilder;

import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.ringcentral.platform.metrics.var.doubleVar.configs.builders.DoubleVarConfigBuilder.*;
import static com.ringcentral.platform.metrics.var.longVar.configs.builders.LongVarConfigBuilder.*;
import static com.ringcentral.platform.metrics.var.objectVar.configs.builders.ObjectVarConfigBuilder.*;
import static com.ringcentral.platform.metrics.var.stringVar.configs.builders.StringVarConfigBuilder.*;
import static java.util.Objects.*;

public abstract class AbstractMetricsProducer implements MetricsProducer {

    protected static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\\s]+");

    private final MetricName namePrefix;
    private final MetricMod metricMod;

    protected AbstractMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this.namePrefix = requireNonNull(namePrefix);
        this.metricMod = metricModBuilder != null ? metricModBuilder.build() : null;
    }

    protected MetricName namePrefix() {
        return namePrefix;
    }

    protected MetricName nameWithSuffix(MetricName suffix) {
        return namePrefix.isEmpty() ? suffix : MetricName.of(namePrefix, suffix);
    }

    protected MetricName nameWithSuffix(String... suffix) {
        return namePrefix.isEmpty() ? MetricName.of(suffix) : MetricName.of(namePrefix, suffix);
    }

    protected Supplier<ObjectVarConfigBuilder> objectVarConfigBuilderSupplier() {
        return () -> modified(objectVarConfigBuilder());
    }

    protected Supplier<LongVarConfigBuilder> longVarConfigBuilderSupplier() {
        return () -> modified(longVarConfigBuilder());
    }

    protected Supplier<DoubleVarConfigBuilder> doubleVarConfigBuilderSupplier() {
        return () -> modified(doubleVarConfigBuilder());
    }

    protected Supplier<StringVarConfigBuilder> stringVarConfigBuilderSupplier() {
        return () -> modified(stringVarConfigBuilder());
    }

    private <CB extends VarConfigBuilder<?, ?>> CB modified(CB builder) {
        if (metricMod != null) {
            if (metricMod.hasVarConfigBuilder()) {
                builder.modify(metricMod.varConfigBuilder());
            } else if (metricMod.hasMetricConfigBuilder()) {
                builder.modify(metricMod.metricConfigBuilder());
            }
        }

        return builder;
    }
}
