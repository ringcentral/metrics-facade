package com.ringcentral.platform.metrics.defaultImpl.histogram.custom;

import com.ringcentral.platform.metrics.defaultImpl.histogram.CustomHistogramImplMaker;
import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.HistogramImplConfig;

public abstract class AbstractTestCustomHistogramImplMaker<C extends HistogramImplConfig> implements CustomHistogramImplMaker<C> {}
