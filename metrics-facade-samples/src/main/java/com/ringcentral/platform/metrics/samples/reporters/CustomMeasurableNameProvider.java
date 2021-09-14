package com.ringcentral.platform.metrics.samples.reporters;

import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.rate.Rate;

public class CustomMeasurableNameProvider extends DefaultMeasurableNameProvider {

    public static final CustomMeasurableNameProvider INSTANCE = new CustomMeasurableNameProvider();

    @Override
    protected String nameForRateInstance(Measurable measurable) {
        if (measurable instanceof Rate.OneMinuteRate) {
            return "rate.1_min";
        } else if (measurable instanceof Rate.FiveMinutesRate) {
            return "rate.5_min";
        } else if (measurable instanceof Rate.FifteenMinutesRate) {
            return "rate.15_min";
        } else {
            return super.nameForRateInstance(measurable);
        }
    }
}
