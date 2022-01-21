package com.ringcentral.platform.metrics.x.histogram.hdr;

import com.ringcentral.platform.metrics.x.histogram.*;

public interface HdrXHistogramImpl extends XHistogramImpl {
    XHistogramImplSnapshot snapshot();
}
