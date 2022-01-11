package com.ringcentral.platform.metrics.x.histogram.hdr;

import com.ringcentral.platform.metrics.x.histogram.XHistogramImpl;

public interface HdrXHistogramImpl extends XHistogramImpl {
    HdrXHistogramImplSnapshot snapshot();
}
