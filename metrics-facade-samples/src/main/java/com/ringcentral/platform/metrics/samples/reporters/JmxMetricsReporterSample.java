package com.ringcentral.platform.metrics.samples.reporters;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.infoProviders.MaskTreeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.reporters.jmx.*;
import com.ringcentral.platform.metrics.samples.AbstractSample;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.forDimensionValues;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.*;
import static com.ringcentral.platform.metrics.predicates.DefaultMetricInstancePredicate.forMetricInstancesMatching;
import static com.ringcentral.platform.metrics.rate.Rate.*;
import static com.ringcentral.platform.metrics.rate.configs.builders.RateConfigBuilder.withRate;
import static com.ringcentral.platform.metrics.reporters.jmx.MBeanSpec.mBeanSpec;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;

@SuppressWarnings("ALL")
public class JmxMetricsReporterSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new DefaultMetricRegistry();

        // Default config
        // registry.addListener(new JmxMetricsReporter());

        MaskTreeMetricNamedInfoProvider<MBeanSpecProvider> mBeanSpecs = new MaskTreeMetricNamedInfoProvider<>();

        mBeanSpecs.addInfo(
            forMetricInstancesMatching(
                nameMask("rate.**"),
                instance -> "service_2".equals(instance.valueOf(SERVICE))),
            instance -> mBeanSpec().disable());

        mBeanSpecs.addInfo(
            forMetricsWithNamePrefix("rate"),
            instance -> mBeanSpec()
                .name(
                    instance.isTotalInstance() ?
                    instance.name() :
                    instance.name().withNewPart(instance.valueOf(SERVICE)))
                .dimensionValues(instance.dimensionValuesWithout(SERVICE)));

        JmxMetricsReporter jmxReporter = new JmxMetricsReporter(
            mBeanSpecs,
            getPlatformMBeanServer(),
            new DefaultObjectNameProvider(),
            new CustomMeasurableNameProvider(),
            "JmxMetricsReporterSample");

        registry.addListener(jmxReporter);

        Rate r = registry.rate(
            withName("rate"),
            () -> withRate()
                .dimensions(SERVICE, SERVER, PORT)
                .measurables(COUNT, MEAN_RATE, ONE_MINUTE_RATE));

        r.mark(1, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
        r.mark(2, forDimensionValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));
        r.mark(3, forDimensionValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));

        hang();
    }
}
