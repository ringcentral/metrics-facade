package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.timer.Timer;
import com.ringcentral.platform.metrics.var.doubleVar.*;
import com.ringcentral.platform.metrics.var.longVar.*;
import com.ringcentral.platform.metrics.var.objectVar.*;
import com.ringcentral.platform.metrics.var.stringVar.*;
import org.slf4j.Logger;

import java.util.*;

import static com.ringcentral.platform.metrics.TestMetricRegistryListener.NotificationType.*;
import static org.slf4j.LoggerFactory.getLogger;

@SuppressWarnings("unchecked")
public class TestMetricRegistryListener implements MetricRegistryListener {

    public enum NotificationType {
        METRIC_ADDED,
        METRIC_REMOVED
    }

    public static class Notification {

        final NotificationType type;
        final Metric metric;

        public Notification(NotificationType type, Metric metric) {
            this.type = type;
            this.metric = metric;
        }

        public NotificationType type() {
            return type;
        }

        public <M extends Metric> M metric() {
            return (M)metric;
        }
    }

    public static class MetricEntry {

        final Metric metric;
        final TestMetricListener listener;

        public MetricEntry(Metric metric) {
            this.metric = metric;
            this.listener = new TestMetricListener();
            this.metric.addListener(this.listener);
        }

        public <M extends Metric> M metric() {
            return (M)metric;
        }

        public TestMetricListener listener() {
            return listener;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            MetricEntry that = (MetricEntry)other;
            return Objects.equals(metric, that.metric);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(metric);
        }
    }

    final List<Notification> notifications = new ArrayList<>();
    final LinkedHashSet<MetricEntry> entries = new LinkedHashSet<>();
    static final Logger logger = getLogger(TestMetricRegistryListener.class);

    public Notification notification(int i) {
        return notifications.get(i);
    }

    public int notificationCount() {
        return notifications.size();
    }

    public TestMetricListener listenerForMetric(MetricName name) {
        return entries.stream()
            .filter(e -> e.metric.name().equals(name))
            .findFirst()
            .map(MetricEntry::listener)
            .orElse(null);
    }

    @Override
    public void objectVarAdded(ObjectVar objectVar) {
        notifications.add(new Notification(METRIC_ADDED, objectVar));
        entries.add(new MetricEntry(objectVar));

        logger.info(
            "Metric added: type = {}, name = {}",
            objectVar.getClass().getSimpleName(), objectVar.name());
    }

    @Override
    public void objectVarRemoved(ObjectVar objectVar) {
        notifications.add(new Notification(METRIC_REMOVED, objectVar));
        entries.remove(new MetricEntry(objectVar));

        logger.info(
            "Metric removed: type = {}, name = {}",
            objectVar.getClass().getSimpleName(), objectVar.name());
    }

    @Override
    public void cachingObjectVarAdded(CachingObjectVar cachingObjectVar) {
        notifications.add(new Notification(METRIC_ADDED, cachingObjectVar));
        entries.add(new MetricEntry(cachingObjectVar));

        logger.info(
            "Metric added: type = {}, name = {}",
            cachingObjectVar.getClass().getSimpleName(), cachingObjectVar.name());
    }

    @Override
    public void cachingObjectVarRemoved(CachingObjectVar cachingObjectVar) {
        notifications.add(new Notification(METRIC_REMOVED, cachingObjectVar));
        entries.remove(new MetricEntry(cachingObjectVar));

        logger.info(
            "Metric removed: type = {}, name = {}",
            cachingObjectVar.getClass().getSimpleName(), cachingObjectVar.name());
    }

    @Override
    public void longVarAdded(LongVar longVar) {
        notifications.add(new Notification(METRIC_ADDED, longVar));
        entries.add(new MetricEntry(longVar));

        logger.info(
            "Metric added: type = {}, name = {}",
            longVar.getClass().getSimpleName(), longVar.name());
    }

    @Override
    public void longVarRemoved(LongVar longVar) {
        notifications.add(new Notification(METRIC_REMOVED, longVar));
        entries.remove(new MetricEntry(longVar));

        logger.info(
            "Metric removed: type = {}, name = {}",
            longVar.getClass().getSimpleName(), longVar.name());
    }

    @Override
    public void cachingLongVarAdded(CachingLongVar cachingLongVar) {
        notifications.add(new Notification(METRIC_ADDED, cachingLongVar));
        entries.add(new MetricEntry(cachingLongVar));

        logger.info(
            "Metric added: type = {}, name = {}",
            cachingLongVar.getClass().getSimpleName(), cachingLongVar.name());
    }

    @Override
    public void cachingLongVarRemoved(CachingLongVar cachingLongVar) {
        notifications.add(new Notification(METRIC_REMOVED, cachingLongVar));
        entries.remove(new MetricEntry(cachingLongVar));

        logger.info(
            "Metric removed: type = {}, name = {}",
            cachingLongVar.getClass().getSimpleName(), cachingLongVar.name());
    }

    @Override
    public void doubleVarAdded(DoubleVar doubleVar) {
        notifications.add(new Notification(METRIC_ADDED, doubleVar));
        entries.add(new MetricEntry(doubleVar));

        logger.info(
            "Metric added: type = {}, name = {}",
            doubleVar.getClass().getSimpleName(), doubleVar.name());
    }

