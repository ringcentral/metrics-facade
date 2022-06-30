package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.*;

public class DefaultInstanceSampleSpecProvider implements InstanceSampleSpecProvider<DefaultInstanceSampleSpec> {

    @Override
    public DefaultInstanceSampleSpec instanceSampleSpecFor(
        Metric metric,
        MetricInstance instance,
        DefaultInstanceSampleSpec currSpec) {

        return new DefaultInstanceSampleSpec(
            Boolean.TRUE,
            instance.name(),
            instance.dimensionValues(),
            Boolean.TRUE);
    }
}
