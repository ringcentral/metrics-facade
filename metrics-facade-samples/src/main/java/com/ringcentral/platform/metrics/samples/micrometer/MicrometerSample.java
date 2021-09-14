package com.ringcentral.platform.metrics.samples.micrometer;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.micrometer.MetricsFacadeMeterRegistry;
import com.ringcentral.platform.metrics.samples.AbstractSample;
import io.micrometer.core.instrument.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SuppressWarnings("ALL")
public class MicrometerSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        MetricRegistry registry = new DropwizardMetricRegistry();
        export(registry);

        MetricsFacadeMeterRegistry mmRegistry = new MetricsFacadeMeterRegistry(registry, Clock.SYSTEM);

        // Gauge
        AtomicLong gaugeValueSupplier = new AtomicLong();
        Gauge gauge = Gauge.builder("gauge.nonDimensional", gaugeValueSupplier::incrementAndGet).register(mmRegistry);

        AtomicLong dimensionalGaugeValueSupplier_1 = new AtomicLong();

        Gauge dimensionalGauge_1 = Gauge.builder("gauge.dimensional", dimensionalGaugeValueSupplier_1::incrementAndGet)
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(mmRegistry);

        AtomicLong dimensionalGaugeValueSupplier_2 = new AtomicLong();

        Gauge dimensionalGauge_2 = Gauge.builder("gauge.dimensional", dimensionalGaugeValueSupplier_2::incrementAndGet)
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(mmRegistry);

        // Counter
        Counter counter = Counter.builder("counter.nonDimensional").register(mmRegistry);
        counter.increment(1);

        Counter dimensionalCounter_1 = Counter.builder("counter.dimensional")
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(mmRegistry);

        dimensionalCounter_1.increment(2);

        Counter dimensionalCounter_2 = Counter.builder("counter.dimensional")
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(mmRegistry);

        dimensionalCounter_2.increment(3);

        // FunctionCounter
        AtomicLong funCounterValueSupplier = new AtomicLong();

        FunctionCounter funCounter = FunctionCounter
            .builder(
                "funCounter.nonDimensional",
                MicrometerSample.class,
                a -> (double)funCounterValueSupplier.incrementAndGet())
            .register(mmRegistry);

        AtomicLong dimensionalFunCounterValueSupplier_1 = new AtomicLong();

        FunctionCounter dimensionalFunCounter_1 = FunctionCounter
            .builder(
                "funCounter.dimensional",
                MicrometerSample.class,
                a -> (double)dimensionalFunCounterValueSupplier_1.incrementAndGet())
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(mmRegistry);

        AtomicLong dimensionalFunCounterValueSupplier_2 = new AtomicLong();

        FunctionCounter dimensionalFunCounter_2 = FunctionCounter
            .builder(
                "funCounter.dimensional",
                MicrometerSample.class,
                a -> (double)dimensionalFunCounterValueSupplier_2.incrementAndGet())
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(mmRegistry);

        // DistributionSummary
        DistributionSummary distributionSummary = DistributionSummary
            .builder("distributionSummary.nonDimensional")
            .register(mmRegistry);

        distributionSummary.record(1.0);
        distributionSummary.record(2.0);

        DistributionSummary dimensionalDistributionSummary_1 = DistributionSummary
            .builder("distributionSummary.dimensional")
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(mmRegistry);

        dimensionalDistributionSummary_1.record(1.0);
        dimensionalDistributionSummary_1.record(2.0);

        DistributionSummary dimensionalDistributionSummary_2 = DistributionSummary
            .builder("distributionSummary.dimensional")
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(mmRegistry);

        dimensionalDistributionSummary_2.record(3.0);
        dimensionalDistributionSummary_2.record(4.0);

        // Timer
        Timer timer = Timer
            .builder("timer.nonDimensional")
            .register(mmRegistry);

        timer.record(1L, MILLISECONDS);
        timer.record(2L, MILLISECONDS);

        Timer dimensionalTimer_1 = Timer
            .builder("timer.dimensional")
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(mmRegistry);

        dimensionalTimer_1.record(1L, MILLISECONDS);
        dimensionalTimer_1.record(2L, MILLISECONDS);

        Timer dimensionalTimer_2 = Timer
            .builder("timer.dimensional")
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(mmRegistry);

        dimensionalTimer_2.record(3L, MILLISECONDS);
        dimensionalTimer_2.record(4L, MILLISECONDS);

        // FunctionTimer
        AtomicLong funTimerCountSupplier = new AtomicLong();
        AtomicLong funTimerTotalTimeSupplier = new AtomicLong();

        FunctionTimer funTimer = FunctionTimer
            .builder(
                "funTimer.nonDimensional",
                MicrometerSample.class,
                a -> funTimerCountSupplier.incrementAndGet(),
                a -> (double)funTimerTotalTimeSupplier.incrementAndGet(),
                MILLISECONDS)
            .register(mmRegistry);

        AtomicLong dimensionalFunTimerCountSupplier_1 = new AtomicLong();
        AtomicLong dimensionalFunTimerTotalTimeSupplier_1 = new AtomicLong();

        FunctionTimer funTimer_1 = FunctionTimer
            .builder(
                "funTimer.dimensional",
                MicrometerSample.class,
                a -> dimensionalFunTimerCountSupplier_1.incrementAndGet(),
                a -> (double)dimensionalFunTimerTotalTimeSupplier_1.incrementAndGet(),
                MILLISECONDS)
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(mmRegistry);

        AtomicLong dimensionalFunTimerCountSupplier_2 = new AtomicLong();
        AtomicLong dimensionalFunTimerTotalTimeSupplier_2 = new AtomicLong();

        FunctionTimer funTimer_2 = FunctionTimer
            .builder(
                "funTimer.dimensional",
                MicrometerSample.class,
                a -> dimensionalFunTimerCountSupplier_2.incrementAndGet(),
                a -> (double)dimensionalFunTimerTotalTimeSupplier_2.incrementAndGet(),
                MILLISECONDS)
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(mmRegistry);

        // LongTaskTimer
        LongTaskTimer longTaskTimer = LongTaskTimer
            .builder("longTaskTimer.nonDimensional")
            .register(mmRegistry);

        longTaskTimer.start();
        longTaskTimer.start();

        LongTaskTimer dimensionalLongTaskTimer_1 = LongTaskTimer
            .builder("longTaskTimer.dimensional")
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(mmRegistry);

        dimensionalLongTaskTimer_1.start();
        dimensionalLongTaskTimer_1.start();

        LongTaskTimer dimensionalLongTaskTimer_2 = LongTaskTimer
            .builder("longTaskTimer.dimensional")
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(mmRegistry);

        dimensionalLongTaskTimer_2.start();
        dimensionalLongTaskTimer_2.start();
        dimensionalLongTaskTimer_2.start();

        // Meter
        AtomicLong dimensionalMeterDurationSupplier_1 = new AtomicLong();
        AtomicLong dimensionalMeterMaxSupplier_1 = new AtomicLong();

        Meter dimensionalMeter_1 = Meter
            .builder(
                "meter.dimensional",
                Meter.Type.OTHER,
                List.of(
                    new Measurement(() -> (double)dimensionalMeterDurationSupplier_1.incrementAndGet(), Statistic.DURATION),
                    new Measurement(() -> (double)dimensionalMeterMaxSupplier_1.incrementAndGet(), Statistic.MAX)))
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(mmRegistry);

        AtomicLong dimensionalMeterDurationSupplier_2 = new AtomicLong();
        AtomicLong dimensionalMeterMaxSupplier_2 = new AtomicLong();

        Meter dimensionalMeter_2 = Meter
            .builder(
                "meter.dimensional",
                Meter.Type.OTHER,
                List.of(
                    new Measurement(() -> (double)dimensionalMeterDurationSupplier_2.incrementAndGet(), Statistic.DURATION),
                    new Measurement(() -> (double)dimensionalMeterMaxSupplier_2.incrementAndGet(), Statistic.MAX)))
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(mmRegistry);

        hang();
    }
}
