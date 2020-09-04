package nablarch.integration.micrometer.instrument.binder.jvm;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Before;
import org.junit.Test;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;

/**
 * {@link NablarchGcCountMetrics}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class NablarchGcCountMetricsTest {
    @Mocked
    private ManagementFactory managementFactory;
    @Mocked
    private GarbageCollectorMXBean garbageCollectorMXBean;

    private SimpleMeterRegistry registry = new SimpleMeterRegistry();

    @Before
    public void setUp() {
        new Expectations() {{
            ManagementFactory.getGarbageCollectorMXBeans();
            result = Arrays.asList(garbageCollectorMXBean, garbageCollectorMXBean);

            garbageCollectorMXBean.getName();
            returns("memory-manager-1", "memory-manager-2");
        }};
    }

    @Test
    public void testRegisterCountersParGarbageCollectorMXBeans() {
        NablarchGcCountMetrics metrics = new NablarchGcCountMetrics();
        metrics.bindTo(registry);

        Collection<FunctionCounter> counters = registry.get("jvm.gc.count").functionCounters();
        assertThat(counters, iterableWithSize(2));

        Iterator<FunctionCounter> iterator = counters.iterator();

        Meter.Id counter1Id = iterator.next().getId();
        assertThat(counter1Id.getTag("memory.manager.name"), is("memory-manager-1"));
        assertThat(counter1Id.getDescription(), is("Count of garbage collection"));

        Meter.Id counter2Id = iterator.next().getId();
        assertThat(counter2Id.getTag("memory.manager.name"), is("memory-manager-2"));
        assertThat(counter2Id.getDescription(), is("Count of garbage collection"));
    }

    @Test
    public void testCounterMeasuresCollectionCount() {
        new Expectations() {{
            garbageCollectorMXBean.getCollectionCount(); result = 13L;
        }};

        NablarchGcCountMetrics metrics = new NablarchGcCountMetrics();
        metrics.bindTo(registry);

        Collection<FunctionCounter> counters = registry.get("jvm.gc.count").functionCounters();
        double count = counters.iterator().next().count();
        assertThat(count, is(13.0));
    }

    @Test
    public void testCanAppendCustomTagsWithConstructor() {
        NablarchGcCountMetrics metrics = new NablarchGcCountMetrics(
                Arrays.asList(Tag.of("foo", "FOO"), Tag.of("bar", "BAR")));
        metrics.bindTo(registry);

        Collection<FunctionCounter> counters = registry.get("jvm.gc.count").functionCounters();
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
}