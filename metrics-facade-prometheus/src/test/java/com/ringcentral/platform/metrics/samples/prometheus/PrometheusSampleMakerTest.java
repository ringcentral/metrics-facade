package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.counter.CounterInstance;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.rate.RateInstance;
import com.ringcentral.platform.metrics.timer.TimerInstance;
import com.ringcentral.platform.metrics.var.VarInstance;
import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.rate.Rate.*;
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusSamplesMakerBuilder.prometheusSamplesMakerBuilder;
import static com.ringcentral.platform.metrics.timer.Timer.DURATION_UNIT;
import static com.ringcentral.platform.metrics.var.longVar.LongVar.LONG_VALUE;
import static io.prometheus.client.Collector.Type.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PrometheusSampleMakerTest {

    static final MetricDimension DIMENSION_1 = new MetricDimension("dimension_1");
    static final MetricDimension DIMENSION_2 = new MetricDimension("dimension_2");

    PrometheusInstanceSampleSpec SIMPLE_COUNTER_INSTANCE_SAMPLE_SPEC = new PrometheusInstanceSampleSpec(
        false,
        mock(CounterInstance.class),
        name("name"),
        "description",
        emptyList());

    PrometheusSampleMaker maker = prometheusSamplesMakerBuilder().separateHistogramAndSummary(false).build();

    @Test
    public void sampleDisabled() {
        PrometheusSampleSpec sampleSpec = new PrometheusSampleSpec(false, COUNT, 1.0);
        assertNull(maker.makeSample(sampleSpec, SIMPLE_COUNTER_INSTANCE_SAMPLE_SPEC, null));
    }

    @Test
    public void noMeasurable() {
        PrometheusSampleSpec sampleSpec = new PrometheusSampleSpec(true, null, 1.0);
        assertNull(maker.makeSample(sampleSpec, SIMPLE_COUNTER_INSTANCE_SAMPLE_SPEC, null));
    }

    @Test
    public void noValue() {
        PrometheusSampleSpec sampleSpec = new PrometheusSampleSpec(true, COUNT, null);
        assertNull(maker.makeSample(sampleSpec, SIMPLE_COUNTER_INSTANCE_SAMPLE_SPEC, null));
    }

    @Test
    public void var() {
        MetricInstance instance = mock(VarInstance.class);

        PrometheusInstanceSampleSpec instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        PrometheusSampleSpec sampleSpec = new PrometheusSampleSpec(true, LONG_VALUE, 1.0);

        PrometheusInstanceSample instanceSample = new PrometheusInstanceSample(
            instanceSampleSpec.name(),
            instanceSampleSpec.name(),
            instanceSampleSpec.description(),
            GAUGE);

        PrometheusSample expectedSample = new PrometheusSample(
            LONG_VALUE,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);
    }

    @Test
    public void counter() {
        MetricInstance instance = mock(CounterInstance.class);

        PrometheusInstanceSampleSpec instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        PrometheusInstanceSample instanceSample = new PrometheusInstanceSample(
            instanceSampleSpec.name(),
            instanceSampleSpec.name(),
            instanceSampleSpec.description(),
            GAUGE);

        PrometheusSampleSpec sampleSpec = new PrometheusSampleSpec(true, COUNT, 1.0);

        PrometheusSample expectedSample = new PrometheusSample(
            COUNT,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);
    }

    @Test
    public void rate() {
        MetricInstance instance = mock(RateInstance.class);

        PrometheusInstanceSampleSpec instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        PrometheusInstanceSample instanceSample = new PrometheusInstanceSample(
            instanceSampleSpec.name(),
            instanceSampleSpec.name(),
            instanceSampleSpec.description(),
            COUNTER);

        // COUNT
        PrometheusSampleSpec sampleSpec = new PrometheusSampleSpec(true, COUNT, 1.0);

        PrometheusSample expectedSample = new PrometheusSample(
            COUNT,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MEAN_RATE
        sampleSpec = new PrometheusSampleSpec(true, MEAN_RATE, 1.0);

        expectedSample = new PrometheusSample(
            MEAN_RATE,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // ONE_MINUTE_RATE
        sampleSpec = new PrometheusSampleSpec(true, ONE_MINUTE_RATE, 1.0);

        expectedSample = new PrometheusSample(
            ONE_MINUTE_RATE,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // FIVE_MINUTES_RATE
        sampleSpec = new PrometheusSampleSpec(true, FIVE_MINUTES_RATE, 1.0);

        expectedSample = new PrometheusSample(
            FIVE_MINUTES_RATE,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // FIFTEEN_MINUTES_RATE
        sampleSpec = new PrometheusSampleSpec(true, FIFTEEN_MINUTES_RATE, 1.0);

        expectedSample = new PrometheusSample(
            FIFTEEN_MINUTES_RATE,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // RATE_UNIT
        sampleSpec = new PrometheusSampleSpec(true, RATE_UNIT, 1.0);

        expectedSample = new PrometheusSample(
            RATE_UNIT,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);
    }

    @Test
    public void histogram() {
        MetricInstance instance = mock(HistogramInstance.class);
        when(instance.isWithPercentiles()).thenReturn(true);
        when(instance.isWithBuckets()).thenReturn(true);

        PrometheusInstanceSampleSpec instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        PrometheusInstanceSample instanceSample = new PrometheusInstanceSample(
            instanceSampleSpec.name(),
            instanceSampleSpec.name(),
            instanceSampleSpec.description(),
            HISTOGRAM);

        // COUNT
        PrometheusSampleSpec sampleSpec = new PrometheusSampleSpec(true, COUNT, 1.0);

        PrometheusSample expectedSample = new PrometheusSample(
            COUNT,
            null,
            null,
            null,
            "_count",
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // TOTAL_SUM
        sampleSpec = new PrometheusSampleSpec(true, TOTAL_SUM, 1.0);

        expectedSample = new PrometheusSample(
            TOTAL_SUM,
            null,
            null,
            null,
            "_sum",
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MIN
        sampleSpec = new PrometheusSampleSpec(true, MIN, 1.0);

        expectedSample = new PrometheusSample(
            MIN,
            name("min"),
            GAUGE,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MAX
        sampleSpec = new PrometheusSampleSpec(true, MAX, 1.0);

        expectedSample = new PrometheusSample(
            MAX,
            name("max"),
            GAUGE,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MEAN
        sampleSpec = new PrometheusSampleSpec(true, MEAN, 1.0);

        expectedSample = new PrometheusSample(
            MEAN,
            name("mean"),
            GAUGE,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // STANDARD_DEVIATION
        sampleSpec = new PrometheusSampleSpec(true, STANDARD_DEVIATION, 1.0);

        expectedSample = new PrometheusSample(
            STANDARD_DEVIATION,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // PERCENTILE_50
        sampleSpec = new PrometheusSampleSpec(true, PERCENTILE_50, 1.0);

        expectedSample = new PrometheusSample(
            PERCENTILE_50,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "quantile"),
            List.of("value_1", "value_2", "0.5"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // PERCENTILE_75
        sampleSpec = new PrometheusSampleSpec(true, PERCENTILE_75, 1.0);

        expectedSample = new PrometheusSample(
            PERCENTILE_75,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "quantile"),
            List.of("value_1", "value_2", "0.75"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // SEC_1_BUCKET
        sampleSpec = new PrometheusSampleSpec(true, SEC_1_BUCKET, 1.0);

        expectedSample = new PrometheusSample(
            SEC_1_BUCKET,
            null,
            null,
            null,
            "_bucket",
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "le"),
            List.of("value_1", "value_2", "1.0E9"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // +Inf bucket
        sampleSpec = new PrometheusSampleSpec(true, INF_BUCKET, 1.0);

        expectedSample = new PrometheusSample(
            SEC_1_BUCKET,
            null,
            null,
            null,
            "_bucket",
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "le"),
            List.of("value_1", "value_2", "+Inf"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // -Inf bucket
        sampleSpec = new PrometheusSampleSpec(true, NEGATIVE_INF_BUCKET, 1.0);

        expectedSample = new PrometheusSample(
            SEC_1_BUCKET,
            null,
            null,
            null,
            "_bucket",
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "le"),
            List.of("value_1", "value_2", "-Inf"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);
    }

    @Test
    public void histogram_NoDimensions() {
        MetricInstance instance = mock(HistogramInstance.class);
        when(instance.isWithPercentiles()).thenReturn(true);
        when(instance.isWithBuckets()).thenReturn(true);

        PrometheusInstanceSampleSpec instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            emptyList());

        PrometheusInstanceSample instanceSample = new PrometheusInstanceSample(
            instanceSampleSpec.name(),
            instanceSampleSpec.name(),
            instanceSampleSpec.description(),
            HISTOGRAM);

        // COUNT
        PrometheusSampleSpec sampleSpec = new PrometheusSampleSpec(true, COUNT, 1.0);

        PrometheusSample expectedSample = new PrometheusSample(
            COUNT,
            null,
            null,
            null,
            "_count",
            emptyList(),
            emptyList(),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // TOTAL_SUM
        sampleSpec = new PrometheusSampleSpec(true, TOTAL_SUM, 1.0);

        expectedSample = new PrometheusSample(
            TOTAL_SUM,
            null,
            null,
            null,
            "_sum",
            emptyList(),
            emptyList(),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MIN
        sampleSpec = new PrometheusSampleSpec(true, MIN, 1.0);

        expectedSample = new PrometheusSample(
            MIN,
            name("min"),
            GAUGE,
            null,
            null,
            emptyList(),
            emptyList(),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MAX
        sampleSpec = new PrometheusSampleSpec(true, MAX, 1.0);

        expectedSample = new PrometheusSample(
            MAX,
            name("max"),
            GAUGE,
            null,
            null,
            emptyList(),
            emptyList(),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MEAN
        sampleSpec = new PrometheusSampleSpec(true, MEAN, 1.0);

        expectedSample = new PrometheusSample(
            MEAN,
            name("mean"),
            GAUGE,
            null,
            null,
            emptyList(),
            emptyList(),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // STANDARD_DEVIATION
        sampleSpec = new PrometheusSampleSpec(true, STANDARD_DEVIATION, 1.0);

        expectedSample = new PrometheusSample(
            STANDARD_DEVIATION,
            null,
            null,
            null,
            null,
            emptyList(),
            emptyList(),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // PERCENTILE_50
        sampleSpec = new PrometheusSampleSpec(true, PERCENTILE_50, 1.0);

        expectedSample = new PrometheusSample(
            PERCENTILE_50,
            null,
            null,
            null,
            null,
            List.of("quantile"),
            List.of("0.5"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // PERCENTILE_75
        sampleSpec = new PrometheusSampleSpec(true, PERCENTILE_75, 1.0);

        expectedSample = new PrometheusSample(
            PERCENTILE_75,
            null,
            null,
            null,
            null,
            List.of("quantile"),
            List.of("0.75"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // SEC_1_BUCKET
        sampleSpec = new PrometheusSampleSpec(true, SEC_1_BUCKET, 1.0);

        expectedSample = new PrometheusSample(
            SEC_1_BUCKET,
            null,
            null,
            null,
            "_bucket",
            List.of("le"),
            List.of("1.0E9"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);
    }

    @Test
    public void timer() {
        maker = prometheusSamplesMakerBuilder()
            .separateHistogramAndSummary(false)
            .minChildInstanceSampleNameSuffix(name("test", "min"))
            .maxChildInstanceSampleNameSuffix(name("test", "max"))
            .meanChildInstanceSampleNameSuffix(name("test", "mean"))
            .build();

        MetricInstance instance = mock(TimerInstance.class);
        when(instance.isWithPercentiles()).thenReturn(true);
        when(instance.isWithBuckets()).thenReturn(true);

        PrometheusInstanceSampleSpec instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        PrometheusInstanceSample instanceSample = new PrometheusInstanceSample(
            instanceSampleSpec.name(),
            instanceSampleSpec.name(),
            instanceSampleSpec.description(),
            HISTOGRAM);

        // COUNT
        PrometheusSampleSpec sampleSpec = new PrometheusSampleSpec(true, COUNT, 1.0);

        PrometheusSample expectedSample = new PrometheusSample(
            COUNT,
            null,
            null,
            null,
            "_count",
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // TOTAL_SUM
        sampleSpec = new PrometheusSampleSpec(true, TOTAL_SUM, 1.0);

        expectedSample = new PrometheusSample(
            TOTAL_SUM,
            null,
            null,
            null,
            "_sum",
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MEAN_RATE
        sampleSpec = new PrometheusSampleSpec(true, MEAN_RATE, 1.0);

        expectedSample = new PrometheusSample(
            MEAN_RATE,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // ONE_MINUTE_RATE
        sampleSpec = new PrometheusSampleSpec(true, ONE_MINUTE_RATE, 1.0);

        expectedSample = new PrometheusSample(
            ONE_MINUTE_RATE,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // FIVE_MINUTES_RATE
        sampleSpec = new PrometheusSampleSpec(true, FIVE_MINUTES_RATE, 1.0);

        expectedSample = new PrometheusSample(
            FIVE_MINUTES_RATE,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // FIFTEEN_MINUTES_RATE
        sampleSpec = new PrometheusSampleSpec(true, FIFTEEN_MINUTES_RATE, 1.0);

        expectedSample = new PrometheusSample(
            FIFTEEN_MINUTES_RATE,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // RATE_UNIT
        sampleSpec = new PrometheusSampleSpec(true, RATE_UNIT, 1.0);

        expectedSample = new PrometheusSample(
            RATE_UNIT,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MIN
        sampleSpec = new PrometheusSampleSpec(true, MIN, 1.0);

        expectedSample = new PrometheusSample(
            MIN,
            name("test", "min"),
            GAUGE,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MAX
        sampleSpec = new PrometheusSampleSpec(true, MAX, 1.0);

        expectedSample = new PrometheusSample(
            MAX,
            name("test", "max"),
            GAUGE,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MEAN
        sampleSpec = new PrometheusSampleSpec(true, MEAN, 1.0);

        expectedSample = new PrometheusSample(
            MEAN,
            name("test", "mean"),
            GAUGE,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // STANDARD_DEVIATION
        sampleSpec = new PrometheusSampleSpec(true, STANDARD_DEVIATION, 1.0);

        expectedSample = new PrometheusSample(
            STANDARD_DEVIATION,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // PERCENTILE_50
        sampleSpec = new PrometheusSampleSpec(true, PERCENTILE_50, 1.0);

        expectedSample = new PrometheusSample(
            PERCENTILE_50,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "quantile"),
            List.of("value_1", "value_2", "0.5"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // PERCENTILE_75
        sampleSpec = new PrometheusSampleSpec(true, PERCENTILE_75, 1.0);

        expectedSample = new PrometheusSample(
            PERCENTILE_75,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "quantile"),
            List.of("value_1", "value_2", "0.75"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // DURATION_UNIT
        sampleSpec = new PrometheusSampleSpec(true, DURATION_UNIT, 1.0);

        expectedSample = new PrometheusSample(
            DURATION_UNIT,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // SEC_1_BUCKET
        sampleSpec = new PrometheusSampleSpec(true, SEC_1_BUCKET, 1.0);

        expectedSample = new PrometheusSample(
            SEC_1_BUCKET,
            null,
            null,
            null,
            "_bucket",
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "le"),
            List.of("value_1", "value_2", "1"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);
    }

    @Test
    public void separatingSummaryFromHistogram() {
        maker = prometheusSamplesMakerBuilder()
            .separateHistogramAndSummary(true)
            .histogramChildInstanceSampleNameSuffix(name("test", "histogram"))
            .summaryChildInstanceSampleNameSuffix(name("test", "summary"))
            .build();

        MetricInstance instance = mock(HistogramInstance.class);
        when(instance.isWithPercentiles()).thenReturn(true);
        when(instance.isWithBuckets()).thenReturn(true);

        PrometheusInstanceSampleSpec instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        PrometheusInstanceSample instanceSample = new PrometheusInstanceSample(
            instanceSampleSpec.name(),
            instanceSampleSpec.name(),
            instanceSampleSpec.description(),
            HISTOGRAM);

        // COUNT
        PrometheusSampleSpec sampleSpec = new PrometheusSampleSpec(true, COUNT, 1.0);

        PrometheusSample expectedChildSample = new PrometheusSample(
            COUNT,
            name("test", "summary"),
            SUMMARY,
            null,
            "_count",
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        PrometheusSample expectedSample = new PrometheusSample(
            COUNT,
            null,
            null,
            null,
            "_count",
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0,
            List.of(expectedChildSample));

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // TOTAL_SUM
        sampleSpec = new PrometheusSampleSpec(true, TOTAL_SUM, 1.0);

        expectedChildSample = new PrometheusSample(
            COUNT,
            name("test", "summary"),
            SUMMARY,
            null,
            "_sum",
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        expectedSample = new PrometheusSample(
            TOTAL_SUM,
            null,
            null,
            null,
            "_sum",
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0,
            List.of(expectedChildSample));

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MIN
        sampleSpec = new PrometheusSampleSpec(true, MIN, 1.0);

        expectedSample = new PrometheusSample(
            MIN,
            name("min"),
            GAUGE,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MAX
        sampleSpec = new PrometheusSampleSpec(true, MAX, 1.0);

        expectedSample = new PrometheusSample(
            MAX,
            name("max"),
            GAUGE,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MEAN
        sampleSpec = new PrometheusSampleSpec(true, MEAN, 1.0);

        expectedSample = new PrometheusSample(
            MEAN,
            name("mean"),
            GAUGE,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // STANDARD_DEVIATION
        sampleSpec = new PrometheusSampleSpec(true, STANDARD_DEVIATION, 1.0);

        expectedSample = new PrometheusSample(
            STANDARD_DEVIATION,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // PERCENTILE_50
        sampleSpec = new PrometheusSampleSpec(true, PERCENTILE_50, 1.0);

        expectedSample = new PrometheusSample(
            PERCENTILE_50,
            name("test", "summary"),
            SUMMARY,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "quantile"),
            List.of("value_1", "value_2", "0.5"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // PERCENTILE_75
        sampleSpec = new PrometheusSampleSpec(true, PERCENTILE_75, 1.0);

        expectedSample = new PrometheusSample(
            PERCENTILE_75,
            name("test", "summary"),
            SUMMARY,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "quantile"),
            List.of("value_1", "value_2", "0.75"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // SEC_1_BUCKET
        sampleSpec = new PrometheusSampleSpec(true, SEC_1_BUCKET, 1.0);

        expectedSample = new PrometheusSample(
            SEC_1_BUCKET,
            null,
            null,
            null,
            "_bucket",
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "le"),
            List.of("value_1", "value_2", "1.0E9"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);
    }

    @Test
    public void separatingHistogramFromSummary() {
        maker = prometheusSamplesMakerBuilder()
            .separateHistogramAndSummary(true)
            .histogramChildInstanceSampleNameSuffix(name("test", "histogram"))
            .summaryChildInstanceSampleNameSuffix(name("test", "summary"))
            .build();

        MetricInstance instance = mock(HistogramInstance.class);
        when(instance.isWithPercentiles()).thenReturn(true);
        when(instance.isWithBuckets()).thenReturn(true);

        PrometheusInstanceSampleSpec instanceSampleSpec = new PrometheusInstanceSampleSpec(
            true,
            instance,
            name("a", "b"),
            "Description for " + name("a", "b"),
            List.of(DIMENSION_1.value("value_1"), DIMENSION_2.value("value_2")));

        PrometheusInstanceSample instanceSample = new PrometheusInstanceSample(
            instanceSampleSpec.name(),
            instanceSampleSpec.name(),
            instanceSampleSpec.description(),
            SUMMARY);

        // COUNT
        PrometheusSampleSpec sampleSpec = new PrometheusSampleSpec(true, COUNT, 1.0);

        PrometheusSample expectedChildSample = new PrometheusSample(
            COUNT,
            name("test", "histogram"),
            HISTOGRAM,
            null,
            "_count",
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        PrometheusSample expectedSample = new PrometheusSample(
            COUNT,
            null,
            null,
            null,
            "_count",
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0,
            List.of(expectedChildSample));

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // TOTAL_SUM
        sampleSpec = new PrometheusSampleSpec(true, TOTAL_SUM, 1.0);

        expectedChildSample = new PrometheusSample(
            COUNT,
            name("test", "histogram"),
            HISTOGRAM,
            null,
            "_sum",
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        expectedSample = new PrometheusSample(
            TOTAL_SUM,
            null,
            null,
            null,
            "_sum",
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0,
            List.of(expectedChildSample));

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MIN
        sampleSpec = new PrometheusSampleSpec(true, MIN, 1.0);

        expectedSample = new PrometheusSample(
            MIN,
            name("min"),
            GAUGE,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MAX
        sampleSpec = new PrometheusSampleSpec(true, MAX, 1.0);

        expectedSample = new PrometheusSample(
            MAX,
            name("max"),
            GAUGE,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // MEAN
        sampleSpec = new PrometheusSampleSpec(true, MEAN, 1.0);

        expectedSample = new PrometheusSample(
            MEAN,
            name("mean"),
            GAUGE,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // STANDARD_DEVIATION
        sampleSpec = new PrometheusSampleSpec(true, STANDARD_DEVIATION, 1.0);

        expectedSample = new PrometheusSample(
            STANDARD_DEVIATION,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name()),
            List.of("value_1", "value_2"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // PERCENTILE_50
        sampleSpec = new PrometheusSampleSpec(true, PERCENTILE_50, 1.0);

        expectedSample = new PrometheusSample(
            PERCENTILE_50,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "quantile"),
            List.of("value_1", "value_2", "0.5"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // PERCENTILE_75
        sampleSpec = new PrometheusSampleSpec(true, PERCENTILE_75, 1.0);

        expectedSample = new PrometheusSample(
            PERCENTILE_75,
            null,
            null,
            null,
            null,
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "quantile"),
            List.of("value_1", "value_2", "0.75"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);

        // SEC_1_BUCKET
        sampleSpec = new PrometheusSampleSpec(true, SEC_1_BUCKET, 1.0);

        expectedSample = new PrometheusSample(
            SEC_1_BUCKET,
            name("test", "histogram"),
            HISTOGRAM,
            null,
            "_bucket",
            List.of(DIMENSION_1.name(), DIMENSION_2.name(), "le"),
            List.of("value_1", "value_2", "1.0E9"),
            1.0);

        check(maker.makeSample(sampleSpec, instanceSampleSpec, instanceSample), expectedSample);
    }

    public void check(PrometheusSample actual, PrometheusSample expected) {
        assertThat(actual.childInstanceSampleNameSuffix(), is(expected.childInstanceSampleNameSuffix()));
        assertThat(actual.childInstanceSampleType(), is(expected.childInstanceSampleType()));
        assertThat(actual.nameSuffix(), is(expected.nameSuffix()));
        assertThat(actual.labelNames(), is(expected.labelNames()));
        assertThat(actual.labelValues(), is(expected.labelValues()));
        assertThat(actual.value(), is(expected.value()));

        assertThat(actual.hasChildren(), is(expected.hasChildren()));
        assertThat(actual.children().size(), is(expected.children().size()));

        for (int i = 0; i < actual.children().size(); ++i) {
            check(actual.children().get(i), expected.children().get(i));
        }
    }
}