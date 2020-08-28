package nablarch.integration.micrometer.logging;

import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import mockit.Deencapsulation;
import nablarch.integration.micrometer.MicrometerConfiguration;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link LoggingMeterRegistryFactory}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class LoggingMeterRegistryFactoryTest {

    @Test
    public void testCreateMeterRegistry() {
        LoggingMeterRegistryFactory sut = new LoggingMeterRegistryFactory();
        sut.setPrefix("test.logging");
        MicrometerConfiguration micrometerConfig = new MicrometerConfiguration("nablarch/integration/micrometer/logging/LoggingMeterRegistryFactory/testCreateMeterRegistry/test.xml");

        LoggingMeterRegistry meterRegistry = sut.createMeterRegistry(micrometerConfig);

        NablarchLoggingRegistryConfig config = Deencapsulation.getField(meterRegistry, "config");
        assertThat(config.batchSize(), is(98765));
    }
}