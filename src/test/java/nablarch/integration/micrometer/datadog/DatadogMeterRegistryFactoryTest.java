package nablarch.integration.micrometer.datadog;

import io.micrometer.datadog.DatadogMeterRegistry;
import mockit.Deencapsulation;
import nablarch.integration.micrometer.DefaultMeterBinderListProvider;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link DatadogMeterRegistryFactory}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class DatadogMeterRegistryFactoryTest {

    @Test
    public void testCreateObject() {
        DatadogMeterRegistryFactory sut = new DatadogMeterRegistryFactory();
        sut.setMeterBinderListProvider(new DefaultMeterBinderListProvider());
        sut.setPrefix("test.datadog");
        sut.setXmlConfigPath("nablarch/integration/micrometer/datadog/DatadogMeterRegistryFactoryTest/testCreateObject/test.xml");

        DatadogMeterRegistry meterRegistry = sut.createObject();

        NablarchDatadogConfig config = Deencapsulation.getField(meterRegistry, "config");
        assertThat(config.apiKey(), is("datadog-test-api-key"));
    }
}