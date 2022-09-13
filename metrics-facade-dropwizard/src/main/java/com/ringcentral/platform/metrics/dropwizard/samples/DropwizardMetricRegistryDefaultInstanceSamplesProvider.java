package com.ringcentral.platform.metrics.dropwizard.samples;

import com.codahale.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.samples.DefaultInstanceSample;
import com.ringcentral.platform.metrics.samples.DefaultSample;
import com.ringcentral.platform.metrics.samples.InstanceSamplesProvider;
import org.slf4j.Logger;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.ringcentral.platform.metrics.measurables.DefaultMeasurableNameProvider.*;
import static com.ringcentral.platform.metrics.samples.SampleTypes.DELTA;
import static com.ringcentral.platform.metrics.samples.SampleTypes.INSTANT;
import static org.slf4j.LoggerFactory.getLogger;

public class DropwizardMetricRegistryDefaultInstanceSamplesProvider implements InstanceSamplesProvider<
        DefaultSample,
        DefaultInstanceSample> {

    private static final Logger logger = getLogger(DropwizardMetricRegistryDefaultInstanceSamplesProvider.class);
    private static final double DURATION_FACTOR = 1.0D / (double)TimeUnit.MILLISECONDS.toNanos(1L);

    private final com.codahale.metrics.MetricRegistry metricRegistry;

    public DropwizardMetricRegistryDefaultInstanceSamplesProvider(final MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public Set<DefaultInstanceSample> instanceSamples() {
        Set<DefaultInstanceSample> result = new LinkedHashSet<>();

        exportGauges(result);
        exportCounters(result);
        exportHistograms(result);
        exportTimers(result);
        exportMeters(result);

        return result;
    }

    private void exportGauges(final Set<DefaultInstanceSample> result) {
        metricRegistry.getGauges()
                .forEach((name, gauge) -> {
                    final var obj = gauge.getValue();
                    double value;
                    if (obj instanceof Number) {
                        value = ((Number)obj).doubleValue();
                    } else if (obj instanceof Boolean) {
                        value = (Boolean)obj ? 1.0D : 0.0D;
                    } else {
                        logger.trace("Invalid type for Gauge {}: {}", name, obj == null ? "null" : obj.getClass().getName());
                        return;
                    }
                    final var defaultInstanceSample = new DefaultInstanceSample();

                    final var sample = new DefaultSample(name, value, INSTANT);

                    defaultInstanceSample.add(sample);

                    if (!defaultInstanceSample.isEmpty()) {
                        result.add(defaultInstanceSample);
                    }
                });
    }

    private void exportCounters(final Set<DefaultInstanceSample> result) {
        metricRegistry.getCounters()
                .forEach((name, counter) -> {
                    final var value = counter.getCount();

                    final var defaultInstanceSample = new DefaultInstanceSample();
                    final var sample = new DefaultSample(name, value, DELTA);

                    defaultInstanceSample.add(sample);

                    if (!defaultInstanceSample.isEmpty()) {
                        result.add(defaultInstanceSample);
                    }
                });
    }

    private void exportHistograms(final Set<DefaultInstanceSample> result) {
        metricRegistry.getHistograms()
                .forEach((name, histogram) -> {
                    final var defaultInstanceSample = new DefaultInstanceSample();

                    final var countSample = createSample(name, COUNT, histogram::getCount, DELTA);
                    defaultInstanceSample.add(countSample);

                    final var snapshot = histogram.getSnapshot();

                    final var meanSample = createSample(name, MEAN, snapshot::getMean, INSTANT);
                    defaultInstanceSample.add(meanSample);

                    final var maxSample = createSample(name, MAX, snapshot::getMax, INSTANT);
                    defaultInstanceSample.add(maxSample);

                    final var minSample = createSample(name, MIN, snapshot::getMin, INSTANT);
                    defaultInstanceSample.add(minSample);

                    final var medianSample = createSample(name, MEDIAN, snapshot::getMedian, INSTANT);
                    defaultInstanceSample.add(medianSample);

                    final var stdDevSample = createSample(name, STD_DEV, snapshot::getStdDev, INSTANT);
                    defaultInstanceSample.add(stdDevSample);

                    final var percentile75Sample = createSample(name, PERCENTILE_75, snapshot::get75thPercentile, INSTANT);
                    defaultInstanceSample.add(percentile75Sample);

                    final var percentile95Sample = createSample(name, PERCENTILE_95, snapshot::get95thPercentile, INSTANT);
                    defaultInstanceSample.add(percentile95Sample);

                    final var percentile98Sample = createSample(name, PERCENTILE_98, snapshot::get98thPercentile, INSTANT);
                    defaultInstanceSample.add(percentile98Sample);

                    final var percentile99Sample = createSample(name, PERCENTILE_99, snapshot::get99thPercentile, INSTANT);
                    defaultInstanceSample.add(percentile99Sample);

                    final var percentile999Sample = createSample(name, PERCENTILE_999, snapshot::get999thPercentile, INSTANT);
                    defaultInstanceSample.add(percentile999Sample);

                    if (!defaultInstanceSample.isEmpty()) {
                        result.add(defaultInstanceSample);
                    }
                });
    }

    private void exportTimers(final Set<DefaultInstanceSample> result) {
        metricRegistry.getTimers()
                .forEach((name, timer) -> {
                    final var defaultInstanceSample = new DefaultInstanceSample();

                    final var countSample = createSample(name, COUNT, timer::getCount, DELTA);
                    defaultInstanceSample.add(countSample);

                    final var oneMinuteRateSample = createSample(name, RATE_1_MINUTE, timer::getOneMinuteRate, INSTANT, DURATION_FACTOR);
                    defaultInstanceSample.add(oneMinuteRateSample);

                    final var fiveMinutesRateSample = createSample(name, RATE_5_MINUTES, timer::getFiveMinuteRate, INSTANT, DURATION_FACTOR);
                    defaultInstanceSample.add(fiveMinutesRateSample);

                    final var fifteenMinutesRateSample = createSample(name, RATE_15_MINUTES, timer::getFifteenMinuteRate, INSTANT, DURATION_FACTOR);
                    defaultInstanceSample.add(fifteenMinutesRateSample);

                    final var meanRateSample = createSample(name, RATE_MEAN, timer::getMeanRate, INSTANT, DURATION_FACTOR);
                    defaultInstanceSample.add(meanRateSample);

                    final var snapshot = timer.getSnapshot();

                    final var meanSample = createSample(name, DURATION_MEAN, snapshot::getMean, INSTANT, DURATION_FACTOR);
                    defaultInstanceSample.add(meanSample);

                    final var maxSample = createSample(name, DURATION_MAX, () -> (double)snapshot.getMax(), INSTANT, DURATION_FACTOR);
                    defaultInstanceSample.add(maxSample);

                    final var minSample = createSample(name, DURATION_MIN, () -> (double)snapshot.getMin(), INSTANT, DURATION_FACTOR);
                    defaultInstanceSample.add(minSample);

                    final var medianSample = createSample(name, DURATION_MEDIAN, snapshot::getMedian, INSTANT, DURATION_FACTOR);
                    defaultInstanceSample.add(medianSample);

                    final var stdDevSample = createSample(name, DURATION_STD_DEV, snapshot::getStdDev, INSTANT, DURATION_FACTOR);
                    defaultInstanceSample.add(stdDevSample);

                    final var percentile75Sample = createSample(name, DURATION_75_PERCENTILE, snapshot::get75thPercentile, INSTANT, DURATION_FACTOR);
                    defaultInstanceSample.add(percentile75Sample);

                    final var percentile95Sample = createSample(name, DURATION_95_PERCENTILE, snapshot::get95thPercentile, INSTANT, DURATION_FACTOR);
                    defaultInstanceSample.add(percentile95Sample);

                    final var percentile98Sample = createSample(name, DURATION_98_PERCENTILE, snapshot::get98thPercentile, INSTANT, DURATION_FACTOR);
                    defaultInstanceSample.add(percentile98Sample);

                    final var percentile99Sample = createSample(name, DURATION_99_PERCENTILE, snapshot::get99thPercentile, INSTANT, DURATION_FACTOR);
                    defaultInstanceSample.add(percentile99Sample);

                    final var percentile999Sample = createSample(name, DURATION_999_PERCENTILE, snapshot::get999thPercentile, INSTANT, DURATION_FACTOR);
                    defaultInstanceSample.add(percentile999Sample);

                    if (!defaultInstanceSample.isEmpty()) {
                        result.add(defaultInstanceSample);
                    }
                });
    }

    private void exportMeters(final Set<DefaultInstanceSample> result) {
        metricRegistry.getMeters()
                .forEach((name, meter) -> {
                    final var defaultInstanceSample = new DefaultInstanceSample();

                    final var totalSample = createSample(name, COUNT, meter::getCount, DELTA);
                    defaultInstanceSample.add(totalSample);

                    final var oneMinuteRate = createSample(name, RATE_1_MINUTE, meter::getOneMinuteRate, INSTANT);
                    defaultInstanceSample.add(oneMinuteRate);

                    final var fiveMinuteRate = createSample(name, RATE_5_MINUTES, meter::getFiveMinuteRate, INSTANT);
                    defaultInstanceSample.add(fiveMinuteRate);

                    final var fifteenMinuteRate = createSample(name, RATE_15_MINUTES, meter::getFifteenMinuteRate, INSTANT);
                    defaultInstanceSample.add(fifteenMinuteRate);

                    final var meanRate = createSample(name, RATE_MEAN, meter::getMeanRate, INSTANT);
                    defaultInstanceSample.add(meanRate);

                    if (!defaultInstanceSample.isEmpty()) {
                        result.add(defaultInstanceSample);
                    }
                });
    }

    private DefaultSample createSample(String metricName, String measurableName, Supplier<Double> valueSupplier, String type, double factor) {
        return createSample(metricName, measurableName, () -> valueSupplier.get() * factor, type);
    }

    private DefaultSample createSample(String metricName, String measurableName, Supplier<Object> valueSupplier, String type) {
        final var value = valueSupplier.get();
        final var name = MetricName.of(metricName, measurableName).toString();
        return new DefaultSample(name, value, type);
    }
}
