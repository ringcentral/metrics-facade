package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import static java.lang.String.*;

public class DefaultSampleMaker implements SampleMaker<
    DefaultSample,
    DefaultSampleSpec,
    DefaultInstanceSampleSpec,
    InstanceSample<DefaultSample>> {

    public static final String DEFAULT_NAME_PARTS_DELIMITER = ".";
    public static final String DEFAULT_MEASURABLE_NAME_DELIMITER = ".";

    private final String namePartsDelimiter;
    private final String measurableNameDelimiter;

    public DefaultSampleMaker() {
        this(
            DEFAULT_NAME_PARTS_DELIMITER,
            DEFAULT_MEASURABLE_NAME_DELIMITER);
    }

    public DefaultSampleMaker(
        String namePartsDelimiter,
        String measurableNameDelimiter) {

        this.namePartsDelimiter = namePartsDelimiter;
        this.measurableNameDelimiter = measurableNameDelimiter;
    }

    @Override
    public DefaultSample makeSample(
        DefaultSampleSpec spec,
        DefaultInstanceSampleSpec instanceSampleSpec,
        InstanceSample<DefaultSample> instanceSample) {

        StringBuilder nameBuilder = new StringBuilder(join(namePartsDelimiter, spec.name()));

        if (spec.hasDimensionValues()) {
            for (MetricDimensionValue dv : spec.dimensionValues()) {
                nameBuilder.append(namePartsDelimiter).append(dv.value());
            }
        }

        if (spec.hasMeasurableName()) {
            nameBuilder.append(measurableNameDelimiter).append(spec.measurableName());
        }

        return new DefaultSample(
            nameBuilder.toString(),
            spec.value(),
            spec.type());
    }
}