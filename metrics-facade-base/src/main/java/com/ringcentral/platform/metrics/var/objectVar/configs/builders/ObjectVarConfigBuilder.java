package com.ringcentral.platform.metrics.var.objectVar.configs.builders;

import java.util.List;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.builders.*;
import com.ringcentral.platform.metrics.var.objectVar.configs.*;

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
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricContext context) {

        return new DefaultObjectVarConfig(
            enabled,
            prefixDimensionValues,
            dimensions,
            context);
    }
}
