package com.ringcentral.platform.metrics.micrometer;

import com.ringcentral.platform.metrics.MetricRegistry;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.internal.DefaultMeter;

import java.util.*;

public class MfMeasurementsMeter extends DefaultMeter implements MfMeter {

    private final List<MfDoubleGauge<?>> gauges = new ArrayList<>();

    public MfMeasurementsMeter(
        MetricRegistry mfRegistry,
        Id id,
        Type type,
        Iterable<Measurement> measurements) {

        super(id, type, measurements);

        measurements.forEach(m -> {
            MfDoubleGauge<?> gauge = new MfDoubleGauge<>(
                mfRegistry,
                id.withTag(m.getStatistic()),
                a -> m.getValue(),
                MfMeasurementsMeter.class,
                false);

            this.gauges.add(gauge);
        });
    }

    @Override
    public void meterRemoved() {
        gauges.forEach(MfDoubleGauge::meterRemoved);
    }
}
