package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.counter.CounterInstance;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.measurables.MeasurableValues;
import com.ringcentral.platform.metrics.rate.RateInstance;
import com.ringcentral.platform.metrics.timer.TimerInstance;
import com.ringcentral.platform.metrics.var.VarInstance;
import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.rate.Rate.*;
import static com.ringcentral.platform.metrics.timer.Timer.DURATION_UNIT;
import static com.ringcentral.platform.metrics.var.longVar.LongVar.LONG_VALUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class PrometheusSampleSpecProviderTest {

    static final MetricDimension DIMENSION_1 = new MetricDimension("dimension_1");
    static final MetricDimension DIMENSION_2 = new MetricDimension("dimension_2");

    @Test
    public void providingSampleSpec() {
        PrometheusSampleSpecProvider provider = new PrometheusSampleSpecProvider();
        MeasurableValues measurableValues = mock(MeasurableValues.class);
        when(measurableValues.valueOf(any())).thenReturn(1L);

        // var
        MetricInstance instance = mock(VarInstance.class);

        PrometheusInstanceSampleSpec instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        PrometheusSampleSpec expectedSampleSpec = new PrometheusSampleSpec(true, LONG_VALUE, 1.0);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                LONG_VALUE,
                null),
            expectedSampleSpec);

        // counter
        instance = mock(CounterInstance.class);

        instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        expectedSampleSpec = new PrometheusSampleSpec(true, COUNT, 1.0);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                COUNT,
                null),
            expectedSampleSpec);

        // rate
        instance = mock(RateInstance.class);

        instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        expectedSampleSpec = new PrometheusSampleSpec(true, COUNT, 1.0);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                COUNT,
                null),
            expectedSampleSpec);

        assertNull(provider.sampleSpecFor(
            instanceSampleSpec,
            instance,
            measurableValues,
            MEAN_RATE,
            null));

        assertNull(provider.sampleSpecFor(
            instanceSampleSpec,
            instance,
            measurableValues,
            ONE_MINUTE_RATE,
            null));

        assertNull(provider.sampleSpecFor(
            instanceSampleSpec,
            instance,
            measurableValues,
            FIVE_MINUTES_RATE,
            null));

        assertNull(provider.sampleSpecFor(
            instanceSampleSpec,
            instance,
            measurableValues,
            FIFTEEN_MINUTES_RATE,
            null));

        assertNull(provider.sampleSpecFor(
            instanceSampleSpec,
            instance,
            measurableValues,
            RATE_UNIT,
            null));

        // histogram
        instance = mock(HistogramInstance.class);

        instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        expectedSampleSpec = new PrometheusSampleSpec(true, COUNT, 1.0);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                COUNT,
                null),
            expectedSampleSpec);

        expectedSampleSpec = new PrometheusSampleSpec(true, TOTAL_SUM, 1.0);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                TOTAL_SUM,
                null),
            expectedSampleSpec);

        expectedSampleSpec = new PrometheusSampleSpec(true, MIN, 1.0);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                MIN,
                null),
            expectedSampleSpec);

        expectedSampleSpec = new PrometheusSampleSpec(true, MAX, 1.0);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                MAX,
                null),
            expectedSampleSpec);

        expectedSampleSpec = new PrometheusSampleSpec(true, MEAN, 1.0);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                MEAN,
                null),
            expectedSampleSpec);

        expectedSampleSpec = new PrometheusSampleSpec(true, PERCENTILE_50, 1.0);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                PERCENTILE_50,
                null),
            expectedSampleSpec);

        expectedSampleSpec = new PrometheusSampleSpec(true, PERCENTILE_75, 1.0);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                PERCENTILE_75,
                null),
            expectedSampleSpec);

        expectedSampleSpec = new PrometheusSampleSpec(true, SEC_1_BUCKET, 1.0);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                SEC_1_BUCKET,
                null),
            expectedSampleSpec);

        assertNull(provider.sampleSpecFor(
            instanceSampleSpec,
            instance,
            measurableValues,
            STANDARD_DEVIATION,
            null));

        // timer
        instance = mock(TimerInstance.class);
        when(((TimerInstance)instance).durationUnit()).thenReturn(MILLISECONDS);

        instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        expectedSampleSpec = new PrometheusSampleSpec(true, COUNT, 1.0);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                COUNT,
                null),
            expectedSampleSpec);

        expectedSampleSpec = new PrometheusSampleSpec(true, TOTAL_SUM, 0.001);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                TOTAL_SUM,
                null),
            expectedSampleSpec);

        expectedSampleSpec = new PrometheusSampleSpec(true, MIN, 0.001);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                MIN,
                null),
            expectedSampleSpec);

        expectedSampleSpec = new PrometheusSampleSpec(true, MAX, 0.001);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                MAX,
                null),
            expectedSampleSpec);

        expectedSampleSpec = new PrometheusSampleSpec(true, MEAN, 0.001);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                MEAN,
                null),
            expectedSampleSpec);

        expectedSampleSpec = new PrometheusSampleSpec(true, PERCENTILE_50, 0.001);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                PERCENTILE_50,
                null),
            expectedSampleSpec);

        expectedSampleSpec = new PrometheusSampleSpec(true, PERCENTILE_75, 0.001);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                PERCENTILE_75,
                null),
            expectedSampleSpec);

        expectedSampleSpec = new PrometheusSampleSpec(true, SEC_1_BUCKET, 1.0);

        check(
            provider.sampleSpecFor(
                instanceSampleSpec,
                instance,
                measurableValues,
                SEC_1_BUCKET,
                null),
            expectedSampleSpec);

        assertNull(provider.sampleSpecFor(
            instanceSampleSpec,
            instance,
            measurableValues,
            MEAN_RATE,
            null));

        assertNull(provider.sampleSpecFor(
            instanceSampleSpec,
            instance,
            measurableValues,
            ONE_MINUTE_RATE,
            null));

        assertNull(provider.sampleSpecFor(
            instanceSampleSpec,
            instance,
            measurableValues,
            FIVE_MINUTES_RATE,
            null));

        assertNull(provider.sampleSpecFor(
            instanceSampleSpec,
            instance,
            measurableValues,
            FIFTEEN_MINUTES_RATE,
            null));

        assertNull(provider.sampleSpecFor(
            instanceSampleSpec,
            instance,
            measurableValues,
            RATE_UNIT,
            null));

        assertNull(provider.sampleSpecFor(
            instanceSampleSpec,
            instance,
            measurableValues,
            STANDARD_DEVIATION,
            null));

        assertNull(provider.sampleSpecFor(
            instanceSampleSpec,
            instance,
            measurableValues,
            DURATION_UNIT,
            null));
    }

    public void check(PrometheusSampleSpec actual, PrometheusSampleSpec expected) {
        assertThat(actual.getEnabled(), is(expected.getEnabled()));
        assertThat(actual.measurable(), is(expected.measurable()));
        assertThat(actual.value(), is(expected.value()));
    }
}