package nablarch.integration.micrometer;

import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import nablarch.integration.micrometer.instrument.binder.jvm.NablarchGcCountMetrics;
import nablarch.test.support.log.app.OnMemoryLogWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;

/**
 * {@link DefaultMeterBinderListProvider}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class DefaultMeterBinderListProviderTest {

    @Before
    public void setUp() {
        OnMemoryLogWriter.clear();
    }

    @Test
    public void testDefaultProvidedMetricsList() {
        DefaultMeterBinderListProvider sut = new DefaultMeterBinderListProvider();
        List<MeterBinder> meterBinderList = sut.provide();

        assertThat(meterBinderList, containsInAnyOrder(
            instanceOf(JvmMemoryMetrics.class),
            instanceOf(JvmGcMetrics.class),
            instanceOf(JvmThreadMetrics.class),
            instanceOf(ClassLoaderMetrics.class),
            instanceOf(ProcessorMetrics.class),
            instanceOf(FileDescriptorMetrics.class),
            instanceOf(UptimeMetrics.class),
            instanceOf(NablarchGcCountMetrics.class)
        ));
    }

    @Mocked
    private JvmGcMetrics jvmGcMetrics;

    @Test
    public void testDisposeAutoCloseableMeterBinder() {
        DefaultMeterBinderListProvider sut = new DefaultMeterBinderListProvider();

        sut.dispose();

        new Verifications() {{
            jvmGcMetrics.close(); times = 1;
        }};
    }

    @Test
    public void testWarningLogIfCloseThrowsException() {
        new Expectations() {{
            jvmGcMetrics.close(); result = new IOException("test IOException");
        }};

        DefaultMeterBinderListProvider sut = new DefaultMeterBinderListProvider();

        sut.dispose();

        OnMemoryLogWriter.assertLogContains("writer.appLog",
                "WARN ROOT Failed to close MeterBinder(io.micrometer.core.instrument.binder.jvm.JvmGcMetrics",
                "test IOException");
    }
}