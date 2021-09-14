package com.ringcentral.platform.metrics.spring.jmx;

import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.measurables.MeasurableNameProvider;
import com.ringcentral.platform.metrics.reporters.jmx.*;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class MfJmxConfig {

    private final PredicativeMetricNamedInfoProvider<MBeanSpecProvider> mBeanSpecModProviders;
    private final ObjectNameProvider objectNameProvider;
    private final MeasurableNameProvider measurableNameProvider;
    private final String domainName;

    public MfJmxConfig(
        PredicativeMetricNamedInfoProvider<MBeanSpecProvider> mBeanSpecModProviders,
        ObjectNameProvider objectNameProvider,
        MeasurableNameProvider measurableNameProvider,
        String domainName) {

        this.mBeanSpecModProviders = mBeanSpecModProviders;
        this.objectNameProvider = objectNameProvider;
        this.measurableNameProvider = measurableNameProvider;
        this.domainName = domainName;
    }

    public boolean hasMBeanSpecModProviders() {
        return mBeanSpecModProviders != null;
    }

    public PredicativeMetricNamedInfoProvider<MBeanSpecProvider> mBeanSpecModProviders() {
        return mBeanSpecModProviders;
    }

    public boolean hasObjectNameProvider() {
        return objectNameProvider != null;
    }

    public ObjectNameProvider objectNameProvider() {
        return objectNameProvider;
    }

    public boolean hasMeasurableNameProvider() {
        return measurableNameProvider != null;
    }

    public MeasurableNameProvider measurableNameProvider() {
        return measurableNameProvider;
    }

    public boolean hasDomainName() {
        return !isBlank(domainName);
    }

    public String domainName() {
        return domainName;
    }
}
