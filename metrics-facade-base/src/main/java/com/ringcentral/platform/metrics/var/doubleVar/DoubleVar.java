package com.ringcentral.platform.metrics.var.doubleVar;

import com.ringcentral.platform.metrics.measurables.MeasurableType;
import com.ringcentral.platform.metrics.var.Var;

import static com.ringcentral.platform.metrics.measurables.MeasurableType.*;

public interface DoubleVar extends Var<Double> {
    class DoubleValue implements Value {

        @Override
        public MeasurableType type() {
            return DOUBLE;
        }
    }

    DoubleValue DOUBLE_VALUE = new DoubleValue();
}
