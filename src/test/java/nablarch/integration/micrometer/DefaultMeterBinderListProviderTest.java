package nablarch.integration.micrometer;

import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.binder.tomcat.TomcatMetrics;
import nablarch.integration.micrometer.instrument.binder.jvm.NablarchGcCountMetrics;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;

/**
 * {@link DefaultMeterBinderListProvider}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class DefaultMeterBinderListProviderTest {

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
}