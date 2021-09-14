package com.ringcentral.platform.metrics.spring;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.counter.Counter;
import org.springframework.web.bind.annotation.*;

import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class CounterEndpoint {

    private final Counter counter;

    public CounterEndpoint(MetricRegistry metricRegistry) {
        this.counter = metricRegistry.counter(withName("counter"));
    }

    @RequestMapping(value = "/counter/inc", method = POST)
    public synchronized void incCounter() {
        counter.inc();
    }
}
