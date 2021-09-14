package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.counter.CounterInstance;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.RateInstance;
import com.ringcentral.platform.metrics.samples.InstanceSampleMaker;
import com.ringcentral.platform.metrics.timer.TimerInstance;
import com.ringcentral.platform.metrics.var.VarInstance;
import com.ringcentral.platform.metrics.var.longVar.LongVarInstance;
import io.prometheus.client.Collector;
import org.slf4j.Logger;

import static io.prometheus.client.Collector.Type.*;
import static org.slf4j.LoggerFactory.getLogger;

public class PrometheusInstanceSampleMaker implements InstanceSampleMaker<
    PrometheusSample,
    PrometheusInstanceSample,
    PrometheusInstanceSampleSpec> {

    public static final String DEFAULT_TOTAL_INSTANCE_NAME_SUFFIX = null;
    public static final String DEFAULT_DIMENSIONAL_TOTAL_INSTANCE_NAME_SUFFIX = "all";
    public static final boolean DEFAULT_EXPORT_NON_DECREASING_LONG_VAR_AS_COUNTER = true;

    private final String totalInstanceNameSuffix;
    private final String dimensionalTotalInstanceNameSuffix;
    private final boolean exportNonDecreasingLongVarAsCounter;

    private static final Logger logger = getLogger(PrometheusInstanceSampleMaker.class);

    public PrometheusInstanceSampleMaker() {
        this(
            DEFAULT_TOTAL_INSTANCE_NAME_SUFFIX,
            DEFAULT_DIMENSIONAL_TOTAL_INSTANCE_NAME_SUFFIX,
            DEFAULT_EXPORT_NON_DECREASING_LONG_VAR_AS_COUNTER);
    }

    public PrometheusInstanceSampleMaker(
        String totalInstanceNameSuffix,
        String dimensionalTotalInstanceNameSuffix) {

        this(
            totalInstanceNameSuffix,
            dimensionalTotalInstanceNameSuffix,
            DEFAULT_EXPORT_NON_DECREASING_LONG_VAR_AS_COUNTER);
    }

    public PrometheusInstanceSampleMaker(
        String totalInstanceNameSuffix,
        String dimensionalTotalInstanceNameSuffix,
        boolean exportNonDecreasingLongVarAsCounter) {

        this.totalInstanceNameSuffix = totalInstanceNameSuffix;
        this.dimensionalTotalInstanceNameSuffix = dimensionalTotalInstanceNameSuffix;
        this.exportNonDecreasingLongVarAsCounter = exportNonDecreasingLongVarAsCounter;
    }

    @Override
    public PrometheusInstanceSample makeInstanceSample(PrometheusInstanceSampleSpec spec) {
        if (!spec.isEnabled() || !spec.hasInstance() || !spec.hasName()) {
            return null;
        }

        MetricInstance instance = spec.instance();
        MetricName name = spec.name();

        if (instance.isTotalInstance()) {
            if (instance.isDimensionalTotalInstance() && dimensionalTotalInstanceNameSuffix != null) {
                name = name.withNewPart(dimensionalTotalInstanceNameSuffix);
            } else if (totalInstanceNameSuffix != null) {
                name = name.withNewPart(totalInstanceNameSuffix);
            }
        }

        Collector.Type type;

        if (instance instanceof TimerInstance || instance instanceof HistogramInstance) {
            type = SUMMARY;
        } else if (instance instanceof CounterInstance) {
            type = GAUGE;
        } else if (instance instanceof RateInstance) {
            name = name.withNewPart("total");
            type = COUNTER;
        } else if (instance instanceof VarInstance) {
            if (exportNonDecreasingLongVarAsCounter
                && instance instanceof LongVarInstance
                && ((LongVarInstance)instance).isNonDecreasing()) {

                type = COUNTER;
            } else {
                type = GAUGE;
            }
        } else {
            logger.warn("Unsupported metric instance type {}", instance.getClass().getName());
            return null;
        }

        return new PrometheusInstanceSample(
            instance.name(),
            name,
            spec.description(),
            type);
    }
}
