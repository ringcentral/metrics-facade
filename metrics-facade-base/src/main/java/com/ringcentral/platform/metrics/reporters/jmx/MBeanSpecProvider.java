package com.ringcentral.platform.metrics.reporters.jmx;

import com.ringcentral.platform.metrics.MetricInstance;

public interface MBeanSpecProvider {
    MBeanSpec mBeanSpecFor(MetricInstance instance);
}
