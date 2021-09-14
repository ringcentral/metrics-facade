package com.ringcentral.platform.metrics.var.objectVar;

import com.ringcentral.platform.metrics.measurables.MeasurableType;
import com.ringcentral.platform.metrics.var.Var;

import static com.ringcentral.platform.metrics.measurables.MeasurableType.*;

public interface ObjectVar extends Var<Object> {
    class ObjectValue implements Value {

        @Override
        public MeasurableType type() {
            return OBJECT;
        }
    }

    ObjectValue OBJECT_VALUE = new ObjectValue();
}
