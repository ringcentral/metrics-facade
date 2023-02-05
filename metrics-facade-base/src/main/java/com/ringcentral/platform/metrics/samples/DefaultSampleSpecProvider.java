package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.counter.*;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.measurables.*;

import java.util.List;

import static com.ringcentral.platform.metrics.samples.SampleTypes.*;
import static java.lang.Boolean.*;

public class DefaultSampleSpecProvider implements SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec> {

    private final MeasurableNameProvider measurableNameProvider;

    public DefaultSampleSpecProvider() {
        this(DefaultMeasurableNameProvider.INSTANCE);
    }

    public DefaultSampleSpecProvider(MeasurableNameProvider measurableNameProvider) {
        this.measurableNameProvider = measurableNameProvider;
    }

    @Override
    public DefaultSampleSpec sampleSpecFor(
        DefaultInstanceSampleSpec instanceSampleSpec,
        MetricInstance instance,
        MeasurableValues measurableValues,
        Measurable measurable,
        DefaultSampleSpec currSpec) {

        List<LabelValue> labelValues = instanceSampleSpec.labelValues();

        if (labelValues == null) {
            labelValues = instance.labelValues();
        }

        return new DefaultSampleSpec(
            TRUE,
            instanceSampleSpec.hasName() ? instanceSampleSpec.name() : instance.name(),
            instanceSampleSpec.isWithMeasurableName() ? measurableNameProvider.nameFor(instance, measurable) : null,
            labelValues,
            measurableValues.valueOf(measurable),
            typeFor(instance, measurable));
    }

    protected String typeFor(MetricInstance instance, Measurable measurable) {
        return
            !(instance instanceof CounterInstance) && measurable instanceof Counter.Count ?
            DELTA :
            INSTANT;
    }
}
