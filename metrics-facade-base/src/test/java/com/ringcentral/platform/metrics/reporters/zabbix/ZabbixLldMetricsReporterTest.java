package com.ringcentral.platform.metrics.reporters.zabbix;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.infoProviders.*;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixLldMetricsReporter.Rule;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixLldMetricsReporter.*;
import com.ringcentral.platform.metrics.utils.Ref;
import org.junit.*;

import java.util.List;

import static com.ringcentral.platform.metrics.names.MetricName.*;
import static com.ringcentral.platform.metrics.names.MetricNameMask.*;
import static com.ringcentral.platform.metrics.predicates.DefaultMetricInstancePredicate.*;
import static java.util.function.Predicate.*;
import static java.util.stream.Collectors.*;
import static org.mockito.Mockito.*;

public class ZabbixLldMetricsReporterTest {

    static final Label SERVICE = new Label("service");
    static final Label SERVER = new Label("server");
    static final Label PORT = new Label("port");

    PredicativeMetricNamedInfoProvider<Rule> rulesProvider;
    ZabbixLldMetricsReporterListener listener = mock(ZabbixLldMetricsReporterListener.class);
    ZabbixLldMetricsReporter reporter;

    @Before
    public void before() {
        rulesProvider = new DefaultConcurrentMetricNamedInfoProvider<>();
        reporter = new ZabbixLldMetricsReporter(rulesProvider, listener);
    }

