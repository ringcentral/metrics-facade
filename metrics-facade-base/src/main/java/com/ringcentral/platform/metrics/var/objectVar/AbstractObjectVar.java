package com.ringcentral.platform.metrics.var.objectVar;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.AbstractVar;
import com.ringcentral.platform.metrics.var.configs.VarConfig;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class AbstractObjectVar extends AbstractVar<Object> implements ObjectVar {

    public interface ObjectVarInstanceMaker extends InstanceMaker<Object> {}

    public static class DefaultObjectVarInstanceMaker implements ObjectVarInstanceMaker {

        public static DefaultObjectVarInstanceMaker INSTANCE = new DefaultObjectVarInstanceMaker();

        @Override
        public ObjectVarInstance makeInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean nonDecreasing,
            Measurable valueMeasurable,
            Supplier<Object> valueSupplier) {

            return new DefaultObjectVarInstance(
                name,
                dimensionValues,
                totalInstance,
                dimensionalTotalInstance,
                nonDecreasing,
                valueMeasurable,
                valueSupplier);
        }
    }

    protected AbstractObjectVar(
        MetricName name,
        VarConfig config,
        Measurable valueMeasurable,
        Supplier<Object> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            valueMeasurable,
            valueSupplier,
            DefaultObjectVarInstanceMaker.INSTANCE,
            executor);
    }

    protected AbstractObjectVar(
        MetricName name,
        VarConfig config,
        Measurable valueMeasurable,
        Supplier<Object> valueSupplier,
        InstanceMaker<Object> instanceMaker,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            valueMeasurable,
            valueSupplier,
            instanceMaker,
            executor);
    }
}
