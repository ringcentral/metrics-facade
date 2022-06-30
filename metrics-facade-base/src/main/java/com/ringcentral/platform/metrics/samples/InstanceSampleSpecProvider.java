package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.*;

public interface InstanceSampleSpecProvider<ISS extends InstanceSampleSpec> {
    ISS instanceSampleSpecFor(Metric metric, MetricInstance instance, ISS currSpec);
}
