package com.ringcentral.platform.metrics.defaultImpl;

import com.ringcentral.platform.metrics.MetricMod;
import com.ringcentral.platform.metrics.defaultImpl.histogram.CustomHistogramImplMaker;
import com.ringcentral.platform.metrics.defaultImpl.rate.CustomRateImplMaker;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.utils.SystemTimeMsProvider;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.AbstractMetricRegistry.makeDefaultExecutor;
import static com.ringcentral.platform.metrics.AbstractMetricRegistry.makeDefaultModsProvider;
import static java.lang.reflect.Modifier.isAbstract;

public class DefaultMetricRegistryBuilder {

    private PredicativeMetricNamedInfoProvider<MetricMod> preModsProvider;
    private PredicativeMetricNamedInfoProvider<MetricMod> postModsProvider;
    private TimeMsProvider timeMsProvider;
    private ScheduledExecutorService executor;
    private final Set<String> customMetricImplsPackages = new LinkedHashSet<>();

    public static DefaultMetricRegistryBuilder defaultMetricRegistry() {
        return defaultMetricRegistryBuilder();
    }

    public static DefaultMetricRegistryBuilder defaultMetricRegistryBuilder() {
        return new DefaultMetricRegistryBuilder();
    }

    public DefaultMetricRegistryBuilder preModsProvider(PredicativeMetricNamedInfoProvider<MetricMod> preModsProvider) {
        this.preModsProvider = preModsProvider;
        return this;
    }

    public DefaultMetricRegistryBuilder postModsProvider(PredicativeMetricNamedInfoProvider<MetricMod> postModsProvider) {
        this.postModsProvider = postModsProvider;
        return this;
    }

    public DefaultMetricRegistryBuilder timeMsProvider(TimeMsProvider timeMsProvider) {
        this.timeMsProvider = timeMsProvider;
        return this;
    }

    /**
     * @param executor MUST be single-threaded.
     */
    public DefaultMetricRegistryBuilder executor(ScheduledExecutorService executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Adds custom metric implementations from the specified packages recursively.
     * For more details on how to define custom metric implementations,
     * please see {@link DefaultMetricRegistry#extendWith(CustomRateImplMaker)} and
     * {@link DefaultMetricRegistry#extendWith(CustomHistogramImplMaker)}.
     */
    public DefaultMetricRegistryBuilder withCustomMetricImplsFromPackages(String... packages) {
        customMetricImplsPackages.addAll(List.of(packages));
        return this;
    }

    public DefaultMetricRegistry build() {
        DefaultMetricRegistry registry = new DefaultMetricRegistry(
            preModsProvider != null ? preModsProvider : makeDefaultModsProvider(),
            postModsProvider != null ? postModsProvider : makeDefaultModsProvider(),
            timeMsProvider != null ? timeMsProvider : SystemTimeMsProvider.INSTANCE,
            executor != null ? executor : makeDefaultExecutor());

        if (!customMetricImplsPackages.isEmpty()) {
            addCustomMetricImpls(registry);
        }

        return registry;
    }

    void addCustomMetricImpls(DefaultMetricRegistry registry) {
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(customMetricImplsPackages.toArray(new String[0])));

        reflections.getSubTypesOf(CustomRateImplMaker.class).stream()
            .filter(this::isConcrete)
            .forEach(implMakerClass -> registry.extendWith(makeCustomMetricImplMaker(implMakerClass)));

        reflections.getSubTypesOf(CustomHistogramImplMaker.class).stream()
            .filter(this::isConcrete)
            .forEach(implMakerClass -> registry.extendWith(makeCustomMetricImplMaker(implMakerClass)));
    }

    private boolean isConcrete(Class<?> type) {
        return !type.isInterface() && !isAbstract(type.getModifiers());
    }

    private <M extends CustomMetricImplMaker<?>> M makeCustomMetricImplMaker(Class<M> implMakerClass) {
        try {
            return implMakerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}