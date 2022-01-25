package com.ringcentral.platform.metrics.x.histogram.scale;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.x.histogram.*;
import com.ringcentral.platform.metrics.x.histogram.hdr.configs.HdrXHistogramImplConfig;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class NeverResetScaleXHistogramImpl extends AbstractScaleXHistogramImpl {

    public NeverResetScaleXHistogramImpl(
        HdrXHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor) {

        super(
            config,
            measurables,
            measurementSpec -> new ExtendedImpl(config, measurementSpec, executor),
            executor);
    }

    protected static class ExtendedImpl implements XHistogramImpl {

        protected ExtendedImpl(
            HdrXHistogramImplConfig config,
            MeasurementSpec measurementSpec,
            ScheduledExecutorService executor) {

            //
        }

        @Override
        public void update(long value) {
            //
        }

        @Override
        public XHistogramSnapshot snapshot() {
            return null;
        }
    }
}
