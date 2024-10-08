package nablarch.integration.micrometer.instrument.binder.jvm;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import nablarch.integration.micrometer.instrument.binder.MetricsMetaData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link NablarchGcCountMetrics}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class NablarchGcCountMetricsTest {
    private final MockedStatic<ManagementFactory> managementFactoryMockedStatic = Mockito.mockStatic(ManagementFactory.class);
    private final GarbageCollectorMXBean garbageCollectorMXBean = mock(GarbageCollectorMXBean.class);

    private SimpleMeterRegistry registry = new SimpleMeterRegistry();

    @Before
    public void setUp() {
        managementFactoryMockedStatic.when(ManagementFactory::getGarbageCollectorMXBeans)
                .thenReturn(List.of(garbageCollectorMXBean, garbageCollectorMXBean));

        when(garbageCollectorMXBean.getName()).thenReturn("memory-manager-1", "memory-manager-2");
    }

    @After
    public void tearDown() {
        managementFactoryMockedStatic.close();
    }

    @Test
    public void testRegisterCountersParGarbageCollectorMXBeans() {
        NablarchGcCountMetrics metrics = new NablarchGcCountMetrics();
        metrics.bindTo(registry);

        Collection<FunctionCounter> counters = registry.get(NablarchGcCountMetrics.DEFAULT_METRICS_NAME).functionCounters();
        assertThat(counters, iterableWithSize(2));

        Iterator<FunctionCounter> iterator = counters.iterator();

        Meter.Id counter1Id = iterator.next().getId();
        assertThat(counter1Id.getTag("memory.manager.name"), is("memory-manager-1"));
        assertThat(counter1Id.getDescription(), is(NablarchGcCountMetrics.DEFAULT_METRICS_DESCRIPTION));

        Meter.Id counter2Id = iterator.next().getId();
        assertThat(counter2Id.getTag("memory.manager.name"), is("memory-manager-2"));
        assertThat(counter2Id.getDescription(), is(NablarchGcCountMetrics.DEFAULT_METRICS_DESCRIPTION));
    }

    @Test
    public void testCounterMeasuresCollectionCount() {
        when(garbageCollectorMXBean.getCollectionCount()).thenReturn(13L);

        NablarchGcCountMetrics metrics = new NablarchGcCountMetrics();
        metrics.bindTo(registry);

        Collection<FunctionCounter> counters = registry.get(NablarchGcCountMetrics.DEFAULT_METRICS_NAME).functionCounters();
        double count = counters.iterator().next().count();
        assertThat(count, is(13.0));
    }

    @Test
    public void testCanAppendCustomTagsWithConstructor() {
        NablarchGcCountMetrics metrics = new NablarchGcCountMetrics(
                Arrays.asList(Tag.of("foo", "FOO"), Tag.of("bar", "BAR")));
        metrics.bindTo(registry);

        Collection<FunctionCounter> counters = registry.get(NablarchGcCountMetrics.DEFAULT_METRICS_NAME).functionCounters();
        Iterator<FunctionCounter> iterator = counters.iterator();

        Meter.Id counter1Id = iterator.next().getId();
        assertThat(counter1Id.getTag("memory.manager.name"), is("memory-manager-1"));
        assertThat(counter1Id.getTag("foo"), is("FOO"));
        assertThat(counter1Id.getTag("bar"), is("BAR"));

        Meter.Id counter2Id = iterator.next().getId();
        assertThat(counter2Id.getTag("memory.manager.name"), is("memory-manager-2"));
        assertThat(counter2Id.getTag("foo"), is("FOO"));
        assertThat(counter2Id.getTag("bar"), is("BAR"));
    }

    @Test
    public void testCustomMetricsNameAndDescription() {
        NablarchGcCountMetrics metrics = new NablarchGcCountMetrics("test.metrics", "Test metrics.");
        metrics.bindTo(registry);

        Collection<FunctionCounter> counters = registry.get("test.metrics").functionCounters();
        Iterator<FunctionCounter> iterator = counters.iterator();

        Meter.Id counter1Id = iterator.next().getId();
        assertThat(counter1Id.getTag("memory.manager.name"), is("memory-manager-1"));
        assertThat(counter1Id.getDescription(), is("Test metrics."));

        Meter.Id counter2Id = iterator.next().getId();
        assertThat(counter2Id.getTag("memory.manager.name"), is("memory-manager-2"));
        assertThat(counter2Id.getDescription(), is("Test metrics."));
    }

    @Test
    public void testCustomMetricsNameAndDescriptionAndTag() {
        NablarchGcCountMetrics metrics = new NablarchGcCountMetrics(
            "test.metrics",
            "Test metrics.",
            Tags.of("fizz", "FIZZ")
        );
        metrics.bindTo(registry);

        Collection<FunctionCounter> counters = registry.get("test.metrics").functionCounters();
        Iterator<FunctionCounter> iterator = counters.iterator();

        Meter.Id counter1Id = iterator.next().getId();
        assertThat(counter1Id.getTag("memory.manager.name"), is("memory-manager-1"));
        assertThat(counter1Id.getTag("fizz"), is("FIZZ"));
        assertThat(counter1Id.getDescription(), is("Test metrics."));

        Meter.Id counter2Id = iterator.next().getId();
        assertThat(counter2Id.getTag("memory.manager.name"), is("memory-manager-2"));
        assertThat(counter2Id.getTag("fizz"), is("FIZZ"));
        assertThat(counter2Id.getDescription(), is("Test metrics."));
    }

    @Test
    public void testCustomMetricsNameAndDescriptionAndTagWithMetircsMetaData() {
        MetricsMetaData metricsMetaData = new MetricsMetaData(
                "test.metrics.metadata",
                "Test metrics metadata.",
                Tags.of("buzz", "BUZZ")
        );
        NablarchGcCountMetrics metrics = new NablarchGcCountMetrics(metricsMetaData);
        metrics.bindTo(registry);

        Collection<FunctionCounter> counters = registry.get("test.metrics.metadata").functionCounters();
        Iterator<FunctionCounter> iterator = counters.iterator();

        Meter.Id counter1Id = iterator.next().getId();
        assertThat(counter1Id.getTag("memory.manager.name"), is("memory-manager-1"));
        assertThat(counter1Id.getTag("buzz"), is("BUZZ"));
        assertThat(counter1Id.getDescription(), is("Test metrics metadata."));

        Meter.Id counter2Id = iterator.next().getId();
        assertThat(counter2Id.getTag("memory.manager.name"), is("memory-manager-2"));
        assertThat(counter2Id.getTag("buzz"), is("BUZZ"));
        assertThat(counter2Id.getDescription(), is("Test metrics metadata."));
    }
}