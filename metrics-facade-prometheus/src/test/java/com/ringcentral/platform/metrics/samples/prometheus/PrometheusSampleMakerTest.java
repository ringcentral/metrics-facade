package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.counter.CounterInstance;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.rate.RateInstance;
import com.ringcentral.platform.metrics.timer.TimerInstance;
import com.ringcentral.platform.metrics.var.VarInstance;
import io.prometheus.client.Collector;
import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.rate.Rate.*;
import static com.ringcentral.platform.metrics.timer.Timer.DURATION_UNIT;
import static com.ringcentral.platform.metrics.var.longVar.LongVar.LONG_VALUE;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class PrometheusSampleMakerTest {

    static final MetricDimension DIMENSION_1 = new MetricDimension("dimension_1");
    static final MetricDimension DIMENSION_2 = new MetricDimension("dimension_2");

    @Test
    public void makingSample() {
        PrometheusSampleMaker maker = new PrometheusSampleMaker();

        PrometheusInstanceSampleSpec instanceSampleSpec = new PrometheusInstanceSampleSpec(
            false,
            mock(CounterInstance.class),
            name("a", "b"),
            "Description for " + name("a", "b"),
            emptyList());

        PrometheusSampleSpec sampleSpec = new PrometheusSampleSpec(false, COUNT, 1.0);
        assertNull(maker.makeSample(sampleSpec, instanceSampleSpec));

        sampleSpec = new PrometheusSampleSpec(true, null, 1.0);
        assertNull(maker.makeSample(sampleSpec, instanceSampleSpec));

        sampleSpec = new PrometheusSampleSpec(true, COUNT, null);
        assertNull(maker.makeSample(sampleSpec, instanceSampleSpec));

        // var
        MetricInstance instance = mock(VarInstance.class);

        instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        sampleSpec = new PrometheusSampleSpec(true, LONG_VALUE, 1.0);

        PrometheusSample expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        // counter
        instance = mock(CounterInstance.class);

        instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        sampleSpec = new PrometheusSampleSpec(true, COUNT, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        // rate
        instance = mock(RateInstance.class);

        instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        sampleSpec = new PrometheusSampleSpec(true, COUNT, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, MEAN_RATE, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, ONE_MINUTE_RATE, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, FIFTEEN_MINUTES_RATE, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, FIFTEEN_MINUTES_RATE, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, RATE_UNIT, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        // histogram
        instance = mock(HistogramInstance.class);

        instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        sampleSpec = new PrometheusSampleSpec(true, COUNT, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            "_count",
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, TOTAL_SUM, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            "_sum",
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, MIN, 1.0);

        expectedSample = new PrometheusSample(
            name("min"),
            Collector.Type.GAUGE,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, MAX, 1.0);

        expectedSample = new PrometheusSample(
            name("max"),
            Collector.Type.GAUGE,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, MEAN, 1.0);

        expectedSample = new PrometheusSample(
            name("mean"),
            Collector.Type.GAUGE,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, STANDARD_DEVIATION, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, PERCENTILE_50, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "quantile"),
            List.of("value_1", "value_2", "0.5"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, PERCENTILE_75, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "quantile"),
            List.of("value_1", "value_2", "0.75"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, SEC_1_BUCKET, 1.0);

        expectedSample = new PrometheusSample(
            name("bucket"),
            Collector.Type.HISTOGRAM,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "le"),
            List.of("value_1", "value_2", "1p0E9"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        // timer
        instance = mock(TimerInstance.class);

        instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        sampleSpec = new PrometheusSampleSpec(true, COUNT, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            "_count",
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, TOTAL_SUM, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            "_sum",
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, MEAN_RATE, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, ONE_MINUTE_RATE, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, FIFTEEN_MINUTES_RATE, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, FIFTEEN_MINUTES_RATE, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, RATE_UNIT, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, MIN, 1.0);

        expectedSample = new PrometheusSample(
            name("min"),
            Collector.Type.GAUGE,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, MAX, 1.0);

        expectedSample = new PrometheusSample(
            name("max"),
            Collector.Type.GAUGE,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, MEAN, 1.0);

        expectedSample = new PrometheusSample(
            name("mean"),
            Collector.Type.GAUGE,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, STANDARD_DEVIATION, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, PERCENTILE_50, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "quantile"),
            List.of("value_1", "value_2", "0.5"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, PERCENTILE_75, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "quantile"),
            List.of("value_1", "value_2", "0.75"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, DURATION_UNIT, 1.0);

        expectedSample = new PrometheusSample(
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);

        sampleSpec = new PrometheusSampleSpec(true, SEC_1_BUCKET, 1.0);

        expectedSample = new PrometheusSample(
            name("bucket"),
            Collector.Type.HISTOGRAM,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "le"),
            List.of("value_1", "value_2", "1.0"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec), expectedSample);
    }

    public void check(PrometheusSample actual, PrometheusSample expected) {
        assertThat(actual.childInstanceSampleNameSuffix(), is(expected.childInstanceSampleNameSuffix()));
        assertThat(actual.childInstanceSampleType(), is(expected.childInstanceSampleType()));
        assertThat(actual.nameSuffix(), is(expected.nameSuffix()));
        assertThat(actual.labelNames(), is(expected.labelNames()));
        assertThat(actual.labelValues(), is(expected.labelValues()));
        assertThat(actual.value(), is(expected.value()));
    }
}