package nablarch.integration.micrometer.datadog;

import io.micrometer.datadog.DatadogMeterRegistry;
import mockit.Deencapsulation;
import nablarch.integration.micrometer.MicrometerConfiguration;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link DatadogMeterRegistryFactory}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class DatadogMeterRegistryFactoryTest {

    @Test
    public void testCreateMeterRegistry() {
        DatadogMeterRegistryFactory sut = new DatadogMeterRegistryFactory();
        sut.setPrefix("test.datadog");
        MicrometerConfiguration micrometerConfig = new MicrometerConfiguration("nablarch/integration/micrometer/datadog/DatadogMeterRegistryFactoryTest/testCreateMeterRegistry/test.xml");

        DatadogMeterRegistry meterRegistry = sut.createMeterRegistry(micrometerConfig);

        NablarchDatadogConfig config = Deencapsulation.getField(meterRegistry, "config");
        assertThat(config.apiKey(), is("datadog-test-api-key"));
    }
}