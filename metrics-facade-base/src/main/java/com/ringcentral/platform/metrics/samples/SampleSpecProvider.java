package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.measurables.MeasurableValues;

public interface SampleSpecProvider<SS extends SampleSpec, ISS extends InstanceSampleSpec> {
    SS sampleSpecFor(
        ISS instanceSampleSpec,
        MetricInstance instance,
        MeasurableValues measurableValues,
        Measurable measurable);
}
