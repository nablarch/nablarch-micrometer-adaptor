package nablarch.integration.micrometer.logging;

import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import mockit.Deencapsulation;
import nablarch.integration.micrometer.DefaultMeterBinderListProvider;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link LoggingMeterRegistryFactory}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class LoggingMeterRegistryFactoryTest {

    @Test
    public void testCreateObject() {
        LoggingMeterRegistryFactory sut = new LoggingMeterRegistryFactory();
        sut.setMeterBinderListProvider(new DefaultMeterBinderListProvider());
        sut.setPrefix("test.logging");
        sut.setXmlConfigPath("nablarch/integration/micrometer/logging/LoggingMeterRegistryFactory/testCreateObject/test.xml");

        LoggingMeterRegistry meterRegistry = sut.createObject();

        NablarchLoggingRegistryConfig config = Deencapsulation.getField(meterRegistry, "config");
        assertThat(config.batchSize(), is(98765));
    }
}