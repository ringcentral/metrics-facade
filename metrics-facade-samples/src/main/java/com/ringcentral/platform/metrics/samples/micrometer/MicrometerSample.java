package com.ringcentral.platform.metrics.samples.micrometer;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.micrometer.MfMeterRegistry;
import com.ringcentral.platform.metrics.samples.AbstractSample;
import io.micrometer.core.instrument.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SuppressWarnings("ALL")
public class MicrometerSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new DefaultMetricRegistry();
        export(registry);

        MfMeterRegistry mmRegistry = new MfMeterRegistry(registry, Clock.SYSTEM);

        // Gauge
        AtomicLong gaugeValueSupplier = new AtomicLong();
        Gauge gauge = Gauge.builder("gauge.nonLabeled", gaugeValueSupplier::incrementAndGet).register(mmRegistry);

        AtomicLong labeledGaugeValueSupplier_1 = new AtomicLong();

        Gauge labeledGauge_1 = Gauge.builder("gauge.labeled", labeledGaugeValueSupplier_1::incrementAndGet)
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(mmRegistry);

        AtomicLong labeledGaugeValueSupplier_2 = new AtomicLong();

        Gauge labeledGauge_2 = Gauge.builder("gauge.labeled", labeledGaugeValueSupplier_2::incrementAndGet)
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(mmRegistry);

        // Counter
        Counter counter = Counter.builder("counter.nonLabeled").register(mmRegistry);
        counter.increment(1);

        Counter labeledCounter_1 = Counter.builder("counter.labeled")
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(mmRegistry);

        labeledCounter_1.increment(2);

        Counter labeledCounter_2 = Counter.builder("counter.labeled")
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(mmRegistry);

        labeledCounter_2.increment(3);

        // FunctionCounter
        AtomicLong funCounterValueSupplier = new AtomicLong();

        FunctionCounter funCounter = FunctionCounter
            .builder(
                "funCounter.nonLabeled",
                MicrometerSample.class,
                a -> (double)funCounterValueSupplier.incrementAndGet())
            .register(mmRegistry);

        AtomicLong labeledFunCounterValueSupplier_1 = new AtomicLong();

        FunctionCounter labeledFunCounter_1 = FunctionCounter
            .builder(
                "funCounter.labeled",
                MicrometerSample.class,
                a -> (double)labeledFunCounterValueSupplier_1.incrementAndGet())
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(mmRegistry);

        AtomicLong labeledFunCounterValueSupplier_2 = new AtomicLong();

        FunctionCounter labeledFunCounter_2 = FunctionCounter
            .builder(
                "funCounter.labeled",
                MicrometerSample.class,
                a -> (double)labeledFunCounterValueSupplier_2.incrementAndGet())
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(mmRegistry);

        // DistributionSummary
        DistributionSummary distributionSummary = DistributionSummary
            .builder("distributionSummary.nonLabeled")
            .register(mmRegistry);

        distributionSummary.record(1.0);
        distributionSummary.record(2.0);

        DistributionSummary labeledDistributionSummary_1 = DistributionSummary
            .builder("distributionSummary.labeled")
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(mmRegistry);

        labeledDistributionSummary_1.record(1.0);
        labeledDistributionSummary_1.record(2.0);

        DistributionSummary labeledDistributionSummary_2 = DistributionSummary
            .builder("distributionSummary.labeled")
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(mmRegistry);

        labeledDistributionSummary_2.record(3.0);
        labeledDistributionSummary_2.record(4.0);

        // Timer
        Timer timer = Timer
            .builder("timer.nonLabeled")
            .register(mmRegistry);

        timer.record(1L, MILLISECONDS);
        timer.record(2L, MILLISECONDS);

        Timer labeledTimer_1 = Timer
            .builder("timer.labeled")
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(mmRegistry);

        labeledTimer_1.record(1L, MILLISECONDS);
        labeledTimer_1.record(2L, MILLISECONDS);

        Timer labeledTimer_2 = Timer
            .builder("timer.labeled")
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(mmRegistry);

        labeledTimer_2.record(3L, MILLISECONDS);
        labeledTimer_2.record(4L, MILLISECONDS);

        // FunctionTimer
        AtomicLong funTimerCountSupplier = new AtomicLong();
        AtomicLong funTimerTotalTimeSupplier = new AtomicLong();

        FunctionTimer funTimer = FunctionTimer
            .builder(
                "funTimer.nonLabeled",
                MicrometerSample.class,
                a -> funTimerCountSupplier.incrementAndGet(),
                a -> (double)funTimerTotalTimeSupplier.incrementAndGet(),
                MILLISECONDS)
            .register(mmRegistry);

        AtomicLong labeledFunTimerCountSupplier_1 = new AtomicLong();
        AtomicLong labeledFunTimerTotalTimeSupplier_1 = new AtomicLong();

        FunctionTimer funTimer_1 = FunctionTimer
            .builder(
                "funTimer.labeled",
                MicrometerSample.class,
                a -> labeledFunTimerCountSupplier_1.incrementAndGet(),
                a -> (double)labeledFunTimerTotalTimeSupplier_1.incrementAndGet(),
                MILLISECONDS)
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(mmRegistry);

        AtomicLong labeledFunTimerCountSupplier_2 = new AtomicLong();
        AtomicLong labeledFunTimerTotalTimeSupplier_2 = new AtomicLong();

        FunctionTimer funTimer_2 = FunctionTimer
            .builder(
                "funTimer.labeled",
                MicrometerSample.class,
                a -> labeledFunTimerCountSupplier_2.incrementAndGet(),
                a -> (double)labeledFunTimerTotalTimeSupplier_2.incrementAndGet(),
                MILLISECONDS)
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(mmRegistry);

        // LongTaskTimer
        LongTaskTimer longTaskTimer = LongTaskTimer
            .builder("longTaskTimer.nonLabeled")
            .register(mmRegistry);

        longTaskTimer.start();
        longTaskTimer.start();

        LongTaskTimer labeledLongTaskTimer_1 = LongTaskTimer
            .builder("longTaskTimer.labeled")
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(mmRegistry);

        labeledLongTaskTimer_1.start();
        labeledLongTaskTimer_1.start();

        LongTaskTimer labeledLongTaskTimer_2 = LongTaskTimer
            .builder("longTaskTimer.labeled")
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(mmRegistry);

        labeledLongTaskTimer_2.start();
        labeledLongTaskTimer_2.start();
        labeledLongTaskTimer_2.start();

        // Meter
        AtomicLong labeledMeterDurationSupplier_1 = new AtomicLong();
        AtomicLong labeledMeterMaxSupplier_1 = new AtomicLong();

        Meter labeledMeter_1 = Meter
            .builder(
                "meter.labeled",
                Meter.Type.OTHER,
                List.of(
                    new Measurement(() -> (double)labeledMeterDurationSupplier_1.incrementAndGet(), Statistic.DURATION),
                    new Measurement(() -> (double)labeledMeterMaxSupplier_1.incrementAndGet(), Statistic.MAX)))
            .tags(SERVICE.name(), "service_1", SERVER.name(), "server_1_1")
            .register(mmRegistry);

        AtomicLong labeledMeterDurationSupplier_2 = new AtomicLong();
        AtomicLong labeledMeterMaxSupplier_2 = new AtomicLong();

        Meter labeledMeter_2 = Meter
            .builder(
                "meter.labeled",
                Meter.Type.OTHER,
                List.of(
                    new Measurement(() -> (double)labeledMeterDurationSupplier_2.incrementAndGet(), Statistic.DURATION),
                    new Measurement(() -> (double)labeledMeterMaxSupplier_2.incrementAndGet(), Statistic.MAX)))
            .tags(SERVICE.name(), "service_2", SERVER.name(), "server_2_1")
            .register(mmRegistry);

        hang();
    }
}
