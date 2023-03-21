package com.ringcentral.platform.metrics.var.objectVar.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.var.configs.builders.AbstractVarConfigBuilder;
import com.ringcentral.platform.metrics.var.objectVar.configs.*;

import java.util.List;

public class ObjectVarConfigBuilder extends AbstractVarConfigBuilder<ObjectVarConfig, ObjectVarConfigBuilder> {

    public static ObjectVarConfigBuilder objectVar() {
        return objectVarConfigBuilder();
    }

    public static ObjectVarConfigBuilder withObjectVar() {
        return objectVarConfigBuilder();
    }

    public static ObjectVarConfigBuilder objectVarConfigBuilder() {
        return new ObjectVarConfigBuilder();
    }

    @Override
    protected ObjectVarConfig buildImpl(
        boolean enabled,
        String description,
        LabelValues prefixLabelValues,
        List<Label> labels,
        boolean nonDecreasing,
        MetricContext context) {

        return new DefaultObjectVarConfig(
            enabled,
            description,
            prefixLabelValues,
            labels,
            nonDecreasing,
            context);
    }
}
