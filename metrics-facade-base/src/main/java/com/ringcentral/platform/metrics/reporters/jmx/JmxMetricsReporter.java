package com.ringcentral.platform.metrics.reporters.jmx;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.reporters.MetricsReporter;
import com.ringcentral.platform.metrics.timer.Timer;
import com.ringcentral.platform.metrics.var.doubleVar.*;
import com.ringcentral.platform.metrics.var.longVar.*;
import com.ringcentral.platform.metrics.var.objectVar.*;
import com.ringcentral.platform.metrics.var.stringVar.*;
import org.slf4j.Logger;

import javax.management.*;
import java.io.Closeable;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Boolean.TRUE;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static java.util.Objects.requireNonNullElse;
import static org.slf4j.LoggerFactory.getLogger;

public class JmxMetricsReporter implements MetricsReporter, MetricRegistryListener, Closeable {

    public static final String DEFAULT_DOMAIN_NAME = "metrics";

    private static class MetricListenerImpl implements MetricListener {

        final PredicativeMetricNamedInfoProvider<MBeanSpecProvider> mBeanSpecModProviders;
        final MBeanServer mBeanServer;
        final ObjectNameProvider objectNameProvider;
        final MeasurableNameProvider measurableNameProvider;
        final String domainName;
        final ConcurrentHashMap<ObjectName, ObjectName> actualObjectNames = new ConcurrentHashMap<>();

        private MetricListenerImpl(
            PredicativeMetricNamedInfoProvider<MBeanSpecProvider> mBeanSpecModProviders,
            MBeanServer mBeanServer,
            ObjectNameProvider objectNameProvider,
            MeasurableNameProvider measurableNameProvider,
            String domainName) {

            this.mBeanSpecModProviders = mBeanSpecModProviders;
            this.mBeanServer = mBeanServer;
            this.objectNameProvider = objectNameProvider;
            this.measurableNameProvider = measurableNameProvider;
            this.domainName = domainName;
        }

        @Override
        public void metricInstanceAdded(MetricInstance instance) {
            MBeanSpec mBeanSpec = mBeanSpec(instance);

            if (mBeanSpec.isEnabled() && mBeanSpec.hasName() && mBeanSpec.hasMeasurables()) {
                registerMBean(
                    objectName(mBeanSpec),
                    new MBeanImpl(instance, mBeanSpec.measurables(), measurableNameProvider));
            }
        }

        private MBeanSpec mBeanSpec(MetricInstance instance) {
            MBeanSpec mBeanSpec = new MBeanSpec(
                TRUE,
                instance.name(),
                instance.measurables(),
                instance.labelValues());

            if (mBeanSpecModProviders != null) {
                for (MBeanSpecProvider mBeanSpecProvider : mBeanSpecModProviders.infosFor(instance)) {
                    mBeanSpec.modify(mBeanSpecProvider.mBeanSpecFor(instance));
                }
            }

            return mBeanSpec;
        }

        private ObjectName objectName(MBeanSpec mBeanSpec) {
            return objectNameProvider.objectNameFor(
                domainName,
                mBeanSpec.name(),
                mBeanSpec.labelValues());
        }

        @Override
        public void metricInstanceRemoved(MetricInstance instance) {
            MBeanSpec mBeanSpec = mBeanSpec(instance);

            if (mBeanSpec.isEnabled() && mBeanSpec.hasName() && mBeanSpec.hasMeasurables()) {
                deregisterMBean(objectName(mBeanSpec));
            }
        }

        private void registerMBean(ObjectName objectName, DynamicMBean mBean) {
            try {
                ObjectInstance registeredMBean = mBeanServer.registerMBean(mBean, objectName);

                if (registeredMBean != null) {
                    actualObjectNames.put(objectName, registeredMBean.getObjectName());
                } else {
                    actualObjectNames.put(objectName, objectName);
                }
            } catch (InstanceAlreadyExistsException e) {
                logger.debug("Failed to register MBean '{}'", objectName.getCanonicalName(), e);
            } catch (Exception e) {
                logger.warn("Failed to register MBean '{}'", objectName.getCanonicalName(), e);
            }
        }

        private void deregisterMBean(ObjectName objectName) {
            try {
                ObjectName actualObjectName = actualObjectNames.remove(objectName);
                mBeanServer.unregisterMBean(requireNonNullElse(actualObjectName, objectName));
            } catch (InstanceNotFoundException e) {
                logger.debug("Failed to deregister MBean '{}'", objectName.getCanonicalName(), e);
            } catch (Exception e) {
                logger.warn("Failed to deregister MBean '{}'", objectName.getCanonicalName(), e);
            }
        }

        public void close() {
            actualObjectNames.keySet().forEach(this::deregisterMBean);
            actualObjectNames.clear();
        }
    }

    private final MetricListenerImpl metricListener;
    private static final Logger logger = getLogger(JmxMetricsReporter.class);

    public JmxMetricsReporter() {
        this(getPlatformMBeanServer());
    }

    public JmxMetricsReporter(MBeanServer mBeanServer) {
        this(
            mBeanServer,
            DefaultObjectNameProvider.INSTANCE,
            DefaultMeasurableNameProvider.INSTANCE,
            DEFAULT_DOMAIN_NAME);
    }

    public JmxMetricsReporter(
        PredicativeMetricNamedInfoProvider<MBeanSpecProvider> mBeanSpecModProviders,
        MeasurableNameProvider measurableNameProvider) {

        this(
            mBeanSpecModProviders,
            getPlatformMBeanServer(),
            DefaultObjectNameProvider.INSTANCE,
            measurableNameProvider,
            DEFAULT_DOMAIN_NAME);
    }

    public JmxMetricsReporter(
        MBeanServer mBeanServer,
        ObjectNameProvider objectNameProvider,
        MeasurableNameProvider measurableNameProvider,
        String domainName) {

        this(
            null,
            mBeanServer,
            objectNameProvider,
            measurableNameProvider,
            domainName);
    }

    public JmxMetricsReporter(
        PredicativeMetricNamedInfoProvider<MBeanSpecProvider> mBeanSpecModProviders,
        MBeanServer mBeanServer,
        ObjectNameProvider objectNameProvider,
        MeasurableNameProvider measurableNameProvider,
        String domainName) {

        this.metricListener = new MetricListenerImpl(
            mBeanSpecModProviders,
            mBeanServer,
            objectNameProvider,
            measurableNameProvider,
            domainName);
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