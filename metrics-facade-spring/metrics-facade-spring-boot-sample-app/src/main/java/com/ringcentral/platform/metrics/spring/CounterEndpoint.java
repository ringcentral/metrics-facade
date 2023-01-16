package com.ringcentral.platform.metrics.spring;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSampleSpecModsProvider;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusSampleSpecModsProvider;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.forMetricWithName;
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusSampleSpec.sampleSpec;
import static java.lang.Double.parseDouble;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class CounterEndpoint {

    private final Counter counter;
    private final PrometheusInstanceSampleSpecModsProvider instanceSampleSpecModsProvider;
    private final PrometheusSampleSpecModsProvider sampleSpecModsProvider;

    public CounterEndpoint(
        MetricRegistry metricRegistry,
        PrometheusInstanceSampleSpecModsProvider instanceSampleSpecModsProvider,
        PrometheusSampleSpecModsProvider sampleSpecModsProvider) {

        this.counter = metricRegistry.counter(withName("counter"));
        this.instanceSampleSpecModsProvider = instanceSampleSpecModsProvider;
        this.sampleSpecModsProvider = sampleSpecModsProvider;
    }

    @RequestMapping(value = "/counter/inc", method = POST)
    public synchronized void incCounter() {
        counter.inc();
    }

    @RequestMapping(value = "/counter/prometheus/value/{value}", method = POST)
    public synchronized void modifyPrometheusValue(@PathVariable String value) {
        sampleSpecModsProvider.removeMod("testTriggerFor.counter");

        if (!"real".equalsIgnoreCase(value)) {
            sampleSpecModsProvider.addMod(
                "testTriggerFor.counter",
                forMetricWithName("counter"),
                (instanceSampleSpec, instance, measurableValues, measurable, currSpec) ->
                    measurable instanceof Count ? sampleSpec().value(parseDouble(value)) : null);
        }
    }
}
