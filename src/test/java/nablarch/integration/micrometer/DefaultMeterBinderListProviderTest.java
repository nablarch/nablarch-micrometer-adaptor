package nablarch.integration.micrometer;

import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import nablarch.integration.micrometer.instrument.binder.jvm.NablarchGcCountMetrics;
import nablarch.test.support.log.app.OnMemoryLogWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

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

    private JvmGcMetrics jvmGcMetrics;
    
    private final MockedConstruction<JvmGcMetrics> mocked = Mockito.mockConstruction(JvmGcMetrics.class, (mock, context) -> {
        jvmGcMetrics = mock;
    });

    @After
    public void tearDown() {
        mocked.close();
    }

    @Test
    public void testDisposeAutoCloseableMeterBinder() {
        DefaultMeterBinderListProvider sut = new DefaultMeterBinderListProvider();

        sut.dispose();

        verify(jvmGcMetrics).close();
    }

    @Test
    public void testWarningLogIfCloseThrowsException() {
        DefaultMeterBinderListProvider sut = new DefaultMeterBinderListProvider();
        
        doThrow(new IOException("test IOException")).when(jvmGcMetrics).close();

        sut.dispose();

        OnMemoryLogWriter.assertLogContains("writer.appLog",
                "WARN ROOT Failed to close MeterBinder(" + jvmGcMetrics + ")",
                "test IOException");
    }
}