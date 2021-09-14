package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.measurables.MeasurableValues;
import com.ringcentral.platform.metrics.var.objectVar.ObjectVar;

import java.util.*;

import static java.util.Objects.requireNonNull;

public abstract class AbstractInstanceSamplesProvider<
    S extends Sample,
    SS extends SampleSpec,
    SSP extends SampleSpecProvider<SS, ISS>,
    SM extends SampleMaker<S, SS, ISS>,
    IS extends InstanceSample<S>,
    ISS extends InstanceSampleSpec,
    ISSP extends InstanceSampleSpecProvider<ISS>,
    ISM extends InstanceSampleMaker<S, IS, ISS>> implements InstanceSamplesProvider<S, IS> {

    private final ISSP instanceSampleSpecProvider;
    private final PredicativeMetricNamedInfoProvider<ISSP> instanceSampleSpecModsProvider;
    private final ISM instanceSampleMaker;

    private final SSP sampleSpecProvider;
    private final PredicativeMetricNamedInfoProvider<SSP> sampleSpecModsProvider;
    private final SM sampleMaker;

    private final MetricRegistry metricRegistry;

    public AbstractInstanceSamplesProvider(
        ISSP instanceSampleSpecProvider,
        PredicativeMetricNamedInfoProvider<ISSP> instanceSampleSpecModsProvider,
        ISM instanceSampleMaker,
        SSP sampleSpecProvider,
        PredicativeMetricNamedInfoProvider<SSP> sampleSpecModsProvider,
        SM sampleMaker,
        MetricRegistry metricRegistry) {

        this.instanceSampleSpecProvider = requireNonNull(instanceSampleSpecProvider);
        this.instanceSampleSpecModsProvider = instanceSampleSpecModsProvider;
        this.instanceSampleMaker = requireNonNull(instanceSampleMaker);

        this.sampleSpecProvider = requireNonNull(sampleSpecProvider);
        this.sampleSpecModsProvider = sampleSpecModsProvider;
        this.sampleMaker = requireNonNull(sampleMaker);

        this.metricRegistry = requireNonNull(metricRegistry);
    }

    @Override
    public Set<IS> instanceSamples() {
        Set<IS> result = new LinkedHashSet<>();

        metricRegistry.metrics().forEach((name, metric) -> {
            if (metric instanceof ObjectVar) {
                return;
            }

            metric.forEach(instance -> {
                ISS instanceSampleSpec = instanceSampleSpecProvider.instanceSampleSpecFor(metric, instance);

                if (instanceSampleSpec == null || !instanceSampleSpec.isEnabled()) {
                    return;
                }

                if (instanceSampleSpecModsProvider != null) {
                    for (ISSP instanceSampleSpecModProvider : instanceSampleSpecModsProvider.infosFor(instance)) {
                        instanceSampleSpec.modify(instanceSampleSpecModProvider.instanceSampleSpecFor(metric, instance));
                    }
                }

                if (!instanceSampleSpec.isEnabled()) {
                    return;
                }

                IS instanceSample = instanceSampleMaker.makeInstanceSample(instanceSampleSpec);

                if (instanceSample == null) {
                    return;
                }

                MeasurableValues measurableValues = instance.measurableValues();

                instance.measurables().forEach(measurable -> {
                    SS sampleSpec = sampleSpecProvider.sampleSpecFor(instanceSampleSpec, instance, measurableValues, measurable);

                    if (sampleSpec == null || !sampleSpec.isEnabled()) {
                        return;
                    }

                    if (sampleSpecModsProvider != null) {
                        for (SSP sampleSpecModProvider : sampleSpecModsProvider.infosFor(instance)) {
                            sampleSpec.modify(sampleSpecModProvider.sampleSpecFor(instanceSampleSpec, instance, measurableValues, measurable));
                        }
                    }

                    if (!sampleSpec.isEnabled()) {
                        return;
                    }

                    S sample = sampleMaker.makeSample(sampleSpec, instanceSampleSpec);

                    if (sample != null) {
                        instanceSample.add(sample);
                    }
                });

                if (!instanceSample.isEmpty()) {
                    result.add(instanceSample);
                }
            });
        });

        return result;
    }
}
