package com.ringcentral.platform.metrics.reporters.zabbix;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.infoProviders.*;
import com.ringcentral.platform.metrics.predicates.MetricInstancePredicate;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.reporters.MetricsReporter;
import com.ringcentral.platform.metrics.timer.Timer;
import com.ringcentral.platform.metrics.var.doubleVar.*;
import com.ringcentral.platform.metrics.var.longVar.*;
import com.ringcentral.platform.metrics.var.objectVar.*;
import com.ringcentral.platform.metrics.var.stringVar.*;
import org.slf4j.Logger;

import java.io.Closeable;
import java.util.*;
import java.util.function.*;

import static com.ringcentral.platform.metrics.utils.Preconditions.*;
import static java.util.Objects.*;
import static org.slf4j.LoggerFactory.*;

public class ZabbixLldMetricsReporter implements MetricsReporter, MetricRegistryListener, Closeable {

    public static class RuleItem {

        private final Function<MetricInstance, String> attrValueProvider;
        private final String attrName;

        public RuleItem(MetricDimension dimension, String attrName) {
            this(instance -> instance.valueOf(dimension), attrName);
        }

        public RuleItem(Function<MetricInstance, String> attrValueProvider, String attrName) {
            this.attrValueProvider = requireNonNull(attrValueProvider);
            this.attrName = requireNonNull(attrName);
        }

        public String attrName() {
            return attrName;
        }

        public String attrValueFor(MetricInstance instance) {
            return attrValueProvider.apply(instance);
        }
    }

    public static class Rule {

        private final String groupName;
        private final List<RuleItem> items;

        public Rule(String groupName, List<RuleItem> items) {
            this.groupName = requireNonNull(groupName);
            checkArgument(items != null && !items.isEmpty(), "items is null or empty");
            this.items = items;
        }

        public String groupName() {
            return groupName;
        }

        public List<RuleItem> items() {
            return items;
        }
    }

    private static class MetricListenerImpl implements MetricListener {

        private final PredicativeMetricNamedInfoProvider<Rule> rulesProvider;
        private final ZabbixLldMetricsReporterListener listener;

        private final Map<ZEntity, Set<MetricInstance>> entityToInstances = new HashMap<>();
        private final Map<MetricInstance, Set<ZEntity>> instanceToEntities = new HashMap<>();

        private MetricListenerImpl(
            PredicativeMetricNamedInfoProvider<Rule> rulesProvider,
            ZabbixLldMetricsReporterListener listener) {

            this.rulesProvider = rulesProvider;
            this.listener = listener;
        }

        @Override
        public synchronized void metricInstanceAdded(MetricInstance instance) {
            if (!instance.hasDimensionValues()) {
                return;
            }

            List<Rule> rules = rulesProvider.infosFor(instance);

            if (rules.isEmpty()) {
                return;
            }

            for (Rule rule : rules) {
                List<ZAttribute> attrs = new ArrayList<>();

                for (RuleItem item : rule.items()) {
                    String attrValue = item.attrValueFor(instance);

                    if (attrValue != null) {
                        attrs.add(new ZAttribute(item.attrName(), attrValue));
                    } else {
                        return;
                    }
                }

                if (!attrs.isEmpty()) {
                    ZEntity entity = new ZEntity(rule.groupName(), attrs);
                    addEntityInstance(entity, instance);
                }
            }
        }

        private void addEntityInstance(ZEntity entity, MetricInstance instance) {
            if (entityToInstances.containsKey(entity)) {
                Set<MetricInstance> entityInstances = entityToInstances.get(entity);

                if (!entityInstances.contains(instance)) {
                    entityInstances.add(instance);
                    instanceToEntities.computeIfAbsent(instance, i -> new HashSet<>()).add(entity);
                }
            } else {
                Set<MetricInstance> entityInstances = new HashSet<>();
                entityInstances.add(instance);
                entityToInstances.put(entity, entityInstances);
                instanceToEntities.computeIfAbsent(instance, i -> new HashSet<>()).add(entity);
                notifyListener(listener, l -> l.entityAdded(entity));
            }
        }

        @Override
        public synchronized void metricInstanceRemoved(MetricInstance instance) {
            if (!instance.hasDimensionValues()) {
                return;
            }

            if (!instanceToEntities.containsKey(instance)) {
                return;
            }

            Set<ZEntity> entities = instanceToEntities.remove(instance);

            entities.forEach(entity -> {
                Set<MetricInstance> entityInstances = entityToInstances.get(entity);
                entityInstances.remove(instance);

                if (entityInstances.isEmpty()) {
                    entityToInstances.remove(entity);
                    notifyListener(listener, l -> l.entityRemoved(entity));
                }
            });
        }

        private static void notifyListener(
            ZabbixLldMetricsReporterListener listener,
            Consumer<ZabbixLldMetricsReporterListener> action) {

            try {
                action.accept(listener);
            } catch (Exception e) {
                logger.error("Failed to notify listener", e);
            }
        }

        public synchronized void close() {
            try {
                listener.close();
            } catch (Exception e) {
                logger.error("Failed to close listener", e);
            }
        }
    }

    private final PredicativeMetricNamedInfoProvider<Rule> rulesProvider;
    private final MetricListenerImpl metricListener;

    private static final Logger logger = getLogger(ZabbixLldMetricsReporter.class);

    public ZabbixLldMetricsReporter(ZabbixLldMetricsReporterListener listener) {
        this(new DefaultConcurrentMetricNamedInfoProvider<>(), listener);
    }

    public ZabbixLldMetricsReporter(
        PredicativeMetricNamedInfoProvider<Rule> rulesProvider,
        ZabbixLldMetricsReporterListener listener) {

        this.rulesProvider = requireNonNull(rulesProvider);
        this.metricListener = new MetricListenerImpl(rulesProvider, requireNonNull(listener));
    }

    public void addRules(MetricInstancePredicate predicate, Rule... rules) {
        for (Rule rule : rules) {
            rulesProvider.addInfo(predicate, rule);
        }
    }

    @Override
    public void objectVarAdded(ObjectVar objectVar) {
        objectVar.addListener(metricListener);
    }

    @Override
    public void cachingObjectVarAdded(CachingObjectVar cachingObjectVar) {
        cachingObjectVar.addListener(metricListener);
    }

    @Override
    public void longVarAdded(LongVar longVar) {
        longVar.addListener(metricListener);
    }

    @Override
    public void cachingLongVarAdded(CachingLongVar cachingLongVar) {
        cachingLongVar.addListener(metricListener);
    }

    @Override
    public void doubleVarAdded(DoubleVar doubleVar) {
        doubleVar.addListener(metricListener);
    }

    @Override
    public void cachingDoubleVarAdded(CachingDoubleVar cachingDoubleVar) {
        cachingDoubleVar.addListener(metricListener);
    }

    @Override
    public void stringVarAdded(StringVar stringVar) {
        stringVar.addListener(metricListener);
    }

    @Override
    public void cachingStringVarAdded(CachingStringVar cachingStringVar) {
        cachingStringVar.addListener(metricListener);
    }

    @Override
    public void counterAdded(Counter counter) {
        counter.addListener(metricListener);
    }

    @Override
    public void rateAdded(Rate rate) {
        rate.addListener(metricListener);
    }

    @Override
    public void histogramAdded(Histogram histogram) {
        histogram.addListener(metricListener);
    }

    @Override
    public void timerAdded(Timer timer) {
        timer.addListener(metricListener);
    }

    @Override
    public void close() {
        metricListener.close();
    }
}