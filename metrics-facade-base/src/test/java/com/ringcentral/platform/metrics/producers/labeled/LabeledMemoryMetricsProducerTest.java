package com.ringcentral.platform.metrics.producers.labeled;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.reporters.MetricsJson;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixMetricsJsonExporter;
import com.ringcentral.platform.metrics.stub.StubMetricRegistry;
import com.ringcentral.platform.metrics.test.time.ScheduledExecutorServiceAdapter;
import org.junit.Before;
import org.junit.Test;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LabeledMemoryMetricsProducerTest {

    private MemoryMXBean memoryMXBean;
    private MetricRegistry registry;
    private ZabbixMetricsJsonExporter exporter;

    @Before
    public void setUp() {
        memoryMXBean = mock(MemoryMXBean.class);
        registry = new StubMetricRegistry(new ScheduledExecutorServiceAdapter());
        exporter = new ZabbixMetricsJsonExporter(registry);
    }

    @Test
    public void shouldRegisterGeneralMemoryMetricsCorrectly() {
        // given
        MemoryUsage heapMemoryUsage = mockMemoryUsage(10, 20, 30, 40);
        when(memoryMXBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
        MemoryUsage nonHeapMemoryUsage = mockMemoryUsage(1, 2, 3, 4);
        when(memoryMXBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);

        LabeledMemoryMetricsProducer producer = new LabeledMemoryMetricsProducer(
                MetricName.name("Memory"),
                null,
                memoryMXBean,
                List.of()
        );
        producer.produceMetrics(registry);

        // when
        MetricsJson json = exporter.exportMetrics();

        // then
        assertEquals(
                "{instant=[Memory.committed.heap.value=30, Memory.committed.non-heap.value=3, Memory.committed.total.value=33, Memory.init.heap.value=10, Memory.init.non-heap.value=1, Memory.init.total.value=11, Memory.max.heap.value=40, Memory.max.non-heap.value=4, Memory.max.total.value=44, Memory.usage.heap.value=0.5, Memory.usage.non-heap.value=0.5, Memory.used.heap.value=20, Memory.used.non-heap.value=2, Memory.used.total.value=22]}",
                json.toString()
        );
    }


    @Test
    public void shouldRequestMemoryUsageOnEachCollectionOfMemoryMetrics() {
        // given
        MemoryUsage heapMemoryUsage1 = mockMemoryUsage(10, 20, 30, 40);
        MemoryUsage heapMemoryUsage2 = mockMemoryUsage(50, 60, 70, 80);
        when(memoryMXBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage1, heapMemoryUsage1, heapMemoryUsage1, heapMemoryUsage1, heapMemoryUsage1, heapMemoryUsage1, heapMemoryUsage1, heapMemoryUsage1, heapMemoryUsage1, heapMemoryUsage2);
        MemoryUsage nonHeapMemoryUsage1 = mockMemoryUsage(1, 2, 3, 4);
        MemoryUsage nonHeapMemoryUsage2 = mockMemoryUsage(5, 6, 7, 8);
        when(memoryMXBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage1, nonHeapMemoryUsage1, nonHeapMemoryUsage1, nonHeapMemoryUsage1, nonHeapMemoryUsage1, nonHeapMemoryUsage1, nonHeapMemoryUsage1, nonHeapMemoryUsage1, nonHeapMemoryUsage1, nonHeapMemoryUsage2);

        LabeledMemoryMetricsProducer producer = new LabeledMemoryMetricsProducer(
                MetricName.name("Memory"),
                null,
                memoryMXBean,
                List.of()
        );
        producer.produceMetrics(registry);

        // when
        MetricsJson firstExport = exporter.exportMetrics();

        // then
        assertEquals(
                "{instant=[Memory.committed.heap.value=30, Memory.committed.non-heap.value=3, Memory.committed.total.value=33, Memory.init.heap.value=10, Memory.init.non-heap.value=1, Memory.init.total.value=11, Memory.max.heap.value=40, Memory.max.non-heap.value=4, Memory.max.total.value=44, Memory.usage.heap.value=0.5, Memory.usage.non-heap.value=0.5, Memory.used.heap.value=20, Memory.used.non-heap.value=2, Memory.used.total.value=22]}",
                firstExport.toString()
        );

        // when
        MetricsJson secondExport = exporter.exportMetrics();

        // then
        assertEquals(
                "{instant=[Memory.committed.heap.value=70, Memory.committed.non-heap.value=7, Memory.committed.total.value=77, Memory.init.heap.value=50, Memory.init.non-heap.value=5, Memory.init.total.value=55, Memory.max.heap.value=80, Memory.max.non-heap.value=8, Memory.max.total.value=88, Memory.usage.heap.value=0.75, Memory.usage.non-heap.value=0.75, Memory.used.heap.value=60, Memory.used.non-heap.value=6, Memory.used.total.value=66]}",
                secondExport.toString()
        );
    }

    @Test
    public void shouldCalculateUsageRatioCorrectly() {
        // given
        MemoryUsage heapMemoryUsage = mockMemoryUsage(0, 50, 100, 200);
        when(memoryMXBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
        MemoryUsage nonHeapMemoryUsage = mockMemoryUsage(0, 20, 100, -1);
        when(memoryMXBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);

        LabeledMemoryMetricsProducer producer = new LabeledMemoryMetricsProducer(
                MetricName.name("Memory"),
                null,
                memoryMXBean,
                List.of()
        );
        producer.produceMetrics(registry);

        // when
        MetricsJson json = exporter.exportMetrics();

        // then
        assertEquals(
                "{instant=[Memory.committed.heap.value=100, Memory.committed.non-heap.value=100, Memory.committed.total.value=200, Memory.init.heap.value=0, Memory.init.non-heap.value=0, Memory.init.total.value=0, Memory.max.heap.value=200, Memory.max.non-heap.value=-1, Memory.max.total.value=199, Memory.usage.heap.value=0.25, Memory.usage.non-heap.value=-20.0, Memory.used.heap.value=50, Memory.used.non-heap.value=20, Memory.used.total.value=70]}",
                json.toString());
    }

    @Test
    public void shouldRegisterMemoryPoolMetricsCorrectly() {
        // given
        MemoryUsage heapMemoryUsage = mockMemoryUsage(0, 50, 100, 200);
        when(memoryMXBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
        MemoryUsage nonHeapMemoryUsage = mockMemoryUsage(0, 20, 100, -1);
        when(memoryMXBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);

        MemoryPoolMXBean memoryPoolMXBean = mock(MemoryPoolMXBean.class);
        when(memoryPoolMXBean.getName()).thenReturn("TestPool");
        MemoryUsage t = mockMemoryUsage(1, 2, 3, 4);
        when(memoryPoolMXBean.getUsage()).thenReturn(t);
        MemoryUsage t1 = mockMemoryUsage(0, 5, 0, 0);
        when(memoryPoolMXBean.getCollectionUsage()).thenReturn(t1);

        LabeledMemoryMetricsProducer producer = new LabeledMemoryMetricsProducer(
                MetricName.name("Memory"),
                null,
                memoryMXBean,
                List.of(memoryPoolMXBean)
        );
        producer.produceMetrics(registry);

        // when
        MetricsJson json = exporter.exportMetrics();

        // then
        assertEquals(
                "{instant=[Memory.committed.heap.value=100, Memory.committed.non-heap.value=100, Memory.committed.total.value=200, Memory.init.heap.value=0, Memory.init.non-heap.value=0, Memory.init.total.value=0, Memory.max.heap.value=200, Memory.max.non-heap.value=-1, Memory.max.total.value=199, Memory.pools.committed.TestPool.value=3, Memory.pools.init.TestPool.value=1, Memory.pools.max.TestPool.value=4, Memory.pools.usage.TestPool.value=0.5, Memory.pools.used.TestPool.value=2, Memory.usage.heap.value=0.25, Memory.usage.non-heap.value=-20.0, Memory.used.heap.value=50, Memory.used.non-heap.value=20, Memory.used.total.value=70, Memory.usedAfterGc.TestPool.value=5]}",
                json.toString());
    }

    @Test
    public void shouldRequestMemoryUsageOnEachCollectionOfMemoryPoolMetrics() {
        // given
        MemoryUsage heapMemoryUsage = mockMemoryUsage(10, 20, 30, 40);
        when(memoryMXBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
        MemoryUsage nonHeapMemoryUsage = mockMemoryUsage(1, 2, 3, 4);
        when(memoryMXBean.getNonHeapMemoryUsage()).thenReturn(nonHeapMemoryUsage);

        MemoryPoolMXBean memoryPoolMXBean = mock(MemoryPoolMXBean.class);
        when(memoryPoolMXBean.getName()).thenReturn("TestPool");

        MemoryUsage poolMemoryUsage1 = mockMemoryUsage(5, 6, 7, 8);
        MemoryUsage poolMemoryUsage2 = mockMemoryUsage(9, 10, 11, 12);
        when(memoryPoolMXBean.getUsage()).thenReturn(poolMemoryUsage1, poolMemoryUsage1, poolMemoryUsage1, poolMemoryUsage1, poolMemoryUsage1, poolMemoryUsage2);

        MemoryUsage collectionUsage1 = mockMemoryUsage(0, 42, 0, 0);
        MemoryUsage collectionUsage2 = mockMemoryUsage(0, 43, 0, 0);
        when(memoryPoolMXBean.getCollectionUsage()).thenReturn(collectionUsage1, collectionUsage1, collectionUsage2);

        LabeledMemoryMetricsProducer producer = new LabeledMemoryMetricsProducer(
                MetricName.name("Memory"),
                null,
                memoryMXBean,
                List.of(memoryPoolMXBean)
        );

        producer.produceMetrics(registry);

        // when
        MetricsJson firstExport = exporter.exportMetrics();

        // then
        assertEquals(
                "{instant=[Memory.committed.heap.value=30, Memory.committed.non-heap.value=3, Memory.committed.total.value=33, Memory.init.heap.value=10, Memory.init.non-heap.value=1, Memory.init.total.value=11, Memory.max.heap.value=40, Memory.max.non-heap.value=4, Memory.max.total.value=44, Memory.pools.committed.TestPool.value=7, Memory.pools.init.TestPool.value=5, Memory.pools.max.TestPool.value=8, Memory.pools.usage.TestPool.value=0.75, Memory.pools.used.TestPool.value=6, Memory.usage.heap.value=0.5, Memory.usage.non-heap.value=0.5, Memory.used.heap.value=20, Memory.used.non-heap.value=2, Memory.used.total.value=22, Memory.usedAfterGc.TestPool.value=42]}",
                firstExport.toString()
        );

        // when
        MetricsJson secondExport = exporter.exportMetrics();

        // then
        assertEquals(
                "{instant=[Memory.committed.heap.value=30, Memory.committed.non-heap.value=3, Memory.committed.total.value=33, Memory.init.heap.value=10, Memory.init.non-heap.value=1, Memory.init.total.value=11, Memory.max.heap.value=40, Memory.max.non-heap.value=4, Memory.max.total.value=44, Memory.pools.committed.TestPool.value=11, Memory.pools.init.TestPool.value=9, Memory.pools.max.TestPool.value=12, Memory.pools.usage.TestPool.value=0.8333333333333334, Memory.pools.used.TestPool.value=10, Memory.usage.heap.value=0.5, Memory.usage.non-heap.value=0.5, Memory.used.heap.value=20, Memory.used.non-heap.value=2, Memory.used.total.value=22, Memory.usedAfterGc.TestPool.value=43]}",
                secondExport.toString()
        );
    }

    @Test
    public void shouldSkipUsedAfterGcIfCollectionUsageNull() {
        // given
        MemoryUsage heapUsage = mockMemoryUsage(0, 50, 100, 200);
        when(memoryMXBean.getHeapMemoryUsage()).thenReturn(heapUsage);
        MemoryUsage nonHeapUsage = mockMemoryUsage(0, 20, 100, -1);
        when(memoryMXBean.getNonHeapMemoryUsage()).thenReturn(nonHeapUsage);

        MemoryPoolMXBean memoryPoolMXBean = mock(MemoryPoolMXBean.class);
        when(memoryPoolMXBean.getName()).thenReturn("TestPool");
        MemoryUsage memoryPoolUsage = mockMemoryUsage(0, 0, 0, 0);
        when(memoryPoolMXBean.getUsage()).thenReturn(memoryPoolUsage);
        when(memoryPoolMXBean.getCollectionUsage()).thenReturn(null);

        LabeledMemoryMetricsProducer producer = new LabeledMemoryMetricsProducer(
                MetricName.name("Memory"),
                null,
                memoryMXBean,
                List.of(memoryPoolMXBean)
        );
        producer.produceMetrics(registry);

        // when
        MetricsJson json = exporter.exportMetrics();

        // then
        assertEquals("No gc metrics should not be exported",
                "{instant=[Memory.committed.heap.value=100, Memory.committed.non-heap.value=100, Memory.committed.total.value=200, Memory.init.heap.value=0, Memory.init.non-heap.value=0, Memory.init.total.value=0, Memory.max.heap.value=200, Memory.max.non-heap.value=-1, Memory.max.total.value=199, Memory.pools.committed.TestPool.value=0, Memory.pools.init.TestPool.value=0, Memory.pools.max.TestPool.value=0, Memory.pools.usage.TestPool.value=NaN, Memory.pools.used.TestPool.value=0, Memory.usage.heap.value=0.25, Memory.usage.non-heap.value=-20.0, Memory.used.heap.value=50, Memory.used.non-heap.value=20, Memory.used.total.value=70]}",
                json.toString());
    }

    private MemoryUsage mockMemoryUsage(long init, long used, long committed, long max) {
        MemoryUsage usage = mock(MemoryUsage.class);
        when(usage.getInit()).thenReturn(init);
        when(usage.getUsed()).thenReturn(used);
        when(usage.getCommitted()).thenReturn(committed);
        when(usage.getMax()).thenReturn(max);
        return usage;
    }
}
