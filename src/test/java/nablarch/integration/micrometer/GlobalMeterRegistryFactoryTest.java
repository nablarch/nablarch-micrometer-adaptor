package nablarch.integration.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import nablarch.core.repository.disposal.BasicApplicationDisposer;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

/**
 * {@link GlobalMeterRegistryFactory}の単体テストクラス。
 * @author Tanaka Tomoyuki
 */
public class GlobalMeterRegistryFactoryTest {

    @Test
    public void testCreateObjectReturnsGlobalRegistry() {
        GlobalMeterRegistryFactory sut = new GlobalMeterRegistryFactory();
        sut.setMeterBinderListProvider(new DefaultMeterBinderListProvider());
        sut.setApplicationDisposer(new BasicApplicationDisposer());

        MeterRegistry meterRegistry = sut.createObject();

        assertThat(meterRegistry, is(sameInstance(Metrics.globalRegistry)));
    }
}