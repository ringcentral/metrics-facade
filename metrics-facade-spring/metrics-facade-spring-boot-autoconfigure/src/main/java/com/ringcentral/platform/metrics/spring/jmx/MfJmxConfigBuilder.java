package com.ringcentral.platform.metrics.spring.jmx;

import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.measurables.MeasurableNameProvider;
import com.ringcentral.platform.metrics.reporters.jmx.*;

public class MfJmxConfigBuilder {

    private PredicativeMetricNamedInfoProvider<MBeanSpecProvider> mBeanSpecModProviders;
    private ObjectNameProvider objectNameProvider;
    private MeasurableNameProvider measurableNameProvider;
    private String domainName;

    public MfJmxConfigBuilder() {}

    public MfJmxConfigBuilder rebase(MfJmxConfigBuilder base) {
        if (base.mBeanSpecModProviders != null && mBeanSpecModProviders == null) {
            mBeanSpecModProviders = base.mBeanSpecModProviders;
        }

        if (base.objectNameProvider != null && objectNameProvider == null) {
            objectNameProvider = base.objectNameProvider;
        }

        if (base.measurableNameProvider != null && measurableNameProvider == null) {
            measurableNameProvider = base.measurableNameProvider;
        }

        if (base.domainName != null && domainName == null) {
            domainName = base.domainName;
        }

        return this;
    }

    public MfJmxConfigBuilder mBeanSpecModProviders(PredicativeMetricNamedInfoProvider<MBeanSpecProvider> mBeanSpecModProviders) {
        this.mBeanSpecModProviders = mBeanSpecModProviders;
        return this;
    }

    public MfJmxConfigBuilder objectNameProvider(ObjectNameProvider objectNameProvider) {
        this.objectNameProvider = objectNameProvider;
        return this;
    }

    public MfJmxConfigBuilder measurableNameProvider(MeasurableNameProvider measurableNameProvider) {
        this.measurableNameProvider = measurableNameProvider;
        return this;
    }

    public MfJmxConfigBuilder domainName(String domainName) {
        this.domainName = domainName;
        return this;
    }

    public MfJmxConfig build() {
        return new MfJmxConfig(
            mBeanSpecModProviders,
            objectNameProvider,
            measurableNameProvider,
            domainName);
    }
}