    @Override
    public void doubleVarRemoved(DoubleVar doubleVar) {
        notifications.add(new Notification(METRIC_REMOVED, doubleVar));
        entries.remove(new MetricEntry(doubleVar));

        logger.info(
            "Metric removed: type = {}, name = {}",
            doubleVar.getClass().getSimpleName(), doubleVar.name());
    }

    @Override
    public void cachingDoubleVarAdded(CachingDoubleVar cachingDoubleVar) {
        notifications.add(new Notification(METRIC_ADDED, cachingDoubleVar));
        entries.add(new MetricEntry(cachingDoubleVar));

        logger.info(
            "Metric added: type = {}, name = {}",
            cachingDoubleVar.getClass().getSimpleName(), cachingDoubleVar.name());
    }

    @Override
    public void cachingDoubleVarRemoved(CachingDoubleVar cachingDoubleVar) {
        notifications.add(new Notification(METRIC_REMOVED, cachingDoubleVar));
        entries.remove(new MetricEntry(cachingDoubleVar));

        logger.info(
            "Metric removed: type = {}, name = {}",
            cachingDoubleVar.getClass().getSimpleName(), cachingDoubleVar.name());
    }

    @Override
    public void stringVarAdded(StringVar stringVar) {
        notifications.add(new Notification(METRIC_ADDED, stringVar));
        entries.add(new MetricEntry(stringVar));

        logger.info(
            "Metric added: type = {}, name = {}",
            stringVar.getClass().getSimpleName(), stringVar.name());
    }

    @Override
    public void stringVarRemoved(StringVar stringVar) {
        notifications.add(new Notification(METRIC_REMOVED, stringVar));
        entries.remove(new MetricEntry(stringVar));

        logger.info(
            "Metric removed: type = {}, name = {}",
            stringVar.getClass().getSimpleName(), stringVar.name());
    }

    @Override
    public void cachingStringVarAdded(CachingStringVar cachingStringVar) {
        notifications.add(new Notification(METRIC_ADDED, cachingStringVar));
        entries.add(new MetricEntry(cachingStringVar));

        logger.info(
            "Metric added: type = {}, name = {}",
            cachingStringVar.getClass().getSimpleName(), cachingStringVar.name());
    }

    @Override
    public void cachingStringVarRemoved(CachingStringVar cachingStringVar) {
        notifications.add(new Notification(METRIC_REMOVED, cachingStringVar));
        entries.remove(new MetricEntry(cachingStringVar));

        logger.info(
            "Metric removed: type = {}, name = {}",
            cachingStringVar.getClass().getSimpleName(), cachingStringVar.name());
    }

    @Override
    public void counterAdded(Counter counter) {
        notifications.add(new Notification(METRIC_ADDED, counter));
        entries.add(new MetricEntry(counter));

        logger.info(
            "Metric added: type = {}, name = {}",
            counter.getClass().getSimpleName(), counter.name());
    }

    @Override
    public void counterRemoved(Counter counter) {
        notifications.add(new Notification(METRIC_REMOVED, counter));
        entries.remove(new MetricEntry(counter));

        logger.info(
            "Metric removed: type = {}, name = {}",
            counter.getClass().getSimpleName(), counter.name());
    }

    @Override
    public void rateAdded(Rate rate) {
        notifications.add(new Notification(METRIC_ADDED, rate));
        entries.add(new MetricEntry(rate));

        logger.info(
            "Metric added: type = {}, name = {}",
            rate.getClass().getSimpleName(), rate.name());
    }

    @Override
    public void rateRemoved(Rate rate) {
        notifications.add(new Notification(METRIC_REMOVED, rate));
        entries.remove(new MetricEntry(rate));

        logger.info(
            "Metric removed: type = {}, name = {}",
            rate.getClass().getSimpleName(), rate.name());
    }

    @Override
    public void histogramAdded(Histogram histogram) {
        notifications.add(new Notification(METRIC_ADDED, histogram));
        entries.add(new MetricEntry(histogram));

        logger.info(
            "Metric added: type = {}, name = {}",
            histogram.getClass().getSimpleName(), histogram.name());
    }

    @Override
    public void histogramRemoved(Histogram histogram) {
        notifications.add(new Notification(METRIC_REMOVED, histogram));
        entries.remove(new MetricEntry(histogram));

        logger.info(
            "Metric removed: type = {}, name = {}",
            histogram.getClass().getSimpleName(), histogram.name());
    }

    @Override
    public void timerAdded(Timer timer) {
        notifications.add(new Notification(METRIC_ADDED, timer));
        entries.add(new MetricEntry(timer));

        logger.info(
            "Metric added: type = {}, name = {}",
            timer.getClass().getSimpleName(), timer.name());
    }

    @Override
    public void timerRemoved(Timer timer) {
        notifications.add(new Notification(METRIC_REMOVED, timer));
        entries.remove(new MetricEntry(timer));

        logger.info(
            "Metric removed: type = {}, name = {}",
            timer.getClass().getSimpleName(), timer.name());
    }
}
