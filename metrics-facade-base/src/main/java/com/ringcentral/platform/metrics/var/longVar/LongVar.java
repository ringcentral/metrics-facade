package com.ringcentral.platform.metrics.var.longVar;

import com.ringcentral.platform.metrics.measurables.MeasurableType;
import com.ringcentral.platform.metrics.var.Var;

import static com.ringcentral.platform.metrics.measurables.MeasurableType.*;

public interface LongVar extends Var<Long> {
    class LongValue implements Value {

        @Override
        public MeasurableType type() {
            return LONG;
        }
    }

    LongValue LONG_VALUE = new LongValue();
}