    @Test
    public void notifyingListenersThatEntitySetUpdated() {
        String serviceAttrName = SERVICE.name() + "_attrName";
        String serverAttrName = SERVER.name() + "_attrName";

        reporter.addRules(
            forMetricInstancesMatching(
                nameMask("ActiveHealthChecker.healthCheck.attemptCount.**"),
                not(MetricInstance::isLevelInstance)),
            new Rule("serviceInstances", List.of(new RuleItem(SERVICE, serviceAttrName), new RuleItem(SERVER, serverAttrName))));

        Histogram histogram = mock(Histogram.class);
        Ref<MetricListener> metricListenerRef = new Ref<>();

        doAnswer(invocation -> {
            MetricListener metricListener = (MetricListener)invocation.getArguments()[0];
            metricListenerRef.setValue(metricListener);
            return null;
        }).when(histogram).addListener(any());

        reporter.histogramAdded(histogram);
        verifyZeroInteractions(listener);

        MeterInstance meterInstance_1 = mock(MeterInstance.class);
        when(meterInstance_1.name()).thenReturn(metricName("ActiveHealthChecker", "healthCheck", "attemptCount", "histogram"));
        when(meterInstance_1.hasLabelValues()).thenReturn(true);
        List<LabelValue> labelValues = List.of(SERVICE.value("service_1"), SERVER.value("server_1"));
        when(meterInstance_1.labelValues()).thenReturn(labelValues);
        when(meterInstance_1.labelToValue()).thenReturn(labelValues.stream().collect(toMap(LabelValue::label, lv -> lv)));
        when(meterInstance_1.valueOf(SERVICE)).thenReturn("service_1");
        when(meterInstance_1.valueOf(SERVER)).thenReturn("server_1");
        when(meterInstance_1.isLevelInstance()).thenReturn(false);
        metricListenerRef.value().metricInstanceAdded(meterInstance_1);

        verify(listener).entityAdded(new ZEntity(
            "serviceInstances",
            List.of(new ZAttribute(serviceAttrName, "service_1"), new ZAttribute(serverAttrName, "server_1"))));

        verifyNoMoreInteractions(listener);

        MeterInstance meterInstance_2 = mock(MeterInstance.class);
        when(meterInstance_2.name()).thenReturn(metricName("ActiveHealthChecker", "healthCheck", "attemptCount", "histogram"));
        when(meterInstance_2.hasLabelValues()).thenReturn(true);
        labelValues = List.of(SERVICE.value("service_1"), SERVER.value("server_1"), PORT.value("port_1"));
        when(meterInstance_2.labelValues()).thenReturn(labelValues);
        when(meterInstance_2.labelToValue()).thenReturn(labelValues.stream().collect(toMap(LabelValue::label, lv -> lv)));
        when(meterInstance_2.valueOf(SERVICE)).thenReturn("service_1");
        when(meterInstance_2.valueOf(SERVER)).thenReturn("server_1");
        when(meterInstance_2.valueOf(PORT)).thenReturn("port_1");
        when(meterInstance_2.isLevelInstance()).thenReturn(false);
        metricListenerRef.value().metricInstanceAdded(meterInstance_2);

        verifyNoMoreInteractions(listener);

        metricListenerRef.value().metricInstanceRemoved(meterInstance_1);
        verifyNoMoreInteractions(listener);

        metricListenerRef.value().metricInstanceRemoved(meterInstance_2);

        verify(listener).entityRemoved(new ZEntity(
            "serviceInstances",
            List.of(new ZAttribute(serviceAttrName, "service_1"), new ZAttribute(serverAttrName, "server_1"))));

        verifyNoMoreInteractions(listener);

        meterInstance_1 = mock(MeterInstance.class);
        when(meterInstance_1.name()).thenReturn(metricName("ActiveHealthChecker", "healthCheck", "NOT_MATCHING", "histogram"));
        when(meterInstance_1.hasLabelValues()).thenReturn(true);
        labelValues = List.of(SERVICE.value("service_1"), SERVER.value("server_1"));
        when(meterInstance_1.labelValues()).thenReturn(labelValues);
        when(meterInstance_1.labelToValue()).thenReturn(labelValues.stream().collect(toMap(LabelValue::label, lv -> lv)));
        when(meterInstance_1.valueOf(SERVICE)).thenReturn("service_1");
        when(meterInstance_1.valueOf(SERVER)).thenReturn("server_1");
        when(meterInstance_1.isLevelInstance()).thenReturn(false);
        metricListenerRef.value().metricInstanceAdded(meterInstance_1);

        verifyNoMoreInteractions(listener);

        meterInstance_1 = mock(MeterInstance.class);
        when(meterInstance_1.name()).thenReturn(metricName("ActiveHealthChecker", "healthCheck", "attemptCount", "histogram"));
        when(meterInstance_1.hasLabelValues()).thenReturn(true);
        labelValues = List.of(SERVICE.value("service_1"), SERVER.value("server_1"));
        when(meterInstance_1.labelValues()).thenReturn(labelValues);
        when(meterInstance_1.labelToValue()).thenReturn(labelValues.stream().collect(toMap(LabelValue::label, lv -> lv)));
        when(meterInstance_1.valueOf(SERVICE)).thenReturn("service_1");
        when(meterInstance_1.valueOf(SERVER)).thenReturn("server_1");
        when(meterInstance_1.isLevelInstance()).thenReturn(true);
        metricListenerRef.value().metricInstanceAdded(meterInstance_1);

        verifyNoMoreInteractions(listener);

        meterInstance_1 = mock(MeterInstance.class);
        when(meterInstance_1.name()).thenReturn(metricName("ActiveHealthChecker", "healthCheck", "attemptCount", "histogram"));
        when(meterInstance_1.hasLabelValues()).thenReturn(true);
        labelValues = List.of(SERVICE.value("service_1"), PORT.value("port_1"));
        when(meterInstance_1.labelValues()).thenReturn(labelValues);
        when(meterInstance_1.labelToValue()).thenReturn(labelValues.stream().collect(toMap(LabelValue::label, lv -> lv)));
        when(meterInstance_2.valueOf(SERVICE)).thenReturn("service_1");
        when(meterInstance_2.valueOf(PORT)).thenReturn("port_1");
        when(meterInstance_1.isLevelInstance()).thenReturn(true);
        metricListenerRef.value().metricInstanceAdded(meterInstance_1);

        verifyNoMoreInteractions(listener);
    }
}