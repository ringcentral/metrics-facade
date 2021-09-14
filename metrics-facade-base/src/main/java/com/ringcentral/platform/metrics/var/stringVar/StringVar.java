package com.ringcentral.platform.metrics.var.stringVar;

import com.ringcentral.platform.metrics.measurables.MeasurableType;
import com.ringcentral.platform.metrics.var.Var;

import static com.ringcentral.platform.metrics.measurables.MeasurableType.*;

public interface StringVar extends Var<String> {
    class StringValue implements Value {

        @Override
        public MeasurableType type() {
            return STRING;
        }
    }

    StringValue STRING_VALUE = new StringValue();
}
