package nablarch.integration.micrometer.otlp;

import io.micrometer.registry.otlp.OtlpMeterRegistry;
import mockit.Deencapsulation;
import nablarch.core.repository.disposal.BasicApplicationDisposer;
import nablarch.integration.micrometer.DefaultMeterBinderListProvider;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link OtlpMeterRegistryFactory}の単体テスト。
 * @author Junya Koyama
 */
public class OtlpMeterRegistryFactoryTest {

    @Test
    public void testCreateObject() {
        OtlpMeterRegistryFactory sut = new OtlpMeterRegistryFactory();
        sut.setApplicationDisposer(new BasicApplicationDisposer());
        sut.setMeterBinderListProvider(new DefaultMeterBinderListProvider());
        sut.setPrefix("test.otlp");
        sut.setXmlConfigPath("nablarch/integration/micrometer/otlp/OtlpMeterRegistryFactory/testCreateObject/test.xml");

        OtlpMeterRegistry meterRegistry = sut.createObject();

        NablarchOtlpConfig config = Deencapsulation.getField(meterRegistry, "config");
        assertThat(config.url(), is("http://localhost:4318/v1/metrics"));
    }
}
