package com.ringcentral.platform.metrics.samples;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class CompositeInstanceSamplesProvider<
    S extends Sample<S>,
    IS extends InstanceSample<S>,
    ISP extends InstanceSamplesProvider<? extends S, ? extends IS>> implements InstanceSamplesProvider<S, IS> {

    private final Collection<ISP> children;

    public CompositeInstanceSamplesProvider(Collection<ISP> children) {
        this.children = children;
    }

    @Override
    public Set<IS> instanceSamples() {
        Set<IS> result = new LinkedHashSet<>();
        children.forEach(child -> result.addAll(child.instanceSamples()));
        return result;
    }
}
