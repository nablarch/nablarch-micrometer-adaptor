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

import java.util.Arrays;
import java.util.List;

/**
 * デフォルトの{@link MeterBinder}リストを提供するクラス。
 * @author Tanaka Tomoyuki
 */
public class DefaultMeterBinderListProvider implements MeterBinderListProvider {

    @Override
    public List<MeterBinder> provide() {
        return Arrays.asList(
            new JvmMemoryMetrics(),
            new JvmGcMetrics(),
            new JvmThreadMetrics(),
            new ClassLoaderMetrics(),
            new ProcessorMetrics(),
            new FileDescriptorMetrics(),
            new UptimeMetrics(),
            new NablarchGcCountMetrics()
        );
    }
}
