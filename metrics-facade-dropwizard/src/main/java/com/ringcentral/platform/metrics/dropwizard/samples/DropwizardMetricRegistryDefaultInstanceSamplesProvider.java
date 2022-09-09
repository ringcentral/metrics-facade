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

import static com.ringcentral.platform.metrics.samples.SampleTypes.DELTA;
import static com.ringcentral.platform.metrics.samples.SampleTypes.INSTANT;
import static org.slf4j.LoggerFactory.getLogger;

public class DropwizardMetricRegistryDefaultInstanceSamplesProvider implements InstanceSamplesProvider<
        DefaultSample,
        DefaultInstanceSample> {

    private static final Logger logger = getLogger(DropwizardMetricRegistryDefaultInstanceSamplesProvider.class);
    private static final double TIMER_FACTOR = 1.0D / (double)TimeUnit.SECONDS.toNanos(1L);

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

                    final var countSample = createSample(name, "count", histogram::getCount, DELTA);
                    defaultInstanceSample.add(countSample);

                    final var snapshot = histogram.getSnapshot();

                    final var meanSample = createSample(name, "mean", snapshot::getMean, INSTANT);
                    defaultInstanceSample.add(meanSample);

                    final var maxSample = createSample(name, "max", snapshot::getMax, INSTANT);
                    defaultInstanceSample.add(maxSample);

                    final var minSample = createSample(name, "min", snapshot::getMin, INSTANT);
                    defaultInstanceSample.add(minSample);

                    final var medianSample = createSample(name, "median", snapshot::getMedian, INSTANT);
                    defaultInstanceSample.add(medianSample);

                    final var stdDevSample = createSample(name, "std_dev", snapshot::getStdDev, INSTANT);
                    defaultInstanceSample.add(stdDevSample);

                    final var percentile75Sample = createSample(name, "75_percentile", snapshot::get75thPercentile, INSTANT);
                    defaultInstanceSample.add(percentile75Sample);

                    final var percentile95Sample = createSample(name, "95_percentile", snapshot::get95thPercentile, INSTANT);
                    defaultInstanceSample.add(percentile95Sample);

                    final var percentile98Sample = createSample(name, "98_percentile", snapshot::get98thPercentile, INSTANT);
                    defaultInstanceSample.add(percentile98Sample);

                    final var percentile99Sample = createSample(name, "99_percentile", snapshot::get99thPercentile, INSTANT);
                    defaultInstanceSample.add(percentile99Sample);

                    final var percentile999Sample = createSample(name, "999_percentile", snapshot::get999thPercentile, INSTANT);
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

                    final var countSample = createSample(name, "count", timer::getCount, DELTA);
                    defaultInstanceSample.add(countSample);

                    final var snapshot = timer.getSnapshot();

                    final var meanSample = createSample(name, "mean", snapshot::getMean, INSTANT, TIMER_FACTOR);
                    defaultInstanceSample.add(meanSample);

                    final var maxSample = createSample(name, "max", () -> (double)snapshot.getMax(), INSTANT, TIMER_FACTOR);
                    defaultInstanceSample.add(maxSample);

                    final var minSample = createSample(name, "min", () -> (double)snapshot.getMin(), INSTANT, TIMER_FACTOR);
                    defaultInstanceSample.add(minSample);

                    final var medianSample = createSample(name, "median", snapshot::getMedian, INSTANT, TIMER_FACTOR);
                    defaultInstanceSample.add(medianSample);

                    final var stdDevSample = createSample(name, "std_dev", snapshot::getStdDev, INSTANT, TIMER_FACTOR);
                    defaultInstanceSample.add(stdDevSample);

                    final var percentile75Sample = createSample(name, "75_percentile", snapshot::get75thPercentile, INSTANT, TIMER_FACTOR);
                    defaultInstanceSample.add(percentile75Sample);

                    final var percentile95Sample = createSample(name, "95_percentile", snapshot::get95thPercentile, INSTANT, TIMER_FACTOR);
                    defaultInstanceSample.add(percentile95Sample);

                    final var percentile98Sample = createSample(name, "98_percentile", snapshot::get98thPercentile, INSTANT, TIMER_FACTOR);
                    defaultInstanceSample.add(percentile98Sample);

                    final var percentile99Sample = createSample(name, "99_percentile", snapshot::get99thPercentile, INSTANT, TIMER_FACTOR);
                    defaultInstanceSample.add(percentile99Sample);

                    final var percentile999Sample = createSample(name, "999_percentile", snapshot::get999thPercentile, INSTANT, TIMER_FACTOR);
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

                    final var totalSample = createSample(name, "total", meter::getCount, DELTA);
                    defaultInstanceSample.add(totalSample);

                    final var oneMinuteRate = createSample(name, "1_minute_rate", meter::getOneMinuteRate, INSTANT);
                    defaultInstanceSample.add(oneMinuteRate);

                    final var fiveMinuteRate = createSample(name, "5_minute_rate", meter::getFiveMinuteRate, INSTANT);
                    defaultInstanceSample.add(fiveMinuteRate);

                    final var fifteenMinuteRate = createSample(name, "15_minute_rate", meter::getFifteenMinuteRate, INSTANT);
                    defaultInstanceSample.add(fifteenMinuteRate);

                    final var meanRate = createSample(name, "mean_rate", meter::getMeanRate, INSTANT);
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
