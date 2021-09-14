package com.ringcentral.platform.metrics;

import org.slf4j.Logger;

import java.util.*;

import static com.ringcentral.platform.metrics.TestMetricListener.NotificationType.*;
import static java.util.stream.Collectors.joining;
import static org.slf4j.LoggerFactory.getLogger;

public class TestMetricListener implements MetricListener {

    public enum NotificationType {
        INSTANCE_ADDED,
        INSTANCE_REMOVED
    }

    public static class Notification {

        final NotificationType type;
        final MetricInstance instance;

        public Notification(NotificationType type, MetricInstance instance) {
            this.type = type;
            this.instance = instance;
        }

        public NotificationType type() {
            return type;
        }

        public MetricInstance instance() {
            return instance;
        }
    }

    final List<Notification> notifications = new ArrayList<>();
    final LinkedHashSet<MetricInstance> instances = new LinkedHashSet<>();
    static final Logger logger = getLogger(TestMetricListener.class);

    @Override
    public void metricInstanceAdded(MetricInstance instance) {
        notifications.add(new Notification(INSTANCE_ADDED, instance));
        instances.add(instance);

        logger.info(
            "Instance added: name = {}, dimension values = [{}], total = {}, level = {}",
            instance.name(),
            instance.dimensionValues().stream()
                .map(dv -> dv.dimension().name() + "=" + dv.value())
                .collect(joining(",")),
            instance.isTotalInstance(),
            instance.isLevelInstance());
    }

    @Override
    public void metricInstanceRemoved(MetricInstance instance) {
        notifications.add(new Notification(INSTANCE_REMOVED, instance));
        instances.remove(instance);

        logger.info(
            "Instance removed: name = {}, dimension values = [{}], total = {}, level = {}",
            instance.name(),
            instance.dimensionValues().stream()
                .map(dv -> dv.dimension().name() + "=" + dv.value())
                .collect(joining(",")),
            instance.isTotalInstance(),
            instance.isLevelInstance());
    }

    public List<Notification> notifications() {
        return notifications;
    }

    public Notification notification(int i) {
        return notifications.get(i);
    }

    public int notificationCount() {
        return notifications.size();
    }

    public LinkedHashSet<MetricInstance> instances() {
        return instances;
    }
}
