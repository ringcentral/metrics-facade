package com.ringcentral.platform.metrics.var.doubleVar;

import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.AbstractVar;
import com.ringcentral.platform.metrics.var.configs.VarConfig;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class AbstractDoubleVar extends AbstractVar<Double> implements DoubleVar {

    public interface DoubleVarInstanceMaker extends InstanceMaker<Double> {}

    public static class DefaultDoubleVarInstanceMaker implements DoubleVarInstanceMaker {

        public static DefaultDoubleVarInstanceMaker INSTANCE = new DefaultDoubleVarInstanceMaker();

        @Override
        public DoubleVarInstance makeInstance(
            MetricName name,
            List<LabelValue> labelValues,
            boolean totalInstance,
            boolean labeledMetricTotalInstance,
            boolean nonDecreasing,
            Measurable valueMeasurable,
            Supplier<Double> valueSupplier) {

            return new DefaultDoubleVarInstance(
                name,
                labelValues,
                totalInstance,
                labeledMetricTotalInstance,
                nonDecreasing,
                valueMeasurable,
                valueSupplier);
        }
    }

    protected AbstractDoubleVar(
        MetricName name,
        VarConfig config,
        Measurable valueMeasurable,
        Supplier<Double> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            valueMeasurable,
            valueSupplier,
            DefaultDoubleVarInstanceMaker.INSTANCE,
            executor);
    }

    protected AbstractDoubleVar(
        MetricName name,
        VarConfig config,
        Measurable valueMeasurable,
        Supplier<Double> valueSupplier,
        InstanceMaker<Double> instanceMaker,
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
